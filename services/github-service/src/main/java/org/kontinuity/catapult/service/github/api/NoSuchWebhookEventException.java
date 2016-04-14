package org.kontinuity.catapult.service.github.api;

/**
 * Indicates a specified Webhook event does not exist.
 * 
 * @author <a href="mailto:rmartine@redhat.com">Ricardo Martinelli de Oliveira</a>
 */
public class NoSuchWebhookEventException extends RuntimeException {
	
	private static final String MSG_PREFIX = "No such Webhook event named ";

    public NoSuchWebhookEventException(final String eventName) {
        super(MSG_PREFIX + eventName);
    }

}
