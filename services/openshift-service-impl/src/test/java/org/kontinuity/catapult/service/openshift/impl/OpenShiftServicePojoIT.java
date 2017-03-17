package org.kontinuity.catapult.service.openshift.impl;

import org.kontinuity.catapult.service.openshift.api.OpenShiftService;
import org.kontinuity.catapult.service.openshift.impl.fabric8.openshift.client.Fabric8OpenShiftServiceFactory;
import org.kontinuity.catapult.service.openshift.test.OpenShiftTestCredentials;

/**
 * @author <a href="mailto:alr@redhat.com">Andrew Lee Rubinger</a>
 * @author <a href="mailto:rmartine@redhat.com">Ricardo Martinelli de
 *         Oliveira</a>
 * @author <a href="mailto:xcoulon@redhat.com">Xavier Coulon</a>
 */
public class OpenShiftServicePojoIT extends OpenShiftServiceTestBase {

    @Override
    public OpenShiftService getOpenShiftService() {
        return new Fabric8OpenShiftServiceFactory().create(OpenShiftTestCredentials.getToken());
    }
}
