package org.kontinuity.catapult.service.openshift.impl;

import org.kontinuity.catapult.service.openshift.api.OpenShiftService;

/**
 * A type that contains an
 * {@link org.kontinuity.catapult.service.openshift.spi.OpenShiftServiceSpi}
 *
 * @author <a href="mailto:alr@redhat.com">Andrew Lee Rubinger</a>
 */
public interface OpenShiftServiceContainer {

    /**
     * @return An {@link OpenShiftService}
     */
    OpenShiftService getOpenShiftService();
}
