package io.fabric8.launcher.addon.preparers;

import java.nio.file.Path;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;

import io.fabric8.launcher.booster.catalog.rhoar.RhoarBooster;
import io.fabric8.launcher.core.api.CreateProjectileContext;
import io.fabric8.launcher.core.api.ProjectileContext;
import io.fabric8.launcher.core.spi.ProjectilePreparer;
import org.jboss.forge.addon.parser.json.resource.JsonResource;
import org.jboss.forge.addon.resource.DirectoryResource;
import org.jboss.forge.addon.resource.ResourceFactory;

/**
 * Called by {@link io.fabric8.launcher.core.api.MissionControl#prepare(ProjectileContext)}
 *
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@ApplicationScoped
public class ChangeNodeJSMetadataPreparer implements ProjectilePreparer {

    @Inject
    private ResourceFactory resourceFactory;

    @Override
    public void prepare(Path projectPath, RhoarBooster booster, ProjectileContext context) {
        if (!(context instanceof CreateProjectileContext)) {
            return;
        }
        DirectoryResource projectDirectory = resourceFactory.create(projectPath.toFile()).as(DirectoryResource.class);
        JsonResource packageJsonResource = projectDirectory.getChildOfType(JsonResource.class, "package.json");
        if (packageJsonResource.exists()) {
            JsonObjectBuilder job = Json.createObjectBuilder();
            job.add("name", ((CreateProjectileContext) context).getArtifactId());
            job.add("version", ((CreateProjectileContext) context).getProjectVersion());
            for (Map.Entry<String, JsonValue> entry : packageJsonResource.getJsonObject().entrySet()) {
                String key = entry.getKey();
                // Do not copy name or version
                if (key.equals("name") || key.equals("version")) {
                    continue;
                }
                job.add(key, entry.getValue());
            }
            packageJsonResource.setContents(job.build());
        }
    }
}

