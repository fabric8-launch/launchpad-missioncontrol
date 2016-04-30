/*******************************************************************************
 * Copyright (c) 2016 Red Hat. All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Red Hat - Initial Contribution
 *******************************************************************************/

package org.kontinuity.catapult.service.github;

import java.text.MessageFormat;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

/**
 * CDI Bean producer to allow for injection of configuration settings in the
 * {@link GithubResource}.
 */
@ApplicationScoped
public class GitHubResourceConfig {

	private static Logger log = Logger.getLogger(GitHubResourceConfig.class.getName());

	/**
	 * Name of the environment variable or system property for the GitHub OAuth
	 * Client ID
	 */
	private static String ENV_VAR_SYS_PROP_NAME_GITHUB_CLIENT_ID = "KONTINUITY_CATAPULT_GITHUB_APP_CLIENT_ID";

	/**
	 * Name of the environment variable or system property for the GitHub OAuth
	 * Client Secret
	 */
	private static String ENV_VAR_SYS_PROP_NAME_GITHUB_CLIENT_SECRET = "KONTINUITY_CATAPULT_GITHUB_APP_CLIENT_SECRET";

	private final static String MESSAGE_PATTERN = "Could not find required env var or sys prop {0}";
	
	@Produces
	@CatapultAppId
	public String getCatapultApplicationId() {
		return getEnvVarOrSysProp(ENV_VAR_SYS_PROP_NAME_GITHUB_CLIENT_ID);
	}

	@Produces
	@CatapultAppOAuthSecret
	public String getCatapultApplicationOAuthSecret() {
		return getEnvVarOrSysProp(ENV_VAR_SYS_PROP_NAME_GITHUB_CLIENT_SECRET);
	}
	
	// TODO: move this code to 'catapult-base' ? (it's a duplicate from GitHubCredentials)
	private static String getEnvVarOrSysProp(final String envVarOrSysProp) {
		String value = System.getProperty(envVarOrSysProp);
		if (value == null) {
			value = System.getenv(envVarOrSysProp);
		}
		if (value == null) {
			final String errorMessage = MessageFormat.format(MESSAGE_PATTERN, envVarOrSysProp);
			log.severe(errorMessage);
			throw new IllegalStateException(errorMessage);
		}
		return value;
	}
}
