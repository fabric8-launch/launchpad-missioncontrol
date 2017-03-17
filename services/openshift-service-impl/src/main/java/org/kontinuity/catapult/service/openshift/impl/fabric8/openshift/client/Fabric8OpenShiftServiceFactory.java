package org.kontinuity.catapult.service.openshift.impl.fabric8.openshift.client;

import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;

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
    public Fabric8OpenShiftServiceImpl create(String oauthToken) {
        if (oauthToken == null) {
            throw new IllegalArgumentException("oauthToken is required");
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
        return new Fabric8OpenShiftServiceImpl(openShiftApiUrl, openshiftConsoleUrl, oauthToken);
    }

    /**
     * Returns an {@link OpenShiftService} given a username and password
     *
     * @param user     a valid Openshift user
     * @param password a valid Openshift password
     * @return an {@link OpenShiftService}
     */
    @Override
    public Fabric8OpenShiftServiceImpl create(String user, String password) {
        if (user == null) {
            throw new IllegalArgumentException("user is required");
        }
        if (password == null) {
            throw new IllegalArgumentException("password is required");
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
        return new Fabric8OpenShiftServiceImpl(openShiftApiUrl, openshiftConsoleUrl, user, password);
    }
}
