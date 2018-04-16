package io.fabric8.launcher.web.endpoints.inputs;

import javax.validation.constraints.NotNull;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.QueryParam;

import io.fabric8.launcher.booster.catalog.rhoar.Mission;
import io.fabric8.launcher.booster.catalog.rhoar.Runtime;
import io.fabric8.launcher.booster.catalog.rhoar.Version;
import io.fabric8.launcher.core.api.projectiles.context.ZipProjectileContext;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public class ZipProjectileInput implements ZipProjectileContext {

    @QueryParam("mission")
    @NotNull(message = "Mission is required")
    private Mission mission;

    @QueryParam("runtime")
    @NotNull(message = "Runtime is required")
    private Runtime runtime;

    @QueryParam("runtimeVersion")
    private Version runtimeVersion;

    @QueryParam("groupId")
    @DefaultValue("io.openshift.booster")
    private String groupId;

    @QueryParam("artifactId")
    private String artifactId;

    @QueryParam("projectVersion")
    @DefaultValue("1.0.0")
    private String projectVersion;

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
}
