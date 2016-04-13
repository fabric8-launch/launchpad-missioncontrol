package org.kontinuity.catapult.service.github.impl.kohsuke;

import org.kontinuity.catapult.service.github.api.GithubWebhook;
import org.kontinuity.catapult.service.github.api.GithubWebhookEvent;

public class KohsukeGithubWebhook implements GithubWebhook {
	
	private String name;
	
	private String url;
	
	private GithubWebhookEvent[] events;
	
	public KohsukeGithubWebhook(String name, String url, GithubWebhookEvent... events) {
		this.name = name;
		this.url = url;
		this.events = events;
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public String getUrl() {
		return this.url;
	}

	@Override
	public GithubWebhookEvent[] getEvents() {
		return this.events;
	}
	
}
