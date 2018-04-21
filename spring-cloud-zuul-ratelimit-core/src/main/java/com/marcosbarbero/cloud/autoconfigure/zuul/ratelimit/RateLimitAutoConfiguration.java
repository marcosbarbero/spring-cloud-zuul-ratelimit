/*
 * Copyright 2012-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit;

import static com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.properties.RateLimitProperties.PREFIX;

import com.ecwid.consul.v1.ConsulClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.hazelcast.core.IMap;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.DefaultRateLimitKeyGenerator;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.RateLimitKeyGenerator;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.RateLimiter;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.properties.RateLimitProperties;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.properties.RateLimitProperties.Policy;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.repository.ConsulRateLimiter;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.repository.DefaultRateLimiterErrorHandler;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.repository.InMemoryRateLimiter;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.repository.RateLimiterErrorHandler;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.repository.RedisRateLimiter;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.repository.bucket4j.Bucket4jHazelcastRateLimiter;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.repository.bucket4j.Bucket4jIgniteRateLimiter;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.repository.bucket4j.Bucket4jInfinispanRateLimiter;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.repository.bucket4j.Bucket4jJCacheRateLimiter;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.repository.springdata.JpaRateLimiter;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.repository.springdata.RateLimiterRepository;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.filters.RateLimitPostFilter;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.filters.RateLimitPreFilter;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.support.RateLimitUtils;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.support.StringToMatchTypeConverter;
import com.netflix.zuul.ZuulFilter;
import io.github.bucket4j.grid.GridBucketState;
import io.github.bucket4j.grid.hazelcast.Hazelcast;
import io.github.bucket4j.grid.ignite.Ignite;
import io.github.bucket4j.grid.infinispan.Infinispan;
import io.github.bucket4j.grid.jcache.JCache;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.cache.Cache;
import lombok.RequiredArgsConstructor;
import org.apache.ignite.IgniteCache;
import org.infinispan.functional.FunctionalMap.ReadWriteMap;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.ConfigurationPropertiesBinding;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.consul.ConditionalOnConsulEnabled;
import org.springframework.cloud.netflix.zuul.filters.RouteLocator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.util.UrlPathHelper;

/**
 * @author Marcos Barbero
 * @author Liel Chayoun
 */
@Configuration
@EnableConfigurationProperties(RateLimitProperties.class)
@ConditionalOnProperty(prefix = PREFIX, name = "enabled", havingValue = "true")
public class RateLimitAutoConfiguration {

    @Bean
    @ConfigurationPropertiesBinding
    public StringToMatchTypeConverter stringToMatchTypeConverter() {
        return new StringToMatchTypeConverter();
    }

    @Bean
    @ConditionalOnMissingBean(RateLimiterErrorHandler.class)
    public RateLimiterErrorHandler rateLimiterErrorHandler() {
        return new DefaultRateLimiterErrorHandler();
    }

    @Bean
    public RateLimitUtils rateLimitUtils(RateLimitProperties rateLimitProperties) {
        return new RateLimitUtils(rateLimitProperties);
    }

    @Bean
    public ZuulFilter rateLimiterPreFilter(RateLimiter rateLimiter, RateLimitProperties rateLimitProperties,
                                           RouteLocator routeLocator, RateLimitKeyGenerator rateLimitKeyGenerator,
                                           RateLimitUtils rateLimitUtils) {
        return new RateLimitPreFilter(rateLimitProperties, routeLocator, new UrlPathHelper(), rateLimiter,
                rateLimitKeyGenerator, rateLimitUtils);
    }

    @Bean
    public ZuulFilter rateLimiterPostFilter(RateLimiter rateLimiter, RateLimitProperties rateLimitProperties,
                                            RouteLocator routeLocator, RateLimitKeyGenerator rateLimitKeyGenerator,
                                            RateLimitUtils rateLimitUtils) {
        return new RateLimitPostFilter(rateLimitProperties, routeLocator, new UrlPathHelper(), rateLimiter,
                rateLimitKeyGenerator, rateLimitUtils);
    }

    @Bean
    @ConditionalOnMissingBean(RateLimitKeyGenerator.class)
    public RateLimitKeyGenerator ratelimitKeyGenerator(RateLimitProperties properties, RateLimitUtils rateLimitUtils) {
        return new DefaultRateLimitKeyGenerator(properties, rateLimitUtils);
    }

    @Configuration
    @ConditionalOnClass(RedisTemplate.class)
    @ConditionalOnMissingBean(RateLimiter.class)
    @ConditionalOnProperty(prefix = PREFIX, name = "repository", havingValue = "REDIS")
    public static class RedisConfiguration {

        @Bean("rateLimiterRedisTemplate")
        public StringRedisTemplate redisTemplate(RedisConnectionFactory connectionFactory) {
            return new StringRedisTemplate(connectionFactory);
        }

        @Bean
        public RateLimiter redisRateLimiter(RateLimiterErrorHandler rateLimiterErrorHandler,
                                            @Qualifier("rateLimiterRedisTemplate") RedisTemplate redisTemplate) {
            return new RedisRateLimiter(rateLimiterErrorHandler, redisTemplate);
        }
    }

