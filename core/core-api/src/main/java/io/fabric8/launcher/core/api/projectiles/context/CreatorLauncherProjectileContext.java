package io.fabric8.launcher.core.api.projectiles.context;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.fabric8.launcher.core.api.ProjectileContext;

public interface CreatorLauncherProjectileContext extends ProjectileContext, ProjectNameCapable, GitCapable {
    ObjectNode getProject();
}