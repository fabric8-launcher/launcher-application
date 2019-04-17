package io.fabric8.launcher.core.impl.preparers;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;

import javax.enterprise.context.ApplicationScoped;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.fabric8.launcher.base.JsonUtils;
import io.fabric8.launcher.booster.catalog.rhoar.RhoarBooster;
import io.fabric8.launcher.core.api.ProjectileContext;
import io.fabric8.launcher.core.api.projectiles.context.CoordinateCapable;
import io.fabric8.launcher.core.api.projectiles.context.ProjectNameCapable;
import io.fabric8.launcher.core.spi.ProjectilePreparer;

import static io.fabric8.launcher.base.JsonUtils.readTree;
import static java.nio.file.Files.exists;
import static java.nio.file.Files.readAllBytes;

/**
 * Called by {@link io.fabric8.launcher.core.api.MissionControl#prepare(ProjectileContext)}
 *
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@ApplicationScoped
public class ChangeNodeJSMetadataPreparer implements ProjectilePreparer {

    @Override
    public void prepare(Path projectPath, RhoarBooster booster, ProjectileContext context) {
        if (!(context instanceof CoordinateCapable) || !(context instanceof ProjectNameCapable)) {
            return;
        }

        CoordinateCapable coordinateCapable = (CoordinateCapable) context;
        Path packageJsonPath = projectPath.resolve("package.json");
        if (exists(packageJsonPath)) {
            try {
                final String projectName = ((ProjectNameCapable) context).getProjectName();
                final String projectVersion = coordinateCapable.getProjectVersion();

                final ObjectNode packageJson = (ObjectNode) readTree(new String(readAllBytes(packageJsonPath)));
                if (projectName != null) {
                    packageJson.put("name", projectName);
                }
                if (projectVersion != null) {
                    packageJson.put("version", projectVersion);
                }
                JsonUtils.writeTree(packageJson, packageJsonPath.toFile());
            } catch (IOException e) {
                throw new UncheckedIOException("Error while reading " + packageJsonPath, e);
            }
        }
    }
}
