package io.fabric8.launcher.core.impl.preparers;

import java.nio.file.Files;
import java.nio.file.Path;

import javax.enterprise.context.ApplicationScoped;

import io.fabric8.launcher.base.maven.Maven;
import io.fabric8.launcher.booster.catalog.rhoar.RhoarBooster;
import io.fabric8.launcher.core.api.ProjectileContext;
import io.fabric8.launcher.core.api.projectiles.context.BoosterCapable;
import io.fabric8.launcher.core.api.projectiles.context.CoordinateCapable;
import io.fabric8.launcher.core.spi.ProjectilePreparer;
import org.apache.maven.model.Activation;
import org.apache.maven.model.Model;
import org.apache.maven.model.Parent;
import org.apache.maven.model.Profile;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@ApplicationScoped
public class ChangeMavenMetadataPreparer implements ProjectilePreparer {

    @Override
    public void prepare(Path projectPath, RhoarBooster booster, ProjectileContext context) {
        if (!(context instanceof CoordinateCapable)) {
            return;
        }
        CoordinateCapable coordinateCapable = (CoordinateCapable) context;
        Path pom = projectPath.resolve("pom.xml");
        // Perform model changes
        if (Files.isRegularFile(pom)) {
            Model model = Maven.readModel(pom);
            model.setGroupId(coordinateCapable.getGroupId());
            model.setArtifactId(coordinateCapable.getArtifactId());
            model.setVersion(coordinateCapable.getProjectVersion());

            String profileId = null;
            if (context instanceof BoosterCapable) {
                if (((BoosterCapable) context).getRuntime() != null) {
                    profileId = ((BoosterCapable) context).getRuntime().getId();
                }
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
                    Model moduleModel = Maven.readModel(modulePom);
                    Parent parent = moduleModel.getParent();
                    if (parent != null) {
                        parent.setGroupId(model.getGroupId());
                        parent.setArtifactId(model.getArtifactId());
                        parent.setVersion(model.getVersion());
                        Maven.writeModel(moduleModel);
                    }
                }
            }
            Maven.writeModel(model);
        }
    }
}