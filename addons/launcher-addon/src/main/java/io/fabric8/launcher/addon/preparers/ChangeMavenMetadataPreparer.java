package io.fabric8.launcher.addon.preparers;

import java.nio.file.Path;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import io.fabric8.launcher.booster.catalog.rhoar.RhoarBooster;
import io.fabric8.launcher.core.api.CreateProjectileContext;
import io.fabric8.launcher.core.api.ProjectileContext;
import io.fabric8.launcher.core.spi.ProjectilePreparer;
import org.apache.maven.model.Activation;
import org.apache.maven.model.Model;
import org.apache.maven.model.Parent;
import org.apache.maven.model.Profile;
import org.jboss.forge.addon.maven.resources.MavenModelResource;
import org.jboss.forge.addon.resource.DirectoryResource;
import org.jboss.forge.addon.resource.ResourceFactory;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@ApplicationScoped
public class ChangeMavenMetadataPreparer implements ProjectilePreparer {
    @Inject
    private ResourceFactory resourceFactory;

    @Override
    public void prepare(Path projectPath, RhoarBooster booster, ProjectileContext context) {
        if (!(context instanceof CreateProjectileContext)) {
            return;
        }
        CreateProjectileContext createProjectileContext = (CreateProjectileContext) context;
        DirectoryResource projectDirectory = resourceFactory.create(projectPath.toFile()).as(DirectoryResource.class);
        MavenModelResource modelResource = projectDirectory.getChildOfType(MavenModelResource.class, "pom.xml");

        // Perform model changes
        if (modelResource.exists()) {
            Model model = modelResource.getCurrentModel();
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
                DirectoryResource moduleDirResource = projectDirectory.getChildDirectory(module);
                MavenModelResource moduleModelResource = moduleDirResource.getChildOfType(MavenModelResource.class,
                                                                                          "pom.xml");
                Model moduleModel = moduleModelResource.getCurrentModel();
                Parent parent = moduleModel.getParent();
                if (parent != null) {
                    parent.setGroupId(model.getGroupId());
                    parent.setArtifactId(model.getArtifactId());
                    parent.setVersion(model.getVersion());
                    moduleModelResource.setCurrentModel(moduleModel);
                }
            }
            modelResource.setCurrentModel(model);
        }
    }
}
