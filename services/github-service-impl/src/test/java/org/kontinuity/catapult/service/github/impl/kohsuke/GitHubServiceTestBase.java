package org.kontinuity.catapult.service.github.impl.kohsuke;

import org.junit.Assert;
import org.junit.Test;
import org.kontinuity.catapult.service.github.api.*;
import org.kontinuity.catapult.service.github.spi.GitHubServiceSpi;
import org.kontinuity.catapult.service.github.test.GitHubTestCredentials;

import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Base test class for extension in Unit and Integration modes
 *
 * @author <a href="mailto:alr@redhat.com">Andrew Lee Rubinger</a>
 */
abstract class GitHubServiceTestBase {

	private static final Logger log = Logger.getLogger(GitHubServiceTestBase.class.getName());
    private static final String NAME_GITHUB_SOURCE_REPO = "jboss-developer/jboss-eap-quickstarts";
    private static final String MY_GITHUB_SOURCE_REPO = "my-test-repo";
    private static final String MY_GITHUB_REPO_DESCRIPTION = "Test project created by Arquillian.";

    /**
     * @return The {@link GitHubService} used in testing
     */
    abstract GitHubService getGitHubService();

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
                + targetRepo.getGitTransportUrl());
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
    	((GitHubServiceSpi)getGitHubService()).createRepository(MY_GITHUB_SOURCE_REPO, null);
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
    	((GitHubServiceSpi)getGitHubService()).createRepository(MY_GITHUB_SOURCE_REPO, "");
    }
    
    @Test
    public void createGitHubRepository() throws Exception {
    	// given
    	final GitHubRepository targetRepo = ((GitHubServiceSpi)getGitHubService()).createRepository(MY_GITHUB_SOURCE_REPO, MY_GITHUB_REPO_DESCRIPTION);
    	// then
    	Assert.assertEquals(GitHubTestCredentials.getUsername() + "/" + MY_GITHUB_SOURCE_REPO, targetRepo.getFullName());
    	// After the test remove the repository we created
    	((GitHubServiceSpi)getGitHubService()).deleteRepository(targetRepo);
    }

    @Test
    public void createGithubWebHook() throws Exception {
        // given
        final URL webhookUrl = new URL("https://10.1.2.2");
        final GitHubRepository targetRepo = ((GitHubServiceSpi)getGitHubService()).createRepository(NAME_GITHUB_SOURCE_REPO, MY_GITHUB_REPO_DESCRIPTION);
        // when
        GitHubWebhook webhook = getGitHubService().createWebhook(targetRepo, webhookUrl, GitHubWebhookEvent.ALL);
        // then
        Assert.assertNotNull(webhook);
        Assert.assertEquals(webhookUrl.toString(), webhook.getUrl());
        // After the test remove the webhook we created
        ((GitHubServiceSpi) getGitHubService()).deleteRepository(targetRepo);
    }

}
