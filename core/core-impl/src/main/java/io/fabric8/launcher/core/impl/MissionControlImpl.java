package io.fabric8.launcher.core.impl;

import java.net.InetAddress;
import java.net.NetworkInterface;

import javax.enterprise.event.Event;
import javax.inject.Inject;

import io.fabric8.launcher.base.identity.Identity;
import io.fabric8.launcher.base.identity.TokenIdentity;
import io.fabric8.launcher.core.api.Boom;
import io.fabric8.launcher.core.api.CreateProjectile;
import io.fabric8.launcher.core.api.LaunchEvent;
import io.fabric8.launcher.core.api.MissionControl;
import io.fabric8.launcher.core.api.Projectile;
import io.fabric8.launcher.core.api.StatusEventType;
import io.fabric8.launcher.core.api.inject.Step;
import io.fabric8.launcher.core.impl.events.CreateProjectileEvent;
import io.fabric8.launcher.service.git.api.GitRepository;
import io.fabric8.launcher.service.git.api.GitService;
import io.fabric8.launcher.service.openshift.api.OpenShiftProject;
import io.fabric8.launcher.service.openshift.api.OpenShiftService;

/**
 * Implementation of the {@link MissionControl} interface.
 *
 * @author <a href="mailto:alr@redhat.com">Andrew Lee Rubinger</a>
 */
public class MissionControlImpl implements MissionControl {

    private static final String LOCAL_USER_ID_PREFIX = "LOCAL_USER_";

    @Inject
    private Event<CreateProjectileEvent> projectileEvent;

    @Inject
    private Event<LaunchEvent> launchEvent;

    @Inject
    private GitService gitService;

    @Inject
    private OpenShiftService openShiftService;

    @Override
    public Boom launch(CreateProjectile projectile) throws IllegalArgumentException {
        int startIndex = projectile.getStartOfStep();
        assert startIndex >= 0 : "startOfStep cannot be negative. Was " + startIndex;
        StatusEventType[] statusEventTypes = StatusEventType.values();

        CreateProjectileEvent event = new CreateProjectileEvent(projectile);
        // TODO: Move this to somewhere else?
        if (startIndex > 0) {
            // Restore event state
            if (startIndex > StatusEventType.GITHUB_CREATE.ordinal()) {
                // Github repository should have already been created.
                GitRepository repository = gitService.getRepository(projectile.getGitHubRepositoryName())
                        .orElseThrow(() -> new IllegalStateException("Git project " + projectile.getGitHubRepositoryName() + " cannot be found"));
                event.setGitRepository(repository);
            }
            if (startIndex > StatusEventType.OPENSHIFT_CREATE.ordinal()) {
                // OpenShift project should have already been created
                OpenShiftProject openShiftProject = openShiftService.findProject(projectile.getOpenShiftProjectName())
                        .orElseThrow(() -> new IllegalStateException("Openshift project " + projectile.getOpenShiftProjectName() + " cannot be found"));
                event.setOpenShiftProject(openShiftProject);
            }
        }
        for (int i = startIndex; i < statusEventTypes.length; i++) {
            this.projectileEvent.select(new Step.Literal(statusEventTypes[i])).fire(event);
        }
        launchEvent.fire(new LaunchEvent(getUserId(projectile), projectile.getId(), projectile.getGitHubRepositoryName(),
                                         projectile.getOpenShiftProjectName(), projectile.getMission(), projectile.getRuntime()));
        return new BoomImpl(event.getGitRepository(), event.getOpenShiftProject(), event.getWebhooks());
    }

    private String getUserId(Projectile projectile) {
        final Identity identity = projectile.getOpenShiftIdentity();
        String userId;
        // User ID will be the token
        if (identity instanceof TokenIdentity) {
            userId = ((TokenIdentity) identity).getToken();
        } else {
            // For users authenticating with user/password (ie. local/Minishift/CDK)
            // let's identify them by their MAC address (which in a VM is the MAC address
            // of the VM, or a fake one, but all we can really rely on to uniquely identify
            // an installation
            final StringBuilder sb = new StringBuilder();
            try {
                byte[] macAddress = NetworkInterface.getByInetAddress(InetAddress.getLocalHost()).getHardwareAddress();
                sb.append(LOCAL_USER_ID_PREFIX);
                for (int i = 0; i < macAddress.length; i++) {
                    sb.append(String.format("%02X%s", macAddress[i], (i < macAddress.length - 1) ? "-" : ""));
                }
                userId = sb.toString();
            } catch (Exception e) {
                userId = LOCAL_USER_ID_PREFIX + "UNKNOWN";
            }
        }
        return userId;
    }
}
