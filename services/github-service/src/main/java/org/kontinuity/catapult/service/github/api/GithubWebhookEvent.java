package org.kontinuity.catapult.service.github.api;

import org.kohsuke.github.GHEvent;

/**
 * Value object representing a webhook event in GitHub
 *
 * @author <a href="mailto:rmartine@redhat.com">Ricardo Martinelli de Oliveira</a>
 */
public interface GithubWebhookEvent {
	
	/**
	 * Obtains the Github Webhook event.
	 * 
	 * @return
	 */
	String getName();
	
	/**
	 * Convert the value object to the actual Github Webhook event.
	 * 
	 * @return
	 */
	GHEvent toGHEvent();
	
}
