package com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.support.config.bucket4j;

import io.github.bucket4j.grid.GridBucketState;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.Ignition;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import javax.annotation.PreDestroy;

@TestConfiguration
@ConditionalOnProperty(name = "zuul.ratelimit.repository", havingValue = "bucket4j_jcache")
public class JCacheConfig {

    private Ignite ignite;

    @Bean
    @Qualifier("RateLimit")
    public IgniteCache<String, GridBucketState> cache() {
        ignite = Ignition.getOrStart(new IgniteConfiguration());
        return ignite.getOrCreateCache("rateLimit");
    }

    @PreDestroy
    public void destroy() {
        ignite.destroyCache("rateLimit");
    }
}
