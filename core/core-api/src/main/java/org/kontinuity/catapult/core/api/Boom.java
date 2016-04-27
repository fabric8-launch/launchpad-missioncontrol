package org.kontinuity.catapult.core.api;

import org.kontinuity.catapult.service.github.api.GitHubRepository;
import org.kontinuity.catapult.service.openshift.api.OpenShiftProject;

/**
 * Value object containing the result of a {@link CatapultService#fling(Projectile)}
 * call.  Implementations should be immutable and therefore thread-safe.
 *
 * @author <a href="mailto:alr@redhat.com">Andrew Lee Rubinger</a>
 */
public interface Boom {

    /**
     * Returns the repository we've created for the user
     *
     * @return
     */
    GitHubRepository getCreatedRepository();

    /**
     * Returns the OpenShift project we've created for the user
     *
     * @return
     */
    OpenShiftProject getCreatedProject();

}
