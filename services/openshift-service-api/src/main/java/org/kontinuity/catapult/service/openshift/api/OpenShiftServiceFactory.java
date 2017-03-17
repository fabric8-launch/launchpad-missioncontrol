package org.kontinuity.catapult.service.openshift.api;

/**
 * Creates {@link OpenShiftService} instances
 *
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public interface OpenShiftServiceFactory {
    /**
     * Returns an {@link OpenShiftService} given it's OAuth token
     *
     * @param oauthToken token from SSO server (OAuth)
     * @return an {@link OpenShiftService}
     */
    OpenShiftService create(String oauthToken);

    /**
     * Returns an {@link OpenShiftService} given a username and password
     *
     * @param user     a valid Openshift user
     * @param password a valid Openshift password
     * @return an {@link OpenShiftService}
     */
    OpenShiftService create(String user, String password);
}
