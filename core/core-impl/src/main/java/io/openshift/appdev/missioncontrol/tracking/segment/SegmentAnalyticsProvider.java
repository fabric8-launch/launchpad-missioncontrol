package io.openshift.appdev.missioncontrol.tracking.segment;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
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

    private static final Logger log = Logger.getLogger(SegmentAnalyticsProvider.class.getName());

    private static final String NAME_EVENT_LAUNCH = "launch";
    private static final String KEY_OPENSHIFT_PROJECT_NAME = "openshiftProjectName";
    private static final String KEY_GITHUB_REPO = "githubRepo";

    private static final String LAUNCHPAD_TRACKER_SEGMENT_TOKEN = "LAUNCHPAD_TRACKER_SEGMENT_TOKEN";
    private static final String LAUNCHPAD_TRACKER_SEGMENT_TOKEN_DEFAULT = "oOlNiAf3K5MDwpd4ErD2ZPRe6z3Ckk7w";

    private Analytics analytics;

    @PostConstruct
    private void initAnalytics() {
        final String token = EnvironmentSupport.INSTANCE.getEnvVarOrSysProp(
                LAUNCHPAD_TRACKER_SEGMENT_TOKEN,
                LAUNCHPAD_TRACKER_SEGMENT_TOKEN_DEFAULT);
        analytics = Analytics.builder(token).networkExecutor(async).build();
        log.finest(() -> "Using Segment analytics with token: " + token);
	}

    @Override
    protected void postTrackingMessage(final String userId,
                                       final UUID projectileId,
                                       final String githubRepo,
                                       final String openshiftProjectName) {
        // Create properties
        final Map<String, String> props = new HashMap<>();
        props.put(KEY_GITHUB_REPO, githubRepo);
        props.put(KEY_OPENSHIFT_PROJECT_NAME, openshiftProjectName);

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
                "openshiftProjectName: " + openshiftProjectName);
    }

    @Produces
    private Analytics getAnalytics() {
        return analytics;
    }
}

