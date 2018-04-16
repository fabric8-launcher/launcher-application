package io.fabric8.launcher.osio.preparers;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import io.fabric8.launcher.base.maven.Maven;
import io.fabric8.launcher.booster.catalog.rhoar.RhoarBooster;
import io.fabric8.launcher.core.api.ProjectileContext;
import io.fabric8.launcher.core.spi.ProjectilePreparer;
import io.fabric8.launcher.osio.projectiles.context.OsioProjectileContext;
import org.apache.maven.model.Model;
import org.apache.maven.model.Dependency;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class InsertMavenDependenciesPreparer implements ProjectilePreparer {

    private static final Logger log = Logger.getLogger(InsertMavenDependenciesPreparer.class.getName());


    @Override
    public void prepare(Path projectPath, RhoarBooster booster, ProjectileContext context) {
        if (!(context instanceof OsioProjectileContext)) {
            return;
        }
        OsioProjectileContext createProjectileContext = (OsioProjectileContext) context;
        Path pom = projectPath.resolve("pom.xml");
        List<Dependency> dependencies = createProjectileContext.getDependencies();

        // Perform model changes
        if (Files.isRegularFile(pom) && !dependencies.isEmpty()) {
            try {
                Model model = Maven.readModel(pom);

                List<String> boosterDependencies = model.getDependencies().stream().map(d ->
                        (d.getGroupId() + ":" + d.getArtifactId())
                ).collect(Collectors.toList());

                for (Dependency dep : dependencies) {
                    if (!isDuplicateDependency(boosterDependencies, dep)) {
                        model.addDependency(dep);
                    }
                }
                Maven.writeModel(model);
            } catch (Exception e) {
                log.log(Level.SEVERE, "An exception occurred while adding the dependencies. " + e.toString());
            }
        }
    }

    private boolean isDuplicateDependency(List<String> allDependencies, Dependency dependency) {
        return allDependencies.contains(dependency.getGroupId() + ":" + dependency.getArtifactId());
    }
}
