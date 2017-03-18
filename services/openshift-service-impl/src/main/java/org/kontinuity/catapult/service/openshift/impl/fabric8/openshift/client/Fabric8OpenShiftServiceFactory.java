package org.kontinuity.catapult.service.openshift.impl.fabric8.openshift.client;

import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;

import org.kontinuity.catapult.base.identity.Identity;
import org.kontinuity.catapult.service.openshift.api.OpenShiftService;
import org.kontinuity.catapult.service.openshift.api.OpenShiftServiceFactory;
import org.kontinuity.catapult.service.openshift.api.OpenShiftSettings;

/**
 * {@link OpenShiftServiceFactory} implementation
 *
 * @author <a href="mailto:xcoulon@redhat.com">Xavier Coulon</a>
 */
@ApplicationScoped
public class Fabric8OpenShiftServiceFactory implements OpenShiftServiceFactory {

    private Logger log = Logger.getLogger(Fabric8OpenShiftServiceFactory.class.getName());

    /**
     * Creates a new {@link OpenShiftService} with the specified, required url and oauthToken
     *
     * @param oauthToken the OAuth token
     * @return the created {@link OpenShiftService}
     * @throws IllegalArgumentException If the {@code openshiftUrl} is not specified
     */
    @Override
    public Fabric8OpenShiftServiceImpl create(Identity identity) {
        if (identity == null) {
            throw new IllegalArgumentException("identity is required");
        }

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
        log.finest(() -> "Created backing OpenShift client for " + openShiftApiUrl);
        return new Fabric8OpenShiftServiceImpl(openShiftApiUrl, openshiftConsoleUrl, identity);
    }
}
