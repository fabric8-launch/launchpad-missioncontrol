package org.kontinuity.catapult.service.github.impl.kohsuke;

import org.kohsuke.github.GHEvent;
import org.kontinuity.catapult.service.github.api.GitHubWebhookEvent;
import org.kontinuity.catapult.service.github.api.NoSuchWebhookEventException;

public class KohsukeGitHubWebhookEvent implements GitHubWebhookEvent {
	
	private GHEvent delegate;
	
	public KohsukeGitHubWebhookEvent(String name) throws NoSuchWebhookEventException {
		if(GHEvent.valueOf(name) == null) throw new NoSuchWebhookEventException(name);
		this.delegate = GHEvent.valueOf(name);
	}
	
	@Override
	public String getName() {
		return delegate.name();
	}

}
