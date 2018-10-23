package io.fabric8.launcher.tracking;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.logging.Logger;

import javax.annotation.Nullable;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.segment.analytics.Analytics;
import com.segment.analytics.messages.MessageBuilder;
import com.segment.analytics.messages.TrackMessage;
import io.fabric8.launcher.base.identity.TokenIdentity;
import io.fabric8.launcher.core.api.Projectile;
import io.fabric8.launcher.core.api.projectiles.CreateProjectile;

import static io.fabric8.launcher.core.impl.CoreEnvironment.LAUNCHER_TRACKER_SEGMENT_TOKEN;

/**
 * Class that posts {@link Projectile} launch information to a Segment service
 *
 * @author <a href="mailto:alr@redhat.com">Andrew Lee Rubinger</a>
 * @author <a href="mailto:tschotan@redhat.com">Tako Schotanus</a>
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@ApplicationScoped
public class SegmentAnalyticsProvider {

    private static final Logger log = Logger.getLogger(SegmentAnalyticsProvider.class.getName());

    private static final String NAME_EVENT_LAUNCH = "launch";

    private static final String KEY_OPENSHIFT_PROJECT_NAME = "openshiftProjectName";

    private static final String KEY_GITHUB_REPO = "githubRepo";

    private static final String KEY_MISSION = "mission";

    private static final String KEY_RUNTIME = "runtime";

    private static final String LOCAL_USER_ID_PREFIX = "LOCAL_USER_";

    @Nullable
    private final Analytics analytics;

    @Inject
    public SegmentAnalyticsProvider(ExecutorService async) {
        final String token = LAUNCHER_TRACKER_SEGMENT_TOKEN.value();
        if (token != null && !token.isEmpty()) {
            analytics = Analytics.builder(token).networkExecutor(async).build();
            log.finest(() -> "Using Segment analytics with token: " + token);
        } else {
            analytics = null;
        }
    }

    /**
     * no-args constructor used by CDI for proxying only
     * but is subsequently replaced with an instance
     * created using the above constructor.
     *
     * @deprecated do not use this constructor
     */
    @Deprecated
    protected SegmentAnalyticsProvider() {
        this.analytics = null;
    }

    public void trackingMessage(CreateProjectile projectile, @Nullable TokenIdentity tokenIdentity) {
        if (analytics != null) {
            // Create properties
            final Map<String, String> props = new HashMap<>();
            props.put(KEY_GITHUB_REPO, projectile.getGitRepositoryName());
            props.put(KEY_OPENSHIFT_PROJECT_NAME, projectile.getOpenShiftProjectName());
            if (projectile.getBooster() != null) {
                props.put(KEY_MISSION, projectile.getBooster().getMission().getId());
                props.put(KEY_RUNTIME, projectile.getBooster().getRuntime().getId());
            }
            // Create message
            final MessageBuilder message = TrackMessage.builder(NAME_EVENT_LAUNCH).
                    messageId(projectile.getId().toString()).
                    userId(getUserId(tokenIdentity)).
                    properties(props);

            // Send to analytics engine
            analytics.enqueue(message);
        }
    }

    private String getUserId(@Nullable TokenIdentity identity) {
        String userId;
        // User ID will be the token
        if (identity != null) {
            userId = identity.getToken();
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
