/**
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
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
	 * @param openshiftUrl
	 *            the URL to the OpenShift instance
	 * @return the created {@link OpenShiftService}
	 * @throws IllegalArgumentException
	 *             If the {@code openshiftUrl} is not specified
	 */
	@Produces
	public OpenShiftService create(@OpenShiftUrl final String openshiftUrl) {

		// Precondition checks
		if (openshiftUrl == null) {
			throw new IllegalArgumentException("openshiftUrl is required");
		}

		// Create and return
		if (log.isLoggable(Level.FINEST)) {
            log.log(Level.FINEST, "Created backing OpenShift client for " + openshiftUrl);
        }
		return new Fabric8OpenShiftClientServiceImpl(openshiftUrl);
	}

	/**
	 * @return the OpenShift URL from the system properties or environment
	 *         variables.
	 */
	@Produces
	@OpenShiftUrl
	public String getOpenShiftUrl() {
		return OpenShiftSettings.getOpenShiftUrl();
	}

}
