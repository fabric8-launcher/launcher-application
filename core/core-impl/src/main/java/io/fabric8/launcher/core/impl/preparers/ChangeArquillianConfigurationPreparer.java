package io.fabric8.launcher.core.impl.preparers;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

import javax.enterprise.context.ApplicationScoped;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import io.fabric8.launcher.booster.catalog.rhoar.RhoarBooster;
import io.fabric8.launcher.core.api.CreateProjectileContext;
import io.fabric8.launcher.core.api.ProjectileContext;
import io.fabric8.launcher.core.spi.ProjectilePreparer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

@ApplicationScoped
public class ChangeArquillianConfigurationPreparer implements ProjectilePreparer {

    private Logger LOG = Logger.getLogger(ChangeArquillianConfigurationPreparer.class.getName());

    @Override
    public void prepare(Path projectPath, RhoarBooster booster, ProjectileContext context) {
        if (!(context instanceof CreateProjectileContext)) {
            return;
        }
        CreateProjectileContext createProjectileContext = (CreateProjectileContext) context;
        updateArquillianConfiguration(projectPath, createProjectileContext.getArtifactId());
    }

    void updateArquillianConfiguration(Path projectPath, String property) {
        try (Stream<Path> stream = Files.walk(projectPath)) {
            stream.filter(path -> path.endsWith("arquillian.xml"))
                    .forEach(path -> prepareArquillianConfig(path, property));
        } catch (IOException e) {
            throw new UncheckedIOException("Error while traversing project " + projectPath, e);
        }
    }

    private Document parseAsXml(File file) {
        try {
            DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            return documentBuilder.parse(file);
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Failed to parse xml from file" + file.getAbsolutePath());
        }
        return null;
    }

    private void prepareArquillianConfig(Path path, String propertyValue) {
        final Document document = parseAsXml(path.toFile());
        NodeList nodes = getNodeList(document, "/arquillian/extension[@qualifier='openshift']/property[@name='app.name']");
        if (nodes != null) {
            if (nodes.getLength() > 0) {
                nodes.item(0).setTextContent(propertyValue);
            } else {
                addPropertyToOpenshiftExtension(document, propertyValue);
            }
            removeWhiteSpaceNodes(document);
        }
        try {
            Transformer xformer = TransformerFactory.newInstance().newTransformer();
            xformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            xformer.setOutputProperty(OutputKeys.INDENT, "yes");
            xformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            xformer.transform(new DOMSource(document), new StreamResult(path.toFile()));
        } catch (TransformerException e) {
            LOG.log(Level.WARNING, "Failed to update configuration for arquillian in " + path.toAbsolutePath(), e);
        }
    }

    private void removeWhiteSpaceNodes(Document document) {
        NodeList nodeList = getNodeList(document, "//text()[normalize-space()='']");

        if (nodeList != null) {
            for (int i = 0; i < nodeList.getLength(); ++i) {
                Node node = nodeList.item(i);
                node.getParentNode().removeChild(node);
            }
        }
    }

    private void addPropertyToOpenshiftExtension(Document document, String propertyValue) {
        NodeList nodes = getNodeList(document, "/arquillian/extension[@qualifier='openshift']");

        if (nodes != null && nodes.getLength() > 0) {
            final Element element = document.createElement("property");
            element.setAttribute("name", "app.name");
            element.setTextContent(propertyValue);

            nodes.item(0).appendChild(element);
        }
    }

    private NodeList getNodeList(Document document, String expression) {
        if (document != null) {
            XPath xpath = XPathFactory.newInstance().newXPath();
            try {
                return
                        (NodeList) xpath.evaluate(expression, document, XPathConstants.NODESET);
            } catch (XPathExpressionException e) {
                LOG.log(Level.WARNING,
                        "Error while evaluating xpath expression for `" + expression + "`");
            }
        }
        return null;
    }
}
