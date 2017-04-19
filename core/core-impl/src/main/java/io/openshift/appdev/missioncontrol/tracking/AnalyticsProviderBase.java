package io.openshift.appdev.missioncontrol.tracking;

import java.io.IOException;
import java.util.UUID;

import javax.annotation.Resource;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.enterprise.event.Observes;

import io.openshift.appdev.missioncontrol.core.api.LaunchEvent;

/**
 * Base class for handling {@link LaunchEvent}
 *
 * @author <a href="mailto:alr@redhat.com">Andrew Lee Rubinger</a>
 * @author <a href="mailto:tschotan@redhat.com">Tako Schotanus</a>
 */
public abstract class AnalyticsProviderBase {

    @Resource
    protected ManagedExecutorService async;

    public void onEvent(@Observes LaunchEvent launch) throws IOException {
        runPostTrackingMessage(
                launch.getUser(),
                launch.getId(),
                launch.getGithubRepo(),
                launch.getOpenshiftProjectName());
	}

    /*
     * This is a hook for any base class that wants to provide
     * their own threading/executor mechanism if the analytics
     * module itself doesn't support it. The default implementation
     * just calls {@link postTrackingMessage} leaving the
     * threading to the analytics module itself
     */
    protected void runPostTrackingMessage(final String userId,
                                       final UUID projectileId,
                                       final String githubRepo,
                                       final String openshiftProjectName) {
        postTrackingMessage(
                userId,
                projectileId,
                githubRepo,
                openshiftProjectName);
    }

    /*
     * The method that will do the actual work of sending the
     * projectile launch information to the aggregator
     */
    protected abstract void postTrackingMessage(final String userId,
                                       final UUID projectileId,
                                       final String githubRepo,
                                       final String openshiftProjectName);
}

