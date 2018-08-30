package io.fabric8.launcher.osio.jenkins;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
class DomHelper {
    private static TransformerFactory transformerFactory;

    private static Transformer transformer;

    private DomHelper() {
        throw new IllegalAccessError("Utility class");
    }

    static Document parseDoc(final InputStream is)
            throws ParserConfigurationException,
            SAXException,
            IOException {
        try {
            BufferedInputStream in = new BufferedInputStream(is);
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            InputSource source = new InputSource(in);
            return builder.parse(source);
        } finally {
            is.close();
        }
    }


    static String toXml(Document document) throws TransformerException {
        Transformer transformer = getTransformer();
        StringWriter buffer = new StringWriter();
        transformer.transform(new DOMSource(document), new StreamResult(buffer));
        return buffer.toString();
    }

    private static Transformer getTransformer() throws TransformerConfigurationException {
        if (transformer == null) {
            transformer = getTransformerFactory().newTransformer();
        }
        return transformer;
    }

    private static TransformerFactory getTransformerFactory() {
        if (transformerFactory == null) {
            transformerFactory = TransformerFactory.newInstance();
            try {
                transformerFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            } catch (TransformerConfigurationException e) {
                Logger.getLogger(DomHelper.class.getName())
                        .log(Level.WARNING, "Error while setting FEATURE_SECURE_PROCESSING", e);
            }
        }
        return transformerFactory;
    }

    /**
     * Returns the first child element for the given name
     */
    static Element firstChild(Element element, String name) {
        NodeList nodes = element.getChildNodes();
        if (nodes != null) {
            for (int i = 0, size = nodes.getLength(); i < size; i++) {
                Node item = nodes.item(i);
                if (item instanceof Element) {
                    Element childElement = (Element) item;

                    if (name.equals(childElement.getTagName())) {
                        return childElement;
                    }
                }
            }
        }
        return null;
    }
}
