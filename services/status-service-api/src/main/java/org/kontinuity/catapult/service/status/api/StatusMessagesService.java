package org.kontinuity.catapult.service.status.api;

import java.util.List;
import java.util.UUID;

import org.kontinuity.catapult.core.api.StatusMessageEvent;

/**
 * A service that stores status of pending / completed actions for a short period of time.
 */
public interface StatusMessagesService {

    List<StatusMessageEvent> getStatusMessages(UUID id);
}
