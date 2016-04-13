package org.kontinuity.catapult.service.github.api;

/**
 * Value object representing a webhook in GitHub
 *
 * @author <a href="mailto:rmartine@redhat.com">Ricardo Martinelli de Oliveira</a>
 */
public interface GithubWebhook {
	
	/**
	 * Obtains the name of the webhook.
	 * 
	 * @return
	 */
	String getName();
	
	/**
	 * Obtains the Webhook URL
	 * 
	 * @return
	 */
	String getUrl();
	
	/**
	 * Obtains the events that will trigger the webhook.
	 * 
	 * @return
	 */
	GithubWebhookEvent[] getEvents();

}
