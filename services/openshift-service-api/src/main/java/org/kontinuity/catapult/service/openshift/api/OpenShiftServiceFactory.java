package org.kontinuity.catapult.service.openshift.api;

import org.kontinuity.catapult.base.identity.Identity;

/**
 * Creates {@link OpenShiftService} instances
 *
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public interface OpenShiftServiceFactory {
    /**
     * Returns an {@link OpenShiftService} given it's OAuth token
     *
     * @param identity an identity
     * @return an {@link OpenShiftService}
     */
    OpenShiftService create(Identity identity);
}
