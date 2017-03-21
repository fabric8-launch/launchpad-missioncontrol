package org.kontinuity.catapult.core.api;

import java.util.UUID;

/**
 * Status message that wraps a {@link StatusMessage} and additional state
 */
public class StatusMessageEvent {
    private Projectile projectile;
    private StatusMessage statusMessage;
    private Object[] data;

    public StatusMessageEvent(Projectile projectile, StatusMessage statusMessage, Object[] data) {
        this.projectile = projectile;
        this.statusMessage = statusMessage;
        this.data = data;
    }

    public UUID getId() {
        return projectile.getId();
    }

    public String getFormattedMessage() {
        return String.format(statusMessage.getMessage(), data);
    }
}
