package org.kontinuity.catapult.service.github.impl.kohsuke;

import org.kohsuke.github.GHHook;
import org.kontinuity.catapult.service.github.api.GitHubWebhook;
import org.kontinuity.catapult.service.github.api.GitHubWebhookEvent;

public class KohsukeGitHubWebhook implements GitHubWebhook {
	
	private GHHook delegate;
	
	public KohsukeGitHubWebhook(GHHook delegate) {
		this.delegate = delegate;
	}

	@Override
	public String getName() {
		return delegate.getName();
	}

	@Override
	public String getUrl() {
		return delegate.getUrl().toString();
	}

	@Override
	public GitHubWebhookEvent[] getEvents() {
		return null; //TODO fix this return
	}
	
}
