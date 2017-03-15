package org.kontinuity.catapult.core.api;

import org.kontinuity.catapult.service.github.api.GitHubRepository;
import org.kontinuity.catapult.service.github.api.GitHubWebhook;
import org.kontinuity.catapult.service.openshift.api.OpenShiftProject;

/**
 * Value object containing the result of a {@link Catapult#fling(Projectile)}
 * call.  Implementations should be immutable and therefore thread-safe.
 *
 * @author <a href="mailto:alr@redhat.com">Andrew Lee Rubinger</a>
 */
public interface Boom {

    /**
     * @return the repository we've created for the user
     */
    GitHubRepository getCreatedRepository();

    /**
     * @return the OpenShift project we've created for the user
     */
    OpenShiftProject getCreatedProject();

    /**
     * @return the webhook created on the forked repo on GitHub to trigger
     * builds on OpenShift.
     */
    GitHubWebhook getGitHubWebhook();

}
