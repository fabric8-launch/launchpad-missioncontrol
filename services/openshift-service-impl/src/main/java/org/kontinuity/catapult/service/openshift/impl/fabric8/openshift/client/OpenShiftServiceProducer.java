package org.kontinuity.catapult.service.openshift.impl.fabric8.openshift.client;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

import org.kontinuity.catapult.service.openshift.api.OpenShiftService;
import org.kontinuity.catapult.service.openshift.api.OpenShiftSettings;

/**
 * CDI producer for the {@link OpenShiftService}.
 *
 * @author <a href="mailto:xcoulon@redhat.com">Xavier Coulon</a>
 */
@ApplicationScoped
public class OpenShiftServiceProducer {

    private Logger log = Logger.getLogger(OpenShiftServiceProducer.class.getName());

    /**
     * Creates a new {@link OpenShiftService} with the specified, required url
     *
     * @return the created {@link OpenShiftService}
     * @throws IllegalArgumentException If the {@code openshiftUrl} is not specified
     */
    @Produces
    public OpenShiftService create() {
        final String openShiftApiUrl = OpenShiftSettings.getOpenShiftApiUrl();
        final String openshiftConsoleUrl = OpenShiftSettings.getOpenShiftConsoleUrl();

        // Precondition checks
        if (openShiftApiUrl == null) {
            throw new IllegalArgumentException("openshiftUrl is required");
        }
        if (openshiftConsoleUrl == null) {
            throw new IllegalArgumentException("openshiftConsoleUrl is required");
        }

        // Create and return
        if (log.isLoggable(Level.FINEST)) {
            log.log(Level.FINEST, "Created backing OpenShift client for " + openShiftApiUrl);
        }
        return new Fabric8OpenShiftClientServiceImpl(openShiftApiUrl, openshiftConsoleUrl);
    }

}
