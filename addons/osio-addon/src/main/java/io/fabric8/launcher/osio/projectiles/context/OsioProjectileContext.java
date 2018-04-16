package io.fabric8.launcher.osio.projectiles.context;

import javax.validation.constraints.NotNull;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;

import io.fabric8.launcher.booster.catalog.rhoar.Mission;
import io.fabric8.launcher.booster.catalog.rhoar.Runtime;
import io.fabric8.launcher.booster.catalog.rhoar.Version;
import io.fabric8.launcher.core.api.projectiles.context.LauncherProjectileContext;
import org.apache.maven.model.Dependency;

import java.util.List;
/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public class OsioProjectileContext extends OsioImportProjectileContext implements LauncherProjectileContext {

    @FormParam("mission")
    @NotNull(message = "mission is required")
    private Mission mission;

    @FormParam("runtime")
    @NotNull(message = "runtime is required")
    private Runtime runtime;

    @FormParam("runtimeVersion")
    private Version runtimeVersion;

    @FormParam("groupId")
    @DefaultValue("io.openshift.booster")
    private String groupId;

    @FormParam("artifactId")
    @NotNull(message = "artifactId is required")
    private String artifactId;

    @FormParam("projectVersion")
    @DefaultValue("1.0.0")
    private String projectVersion;

    @FormParam("dependency")
    private List<Dependency> dependencies;

    @Override
    public Mission getMission() {
        return mission;
    }

    @Override
    public Runtime getRuntime() {
        return runtime;
    }

    @Override
    public Version getRuntimeVersion() {
        return runtimeVersion;
    }

    @Override
    public String getGroupId() {
        return groupId;
    }

    @Override
    public String getArtifactId() {
        return artifactId;
    }

    @Override
    public String getProjectVersion() {
        return projectVersion;
    }

    public List<Dependency> getDependencies() {
        return dependencies;
    }

}
