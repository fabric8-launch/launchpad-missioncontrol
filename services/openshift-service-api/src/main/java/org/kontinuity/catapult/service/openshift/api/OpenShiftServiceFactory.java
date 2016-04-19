package org.kontinuity.catapult.service.openshift.api;

import java.util.ServiceLoader;

/**
 * Creates {@link OpenShiftService} instances using the {@link ServiceLoader} pattern to decouple
 * the API from the implementing provider.  Requires a {@link ServiceLoader} implementation class with
 * no-arg constructor to be defined in "META-INF/services" on the classpath
 * <p>
 * https://docs.oracle.com/javase/7/docs/api/java/util/ServiceLoader.html
 *
 * @author <a href="mailto:alr@redhat.com">Andrew Lee Rubinger</a>
 */
public enum OpenShiftServiceFactory {

    INSTANCE;

    private static final String MSG_MORE_THAN_ONE_IMPL = "Cannot have more than one implementation of " +
            OpenShiftServiceLoader.class.getName() + " defined as a " +
            ServiceLoader.class.getName() + " implementation";
    private static final String MSG_NEEDS_IMPL = "Must have one implementation of " +
            OpenShiftServiceLoader.class.getName() + " defined as a " +
            ServiceLoader.class.getName() + ".";
    private final ServiceLoader<OpenShiftServiceLoader> serviceLoader = ServiceLoader.load(OpenShiftServiceLoader.class);


    /**
     * Creates a new {@link OpenShiftService} to interact with the backend service via
     * the specified, required apiUrl
     *
     * @param apiUrl
     * @return
     * @throws IllegalArgumentException If the apiUrl is not specified
     */
    public OpenShiftService create(final String apiUrl) throws IllegalArgumentException {

        // Precondition checks
        if (apiUrl == null || apiUrl.isEmpty()) {
            throw new IllegalArgumentException("apiUrl is required");
        }

        // We only want one implementation
        OpenShiftServiceLoader ossl = null;
        for (final OpenShiftServiceLoader candidate : serviceLoader) {
            if (ossl != null) {
                throw new IllegalStateException(MSG_MORE_THAN_ONE_IMPL);
            }
            ossl = candidate;
        }
        if (ossl == null) {
            throw new IllegalStateException(MSG_NEEDS_IMPL);
        }

        // Create
        final OpenShiftService openShiftService = ossl.create(apiUrl);
        return openShiftService;
    }

}
