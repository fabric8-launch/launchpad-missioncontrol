package org.kontinuity.catapult.service.openshift.api;

import java.util.ServiceLoader;

/**
 * Defines a {@link ServiceLoader} implementation that returns instances of
 * {@link OpenShiftService}.  Implementations must supply a no-arg constructor.
 *
 * @author <a href="mailto:alr@redhat.com">Andrew Lee Rubinger</a>
 */
public interface OpenShiftServiceLoader {

    /**
     * Creates and returns a new instance of {@link OpenShiftService} to interact with
     * the backend service via the required, specified apiUrl
     *
     * @param apiUrl
     * @return
     * @throws IllegalArgumentException If the apiUrl is not specified
     */
    OpenShiftService create(String apiUrl) throws IllegalArgumentException;

}
