package io.fabric8.launcher.base.maven;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public final class Maven {

    private Maven() {
        throw new IllegalAccessError("Utility class");
    }

    /**
     * Read the {@link Path} as a {@link Model}
     *
     * @param pom a path to a pom.xml file
     * @return the maven {@link Model}
     */
    public static Model readModel(Path pom) {
        Model model;
        MavenXpp3Reader reader = new MavenXpp3Reader();
        try (BufferedReader br = Files.newBufferedReader(pom)) {
            model = reader.read(br);
            model.setPomFile(pom.toFile());
        } catch (IOException io) {
            throw new UncheckedIOException("Error while reading pom.xml", io);
        } catch (XmlPullParserException e) {
            throw new RuntimeException("Error while parsing pom.xml", e);
        }
        return model;
    }

    /**
     * Shortcut to writeModel(model,model.getPomFile().toPath());
     *
     * @param model
     */
    public static void writeModel(Model model) {
        writeModel(model, model.getPomFile().toPath());
    }

    /**
     * Write the Model back to the provided {@link Path}
     *
     * @param model
     * @param pom
     */
    public static void writeModel(Model model, Path pom) {
        if (pom.toFile().length() == 0L) {
            // Initialize an empty XML
            try (OutputStream os = Files.newOutputStream(pom)) {
                MavenXpp3Writer writer = new MavenXpp3Writer();
                writer.write(os, model);
            } catch (IOException e) {
                throw new UncheckedIOException("Could not write POM file: " + pom, e);
            }
        } else {
            Document document;
            try (InputStream is = Files.newInputStream(pom)) {
                document = new SAXBuilder().build(is);
            } catch (JDOMException e) {
                throw new RuntimeException("Could not parse POM file: " + pom, e);
            } catch (IOException e) {
                throw new UncheckedIOException("Could not read POM file: " + pom, e);
            }
            try (OutputStream os = Files.newOutputStream(pom);
                 OutputStreamWriter ow = new OutputStreamWriter(os)) {
                MavenJDOMWriter writer = new MavenJDOMWriter();
                writer.write(model, document, "UTF-8", ow);
            } catch (IOException e) {
                throw new UncheckedIOException("Could not write POM file: " + pom, e);
            }
        }
    }
}
