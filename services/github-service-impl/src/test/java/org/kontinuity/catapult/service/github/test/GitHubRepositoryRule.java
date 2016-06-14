package org.kontinuity.catapult.service.github.test;

import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.junit.rules.ExternalResource;
import org.kontinuity.catapult.service.github.api.GitHubRepository;
import org.kontinuity.catapult.service.github.spi.GitHubServiceSpi;

/**
 * Utility class for {@link GitHubRepository} 
 */
public class GitHubRepositoryRule extends ExternalResource {

	
	/** the {@link GitHubServiceSpi} to use to submit the delete request. */
	private final GitHubServiceSpi gitHubService;
	
	private final String repositoryNamePrefix;
	
	private final List<String> repositoryNames = new ArrayList<>();
	
	/**
	 * Constructor
	 * @param gitHubService
	 */
	public GitHubRepositoryRule(final GitHubServiceSpi gitHubService, final String repositoryNamePrefix) {
		this.gitHubService = gitHubService;
		this.repositoryNamePrefix = repositoryNamePrefix;
	}
	
	

	
}
