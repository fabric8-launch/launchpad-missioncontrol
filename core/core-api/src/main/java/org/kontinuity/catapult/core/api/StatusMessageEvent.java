package org.kontinuity.catapult.core.api;

import java.util.Map;
import java.util.UUID;

/**
 * Status message that wraps a {@link StatusMessage} and additional state
 */
public class StatusMessageEvent {
    private UUID id;
    private StatusMessage statusMessage;
    private Map<String, Object> data;

    public StatusMessageEvent(Projectile projectile, StatusMessage statusMessage, Map<String, Object> data) {
        this.id = projectile.getId();
        this.statusMessage = statusMessage;
        this.data = data;
    }

    public UUID getId() {
        return id;
    }

    public StatusMessage getStatusMessage() {
        return statusMessage;
    }

    public Map<String, Object> getData() {
        return data;
    }
}
