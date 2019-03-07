package com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.support.config.bucket4j;

import com.hazelcast.core.IMap;
import io.github.bucket4j.grid.GridBucketState;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
@ConditionalOnProperty(name = "zuul.ratelimit.repository", havingValue = "bucket4j_hazelcast")
public class HazelcastConfig {

    @Bean
    @Qualifier("RateLimit")
    public IMap<String, GridBucketState> map() {
        return com.hazelcast.core.Hazelcast.newHazelcastInstance().getMap("rateLimit");
    }
}
