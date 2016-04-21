/*******************************************************************************
 * Copyright (c) 2016 Red Hat.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/

package org.kontinuity.catapult.service.github.utils;

import java.text.MessageFormat;
import java.util.logging.Logger;

/**
 * Utility class to retrieve configuration values from the environment variables
 * or system properties.
 */
public class SystemUtils {

	private static Logger log = Logger.getLogger(SystemUtils.class.getName());

	private static final String ERROR_MESSAGE_TEMPLATE = "Failed to bind {0} "
			+ "to an environment variable or a system property.";

	/**
	 * Private constructor of the utility class.
	 */
	private SystemUtils() {
		// does nothing.
	}

	/**
	 * Looks-up the configuration element in the environment variables and in
	 * the system properties.
	 * 
	 * @param key
	 *            the config key
	 * @return the value, if found
	 * @throws IllegalArgumentException
	 *             if the value was not found
	 */
	public static String getPropertyOrEnvVariable(final String key) throws IllegalArgumentException {
		String systemPropertyValue = System.getProperty(key);
		if (systemPropertyValue == null) {
			systemPropertyValue = System.getenv(key);
		}
		if (systemPropertyValue == null) {
			final String errorMessage = MessageFormat.format(ERROR_MESSAGE_TEMPLATE, key);
			log.severe(errorMessage);
			throw new IllegalStateException(errorMessage);
		}
		return systemPropertyValue;
	}
}
