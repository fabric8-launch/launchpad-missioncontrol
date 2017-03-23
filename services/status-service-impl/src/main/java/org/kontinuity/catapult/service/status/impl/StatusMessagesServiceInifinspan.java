package org.kontinuity.catapult.service.status.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.infinispan.Cache;
import org.kontinuity.catapult.core.api.StatusMessage;
import org.kontinuity.catapult.core.api.StatusMessageEvent;
import org.kontinuity.catapult.service.status.api.StatusMessagesService;
import org.kontinuity.catapult.service.status.impl.config.StatusMessageCache;

/**
 * Implementation of {@link StatusMessagesService} that relies on infinispan to do the caching
 */
@ApplicationScoped
public class StatusMessagesServiceInifinspan implements StatusMessagesService {
    private final Cache<UUID, List<StatusMessageEvent>> cache;

    @Inject
    @StatusMessageCache
    public StatusMessagesServiceInifinspan(Cache<UUID, List<StatusMessageEvent>> cache) {
        this.cache = cache;
    }

    @Override
    public List<StatusMessageEvent> getStatusMessages(UUID id) {
        return cache.get(id);
    }

    public void onEvent(@Observes StatusMessageEvent msg) throws IOException {
        List<StatusMessageEvent> messageEventList = cache.get(msg.getId());
        if (messageEventList == null) {
            messageEventList = new ArrayList<>(StatusMessage.values().length);
        }
        messageEventList.add(msg);
        cache.put(msg.getId(), messageEventList);
    }
}