    @Configuration
    @ConditionalOnConsulEnabled
    @ConditionalOnMissingBean(RateLimiter.class)
    @ConditionalOnProperty(prefix = PREFIX, name = "repository", havingValue = "CONSUL")
    public static class ConsulConfiguration {

        @Bean
        public RateLimiter consultRateLimiter(RateLimiterErrorHandler rateLimiterErrorHandler,
                                              ConsulClient consulClient, ObjectMapper objectMapper) {
            return new ConsulRateLimiter(rateLimiterErrorHandler, consulClient, objectMapper);
        }

    }

    @Configuration
    @ConditionalOnMissingBean(RateLimiter.class)
    @ConditionalOnClass({JCache.class, Cache.class})
    @ConditionalOnProperty(prefix = PREFIX, name = "repository", havingValue = "BUCKET4J_JCACHE")
    public static class Bucket4jJCacheConfiguration {

        @Bean
        public RateLimiter jCache4jHazelcastRateLimiter(@Qualifier("RateLimit") Cache<String, GridBucketState> cache) {
            return new Bucket4jJCacheRateLimiter(cache);
        }
    }

    @Configuration  
    @ConditionalOnMissingBean(RateLimiter.class)
    @ConditionalOnClass({Hazelcast.class, IMap.class})
    @ConditionalOnProperty(prefix = PREFIX, name = "repository", havingValue = "BUCKET4J_HAZELCAST")
    public static class Bucket4jHazelcastConfiguration {

        @Bean
        public RateLimiter bucket4jHazelcastRateLimiter(@Qualifier("RateLimit") IMap<String, GridBucketState> rateLimit) {
            return new Bucket4jHazelcastRateLimiter(rateLimit);
        }
    }

    @Configuration
    @ConditionalOnMissingBean(RateLimiter.class)
    @ConditionalOnClass({Ignite.class, IgniteCache.class})
    @ConditionalOnProperty(prefix = PREFIX, name = "repository", havingValue = "BUCKET4J_IGNITE")
    public static class Bucket4jIgniteConfiguration {

        @Bean
        public RateLimiter bucket4jIgniteRateLimiter(@Qualifier("RateLimit") IgniteCache<String, GridBucketState> cache) {
            return new Bucket4jIgniteRateLimiter(cache);
        }
    }

    @Configuration
    @ConditionalOnMissingBean(RateLimiter.class)
    @ConditionalOnClass({Infinispan.class, ReadWriteMap.class})
    @ConditionalOnProperty(prefix = PREFIX, name = "repository", havingValue = "BUCKET4J_INFINISPAN")
    public static class Bucket4jInfinispanConfiguration {

        @Bean
        public RateLimiter bucket4jInfinispanRateLimiter(@Qualifier("RateLimit") ReadWriteMap<String, GridBucketState> readWriteMap) {
            return new Bucket4jInfinispanRateLimiter(readWriteMap);
        }
    }

    @EntityScan
    @Configuration  
    @EnableJpaRepositories
    @ConditionalOnMissingBean(RateLimiter.class)
    @ConditionalOnProperty(prefix = PREFIX, name = "repository", havingValue = "JPA")
    public static class SpringDataConfiguration {

        @Bean
        public RateLimiter springDataRateLimiter(RateLimiterErrorHandler rateLimiterErrorHandler,
                                                 RateLimiterRepository rateLimiterRepository) {
            return new JpaRateLimiter(rateLimiterErrorHandler, rateLimiterRepository);
        }

    }

    @Configuration
    @ConditionalOnMissingBean(RateLimiter.class)
    @ConditionalOnProperty(prefix = PREFIX, name = "repository", havingValue = "IN_MEMORY", matchIfMissing = true)
    public static class InMemoryConfiguration {

        @Bean
        public RateLimiter inMemoryRateLimiter(RateLimiterErrorHandler rateLimiterErrorHandler) {
            return new InMemoryRateLimiter(rateLimiterErrorHandler);
        }
    }

    @Configuration
    @RequiredArgsConstructor
    protected static class RateLimitPropertiesAdjuster {

        private final RateLimitProperties rateLimitProperties;

        @PostConstruct
        public void init() {
            Policy defaultPolicy = rateLimitProperties.getDefaultPolicy();
            if (defaultPolicy != null) {
                ArrayList<Policy> defaultPolicies = Lists.newArrayList(defaultPolicy);
                defaultPolicies.addAll(rateLimitProperties.getDefaultPolicyList());
                rateLimitProperties.setDefaultPolicyList(defaultPolicies);
            }
            rateLimitProperties.getPolicies().forEach((route, policy) ->
                rateLimitProperties.getPolicyList().compute(route, (key, policies) -> getPolicies(policy, policies)));
        }

        private List<Policy> getPolicies(Policy policy, List<Policy> policies) {
            List<Policy> combinedPolicies = Lists.newArrayList(policy);
            if (policies != null) {
                combinedPolicies.addAll(policies);
            }
            return combinedPolicies;
        }
    }
}
