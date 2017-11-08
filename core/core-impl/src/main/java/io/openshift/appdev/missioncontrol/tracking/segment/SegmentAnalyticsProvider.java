package io.openshift.appdev.missioncontrol.tracking.segment;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

import com.segment.analytics.Analytics;
import com.segment.analytics.messages.MessageBuilder;
import com.segment.analytics.messages.TrackMessage;

import io.openshift.appdev.missioncontrol.base.EnvironmentSupport;
import io.openshift.appdev.missioncontrol.tracking.AnalyticsProviderBase;

/**
 * Class that posts {@link Projectile} launch information to a Segment service
 *
 * @author <a href="mailto:alr@redhat.com">Andrew Lee Rubinger</a>
 * @author <a href="mailto:tschotan@redhat.com">Tako Schotanus</a>
 */
@ApplicationScoped
public class SegmentAnalyticsProvider extends AnalyticsProviderBase {

    @Resource
    ManagedExecutorService async;

    private static final Logger log = Logger.getLogger(SegmentAnalyticsProvider.class.getName());

    private static final String NAME_EVENT_LAUNCH = "launch";
    private static final String KEY_OPENSHIFT_PROJECT_NAME = "openshiftProjectName";
    private static final String KEY_GITHUB_REPO = "githubRepo";
    private static final String KEY_MISSION = "mission";
    private static final String KEY_RUNTIME = "runtime";

    private static final String LAUNCHPAD_TRACKER_SEGMENT_TOKEN = "LAUNCHPAD_TRACKER_SEGMENT_TOKEN";

    private Analytics analytics;

    @PostConstruct
    private void initAnalytics() {
        final String token = EnvironmentSupport.INSTANCE.getEnvVarOrSysProp(
                LAUNCHPAD_TRACKER_SEGMENT_TOKEN);
        if (token != null && !token.isEmpty()) {
            analytics = Analytics.builder(token).networkExecutor(async).build();
            log.finest(() -> "Using Segment analytics with token: " + token);
        }
	}

    @Override
    protected void postTrackingMessage(final String userId,
                                       final UUID projectileId,
                                       final String githubRepo,
                                       final String openshiftProjectName,
                                       final String mission,
                                       final String runtime) {
        if (analytics != null) {
            // Create properties
            final Map<String, String> props = new HashMap<>();
            props.put(KEY_GITHUB_REPO, githubRepo);
            props.put(KEY_OPENSHIFT_PROJECT_NAME, openshiftProjectName);
            props.put(KEY_MISSION, mission);
            props.put(KEY_RUNTIME, runtime);
    
            // Create message
            final MessageBuilder message = TrackMessage.builder(NAME_EVENT_LAUNCH).
                    messageId(projectileId).
                    userId(userId).
                    properties(props);
    
            // Send to analytics engine
            analytics.enqueue(message);
    
            log.finest(() -> "Queued tracking message for: " +
                    "userId: " + userId + ", " +
                    "projectileId: " + projectileId + ", " +
                    "githubRepo: " + githubRepo + ", " +
                    "openshiftProjectName: " + openshiftProjectName + ", " +
                    "mission: " + mission + ", " +
                    "runtime: " + runtime);
        }
    }

    @Produces
    private Analytics getAnalytics() {
        return analytics;
    }
}
