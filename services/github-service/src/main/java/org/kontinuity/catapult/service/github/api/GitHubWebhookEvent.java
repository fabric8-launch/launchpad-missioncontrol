package org.kontinuity.catapult.service.github.api;

/**
 * Value object representing a webhook event in GitHub
 *
 * @author <a href="mailto:rmartine@redhat.com">Ricardo Martinelli de Oliveira</a>
 */
public interface GitHubWebhookEvent {
	
	/**
	 * Obtains the Github Webhook event.
	 * 
	 * @return
	 */
	String getName();
	
}
