package org.kontinuity.catapult.service.github.impl.kohsuke;

import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.Assert;
import org.junit.Test;
import org.kontinuity.catapult.service.github.api.GitHubRepository;
import org.kontinuity.catapult.service.github.api.GitHubService;
import org.kontinuity.catapult.service.github.api.GitHubWebhook;
import org.kontinuity.catapult.service.github.api.GitHubWebhookEvent;
import org.kontinuity.catapult.service.github.api.NoSuchRepositoryException;
import org.kontinuity.catapult.service.github.spi.GitHubServiceSpi;
import org.kontinuity.catapult.service.github.utils.SystemUtils;

/**
 * Unit Tests for the {@link GitHubService}
 *
 * Relies on having environment variables set for: GITHUB_USERNAME GITHUB_TOKEN
 *
 * @author <a href="mailto:alr@redhat.com">Andrew Lee Rubinger</a>
 */
public class GitHubServiceTest {

	private static final Logger log = Logger.getLogger(GitHubServiceTest.class.getName());
	private static final String NAME_GITHUB_SOURCE_REPO = "jboss-developer/jboss-eap-quickstarts";

	protected GitHubService getGitHubService() {
		return new GitHubServiceProducer().create(
				SystemUtils.getPropertyOrEnvVariable(GitHubServiceProducer.GITHUB_TOKEN),
				SystemUtils.getPropertyOrEnvVariable(GitHubServiceProducer.GITHUB_USERNAME));
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
				+ targetRepo.getGitTransportUrl());
	}

	@Test(expected = NoSuchRepositoryException.class)
	public void cannotForkNonexistentRepo() {
		getGitHubService().fork("ALRubinger/someRepoThatDoesNotAndWillNeverExist");
	}

	@Test
	public void createGithubWebHook() throws Exception {
		// given
		final URL webhookUrl = new URL("https://10.1.2.2");
		final GitHubRepository targetRepo = getGitHubService().fork(NAME_GITHUB_SOURCE_REPO);
		// when
		GitHubWebhook webhook = getGitHubService().createWebhook(targetRepo, webhookUrl, GitHubWebhookEvent.ALL);
		// then
		Assert.assertNotNull(webhook);
		Assert.assertEquals(webhookUrl.toString(), webhook.getUrl());
		// After the test removes all webhooks
		((GitHubServiceSpi) getGitHubService()).deleteWebhooks(targetRepo);
	}

}