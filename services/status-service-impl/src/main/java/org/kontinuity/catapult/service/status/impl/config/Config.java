package org.kontinuity.catapult.service.status.impl.config;

import java.util.concurrent.TimeUnit;

import javax.enterprise.inject.Produces;

import org.infinispan.cdi.ConfigureCache;
import org.infinispan.configuration.cache.Configuration;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.eviction.EvictionStrategy;
import org.infinispan.eviction.EvictionType;

/**
 * Configuration for the {@link StatusMessageCache}
 */
public class Config {

    /**
     * <p>This producer defines the status message cache configuration.</p>
     *
     * <p>This cache will have:
     * <ul>
     *    <li>a maximum of 1 entries</li>
     *    <li>use the strategy LRU for eviction</li>
     * </ul>
     * </p>
     *
     * @return the greeting cache configuration.
     */
    @StatusMessageCache
    @ConfigureCache("statusMessage-cache")
    @Produces
    public Configuration greetingCache() {
        return new ConfigurationBuilder()
                .eviction().strategy(EvictionStrategy.LRU).type(EvictionType.COUNT).size(1)
                .build();
    }

    /**
     * <p>This producer overrides the default cache configuration used by the default cache manager.</p>
     *
     * <p>The default cache configuration defines that a cache entry will have a lifespan of 60000 ms.</p>
     */
    @Produces
    public Configuration defaultCacheConfiguration() {
        return new ConfigurationBuilder()
                .jmxStatistics().disable()
                .expiration().lifespan(1, TimeUnit.MINUTES)
                .build();
    }
}
