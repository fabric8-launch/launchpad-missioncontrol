package org.kontinuity.catapult.service.github.impl.kohsuke;


import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kontinuity.catapult.service.github.api.*;
import org.kontinuity.catapult.service.github.spi.GitHubServiceSpi;

import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Tests for the {@link GitHubService}
 *
 * Relies on having environment variables set for:
 * GITHUB_USERNAME
 * GITHUB_TOKEN
 *
 * @author <a href="mailto:alr@redhat.com">Andrew Lee Rubinger</a>
 */
public class GitHubServiceIT {

    private static final Logger log = Logger.getLogger(GitHubServiceIT.class.getName());
    private static final String NAME_GITHUB_SOURCE_REPO = "jboss-developer/jboss-eap-quickstarts";

    private static String GITHUB_USERNAME;
    private static String GITHUB_PERSONAL_ACCESS_TOKEN;

    private GitHubService gitHubService;

    @BeforeClass
    public static void initGithubCredentials() throws IOException {
        GITHUB_USERNAME = GitHubTestingCredentials.getUsername();
        GITHUB_PERSONAL_ACCESS_TOKEN = GitHubTestingCredentials.getToken();
    }

    @Before
    public void initGithubService() {
        gitHubService = GitHubServiceFactory.INSTANCE.create(GITHUB_PERSONAL_ACCESS_TOKEN, GITHUB_USERNAME);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void forkRepoCannotBeNull() {
        final GitHubRepository targetRepo = gitHubService.fork(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void forkRepoCannotBeEmpty() {
        final GitHubRepository targetRepo = gitHubService.fork("");
    }

    @Test
    public void fork() {
        final GitHubRepository targetRepo = gitHubService.fork(NAME_GITHUB_SOURCE_REPO);
        Assert.assertNotNull("Got null result in forking " + NAME_GITHUB_SOURCE_REPO, targetRepo);
        log.log(Level.INFO, "Forked " + NAME_GITHUB_SOURCE_REPO + " as " + targetRepo.getFullName() +
                " available at " + targetRepo.getGitTransportUrl());
    }

    @Test(expected = NoSuchRepositoryException.class)
    public void cannotForkNonexistentRepo(){
        gitHubService.fork("ALRubinger/someRepoThatDoesNotAndWillNeverExist");
    }

    @Test
    public void createGithubWebHook() throws Exception{
    	final URL webhookUrl = new URL("https://10.1.2.2");
    	
    	final GitHubRepository targetRepo = gitHubService.fork(NAME_GITHUB_SOURCE_REPO);
		GitHubWebhook webhook = gitHubService.createWebhook(
    			targetRepo,
    			webhookUrl,
    			GitHubWebhookEvent.ALL);
    	
    	Assert.assertNotNull(webhook);
    	Assert.assertEquals(webhookUrl.toString(), webhook.getUrl());
    	// After the test removes all webhooks
    	((GitHubServiceSpi)gitHubService).deleteWebhook(targetRepo, webhook);    	
    }

}