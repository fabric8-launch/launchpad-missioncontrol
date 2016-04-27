package org.kontinuity.catapult.service.openshift.impl;

import org.kontinuity.catapult.service.openshift.api.OpenShiftService;
import org.kontinuity.catapult.service.openshift.api.OpenShiftSettings;
import org.kontinuity.catapult.service.openshift.impl.fabric8.openshift.client.OpenShiftServiceProducer;

/**
 * @author <a href="mailto:alr@redhat.com">Andrew Lee Rubinger</a>
 * @author <a href="mailto:rmartine@redhat.com">Ricardo Martinelli de
 *         Oliveira</a>
 * @author <a href="mailto:xcoulon@redhat.com">Xavier Coulon</a>
 */
public class OpenShiftServiceTest extends OpenShiftServiceTestBase {

	private OpenShiftService openshiftService = new OpenShiftServiceProducer()
			.create(OpenShiftSettings.getOpenShiftUrl());

	@Override
	protected OpenShiftService getOpenShiftService() {
		return this.openshiftService;
	}
}
