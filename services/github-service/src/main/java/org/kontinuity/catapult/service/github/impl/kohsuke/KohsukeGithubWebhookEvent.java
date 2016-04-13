package org.kontinuity.catapult.service.github.impl.kohsuke;

import org.kohsuke.github.GHEvent;
import org.kontinuity.catapult.service.github.api.GithubWebhookEvent;
import org.kontinuity.catapult.service.github.api.NoSuchEventException;

public class KohsukeGithubWebhookEvent implements GithubWebhookEvent {
	
	private String name;
	
	public KohsukeGithubWebhookEvent(String name) throws NoSuchEventException {
		if(GHEvent.valueOf(name) == null) throw new NoSuchEventException();
		this.name = name;
	}
	
	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public GHEvent toGHEvent() {
		return GHEvent.valueOf(name);
	}
	
}
