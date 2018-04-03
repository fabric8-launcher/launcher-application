package io.fabric8.launcher.core.impl.preparers;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.enterprise.context.ApplicationScoped;

import io.fabric8.launcher.booster.catalog.rhoar.RhoarBooster;
import io.fabric8.launcher.core.api.CreateProjectileContext;
import io.fabric8.launcher.core.api.ProjectileContext;
import io.fabric8.launcher.core.spi.ProjectilePreparer;
import org.apache.maven.model.Activation;
import org.apache.maven.model.Model;
import org.apache.maven.model.Parent;
import org.apache.maven.model.Profile;
import org.apache.maven.model.io.jdom.MavenJDOMWriter;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@ApplicationScoped
public class ChangeMavenMetadataPreparer implements ProjectilePreparer {

    @Override
    public void prepare(Path projectPath, RhoarBooster booster, ProjectileContext context) {
        if (!(context instanceof CreateProjectileContext)) {
            return;
        }
        CreateProjectileContext createProjectileContext = (CreateProjectileContext) context;
        Path pom = projectPath.resolve("pom.xml");
        // Perform model changes
        if (Files.isRegularFile(pom)) {
            Model model = getModel(pom);
            model.setGroupId(createProjectileContext.getGroupId());
            model.setArtifactId(createProjectileContext.getArtifactId());
            model.setVersion(createProjectileContext.getProjectVersion());

            String profileId = null;
            if (createProjectileContext.getRuntime() != null) {
                profileId = createProjectileContext.getRuntime().getId();
            }
            profileId = booster.getMetadata("buildProfile", profileId);
            if (profileId != null) {
                // Set the corresponding profile as active
                for (Profile p : model.getProfiles()) {
                    boolean isActive = profileId.equals(p.getId());
                    Activation act = p.getActivation();
                    if (act == null) {
                        act = new Activation();
                        p.setActivation(act);
                    }
                    act.setActiveByDefault(isActive);
                }
            }

            // Change child modules
            for (String module : model.getModules()) {
                Path modulePom = projectPath.resolve(module).resolve("pom.xml");
                if (Files.isRegularFile(modulePom)) {
                    Model moduleModel = getModel(modulePom);
                    Parent parent = moduleModel.getParent();
                    if (parent != null) {
                        parent.setGroupId(model.getGroupId());
                        parent.setArtifactId(model.getArtifactId());
                        parent.setVersion(model.getVersion());
                        setModel(moduleModel, modulePom);
                    }
                }
            }
            setModel(model, pom);
        }
    }

    //TODO: Move to a public utility class
    private Model getModel(Path pom) {
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

    //TODO: Move to a public utility class
    private void setModel(Model model, Path pom) {
        Document document;
        try (InputStream is = Files.newInputStream(pom)) {
            document = new SAXBuilder().build(is);
        } catch (JDOMException e) {
            throw new RuntimeException("Could not parse POM file: " + pom, e);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not read POM file: " + pom, e);
        }
        try (BufferedWriter bw = Files.newBufferedWriter(pom)) {
            MavenJDOMWriter writer = new MavenJDOMWriter();
            writer.write(model, document, bw);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not write POM file: " + pom, e);
        }
    }
}