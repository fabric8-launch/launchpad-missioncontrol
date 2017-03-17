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
}
