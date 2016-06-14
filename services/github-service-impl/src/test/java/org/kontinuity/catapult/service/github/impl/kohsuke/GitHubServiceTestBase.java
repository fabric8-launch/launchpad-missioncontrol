package org.kontinuity.catapult.service.github.impl.kohsuke;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.kontinuity.catapult.service.github.api.GitHubRepository;
import org.kontinuity.catapult.service.github.api.GitHubService;
import org.kontinuity.catapult.service.github.api.GitHubWebhook;
import org.kontinuity.catapult.service.github.api.GitHubWebhookEvent;
import org.kontinuity.catapult.service.github.api.NoSuchRepositoryException;
import org.kontinuity.catapult.service.github.spi.GitHubServiceSpi;
import org.kontinuity.catapult.service.github.test.GitHubTestCredentials;

/**
 * Base test class for extension in Unit and Integration modes
 *
 * @author <a href="mailto:alr@redhat.com">Andrew Lee Rubinger</a>
 */
abstract class GitHubServiceTestBase {

	private static final Logger log = Logger.getLogger(GitHubServiceTestBase.class.getName());
    private static final String NAME_GITHUB_SOURCE_REPO = "jboss-developer/jboss-eap-quickstarts";
    private static final String MY_GITHUB_SOURCE_REPO_PREFIX = "my-test-repo-";
    private static final String MY_GITHUB_REPO_DESCRIPTION = "Test project created by Arquillian.";

    /**
     * @return The {@link GitHubService} used in testing
     */
    abstract GitHubService getGitHubService();

    private final List<String> repositoryNames = new ArrayList<>();
	
    private String generateRepositoryName() {
		final String repoName = this.MY_GITHUB_SOURCE_REPO_PREFIX + UUID.randomUUID().toString();
		this.repositoryNames.add(repoName);
		return repoName;
	}

	@Before
	public void before() {
		this.repositoryNames.clear();
	}

	@After
	public void after() {
		repositoryNames.stream().map(repo -> GitHubTestCredentials.getUsername() + '/' + repo)
		        .filter(repo -> ((GitHubServiceSpi) getGitHubService()).repositoryExists(repo))
		        .forEach(repo -> ((GitHubServiceSpi) getGitHubService()).deleteRepository(repo));
	}
	
    @Test(expected = IllegalArgumentException.class)
    public void forkRepoCannotBeNull() {
        getGitHubService().fork(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void forkRepoCannotBeEmpty() {
        getGitHubService().fork("");
    }

    @Test
    public void fork() {
        // when
        final GitHubRepository targetRepo = getGitHubService().fork(NAME_GITHUB_SOURCE_REPO);
        // then
        Assert.assertNotNull("Got null result in forking " + NAME_GITHUB_SOURCE_REPO, targetRepo);
        log.log(Level.INFO, "Forked " + NAME_GITHUB_SOURCE_REPO + " as " + targetRepo.getFullName() + " available at "
                + targetRepo.getHomepage());
    }

    @Test(expected = NoSuchRepositoryException.class)
    public void cannotForkNonexistentRepo() {
        getGitHubService().fork("ALRubinger/someRepoThatDoesNotAndWillNeverExist");
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void createGitHubRepositoryCannotBeNull() throws Exception {
    	((GitHubServiceSpi)getGitHubService()).createRepository(null, null);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void createGitHubRepositoryNameCannotBeNull() throws Exception {
    	((GitHubServiceSpi)getGitHubService()).createRepository(null, MY_GITHUB_REPO_DESCRIPTION);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void createGitHubRepositoryDescriptionCannotBeNull() throws Exception {
    	((GitHubServiceSpi)getGitHubService()).createRepository(MY_GITHUB_SOURCE_REPO_PREFIX, null);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void createGitHubRepositoryCannotBeEmpty() throws Exception {
    	((GitHubServiceSpi)getGitHubService()).createRepository("", "");
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void createGitHubRepositoryNameCannotBeEmpty() throws Exception {
    	((GitHubServiceSpi)getGitHubService()).createRepository("", MY_GITHUB_REPO_DESCRIPTION);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void createGitHubRepositoryCannotDescriptionBeEmpty() throws Exception {
    	((GitHubServiceSpi)getGitHubService()).createRepository(MY_GITHUB_SOURCE_REPO_PREFIX, "");
    }
    
    @Test
    public void createGitHubRepository() throws Exception {
    	// given
    	final String repositoryName = generateRepositoryName();
    	// when
    	final GitHubRepository targetRepo = ((GitHubServiceSpi)getGitHubService()).createRepository(repositoryName, MY_GITHUB_REPO_DESCRIPTION);
    	// then
    	Assert.assertEquals(GitHubTestCredentials.getUsername() + "/" + repositoryName, targetRepo.getFullName());
    }

    @Test
    public void createGithubWebHook() throws Exception {
    	// given
    	final String repositoryName = generateRepositoryName();
        final URL webhookUrl = new URL("https://10.1.2.2");
        final GitHubRepository targetRepo = ((GitHubServiceSpi)getGitHubService()).createRepository(repositoryName, MY_GITHUB_REPO_DESCRIPTION);
        // when
        final GitHubWebhook webhook = getGitHubService().createWebhook(targetRepo, webhookUrl, GitHubWebhookEvent.ALL);
        // then
        Assert.assertNotNull(webhook);
        Assert.assertEquals(webhookUrl.toString(), webhook.getUrl());
    }

}
