package org.kontinuity.catapult.service.status.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;

import org.cache2k.Cache;
import org.cache2k.Cache2kBuilder;
import org.kontinuity.catapult.core.api.StatusMessage;
import org.kontinuity.catapult.core.api.StatusMessageEvent;
import org.kontinuity.catapult.service.status.api.StatusMessagesService;

/**
 * Implementation of {@link StatusMessagesService} that relies on infinispan to do the caching
 */
@ApplicationScoped
public class Cache2kStatusMessagesService implements StatusMessagesService {

    private Cache<UUID, List<StatusMessageEvent>> cache;

    @PostConstruct
    public void init() {
        cache = new Cache2kBuilder<UUID, List<StatusMessageEvent>>() {}
                .name("status-cache")
                .expireAfterWrite(1, TimeUnit.MINUTES)
                .entryCapacity(1000)
                .build();
    }

    public Cache2kStatusMessagesService() {}

    Cache2kStatusMessagesService(Cache<UUID, List<StatusMessageEvent>> cache) {
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
