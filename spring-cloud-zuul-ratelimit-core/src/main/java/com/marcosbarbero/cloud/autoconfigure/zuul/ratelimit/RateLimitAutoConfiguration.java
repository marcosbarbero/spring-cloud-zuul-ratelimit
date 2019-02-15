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

import com.ecwid.consul.v1.ConsulClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hazelcast.core.IMap;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.RateLimitKeyGenerator;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.RateLimitUtils;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.RateLimiter;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.properties.RateLimitProperties;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.repository.ConsulRateLimiter;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.repository.DefaultRateLimiterErrorHandler;
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
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.support.DefaultRateLimitKeyGenerator;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.support.DefaultRateLimitUtils;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.support.SecuredRateLimitUtils;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.support.StringToMatchTypeConverter;
import com.netflix.zuul.ZuulFilter;
import io.github.bucket4j.grid.GridBucketState;
import io.github.bucket4j.grid.hazelcast.Hazelcast;
import io.github.bucket4j.grid.ignite.Ignite;
import io.github.bucket4j.grid.infinispan.Infinispan;
import io.github.bucket4j.grid.jcache.JCache;
import org.apache.ignite.IgniteCache;
import org.infinispan.functional.FunctionalMap.ReadWriteMap;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
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

import javax.cache.Cache;

import static com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.properties.RateLimitProperties.PREFIX;

/**
 * @author Marcos Barbero
 * @author Liel Chayoun
 */
@Configuration
@EnableConfigurationProperties(RateLimitProperties.class)
@ConditionalOnProperty(prefix = PREFIX, name = "enabled", havingValue = "true")
public class RateLimitAutoConfiguration {

    private final UrlPathHelper urlPathHelper = new UrlPathHelper();

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
    public ZuulFilter rateLimiterPreFilter(final RateLimiter rateLimiter, final RateLimitProperties rateLimitProperties,
                                           final RouteLocator routeLocator, final RateLimitKeyGenerator rateLimitKeyGenerator,
                                           final RateLimitUtils rateLimitUtils) {
        return new RateLimitPreFilter(rateLimitProperties, routeLocator, urlPathHelper, rateLimiter,
                rateLimitKeyGenerator, rateLimitUtils);
    }

    @Bean
    public ZuulFilter rateLimiterPostFilter(final RateLimiter rateLimiter, final RateLimitProperties rateLimitProperties,
                                            final RouteLocator routeLocator, final RateLimitKeyGenerator rateLimitKeyGenerator,
                                            final RateLimitUtils rateLimitUtils) {
        return new RateLimitPostFilter(rateLimitProperties, routeLocator, urlPathHelper, rateLimiter,
                rateLimitKeyGenerator, rateLimitUtils);
    }

    @Bean
    @ConditionalOnMissingBean(RateLimitKeyGenerator.class)
    public RateLimitKeyGenerator ratelimitKeyGenerator(final RateLimitProperties properties,
                                                       final RateLimitUtils rateLimitUtils) {
        return new DefaultRateLimitKeyGenerator(properties, rateLimitUtils);
    }

    @Configuration
    @ConditionalOnMissingBean(RateLimitUtils.class)
    public static class RateLimitUtilsConfiguration {

        @Bean
        @ConditionalOnClass(name = "org.springframework.security.core.Authentication")
        public RateLimitUtils securedRateLimitUtils(final RateLimitProperties rateLimitProperties) {
            return new SecuredRateLimitUtils(rateLimitProperties);
        }

        @Bean
        @ConditionalOnMissingClass("org.springframework.security.core.Authentication")
        public RateLimitUtils rateLimitUtils(final RateLimitProperties rateLimitProperties) {
            return new DefaultRateLimitUtils(rateLimitProperties);
        }
    }

    @Configuration
    @ConditionalOnClass(RedisTemplate.class)
    @ConditionalOnMissingBean(RateLimiter.class)
    @ConditionalOnProperty(prefix = PREFIX, name = "repository", havingValue = "REDIS")
    public static class RedisConfiguration {

        @Bean("rateLimiterRedisTemplate")
        public StringRedisTemplate redisTemplate(final RedisConnectionFactory connectionFactory) {
            return new StringRedisTemplate(connectionFactory);
        }

        @Bean
        public RateLimiter redisRateLimiter(final RateLimiterErrorHandler rateLimiterErrorHandler,
                                            @Qualifier("rateLimiterRedisTemplate") final RedisTemplate redisTemplate) {
            return new RedisRateLimiter(rateLimiterErrorHandler, redisTemplate);
        }
    }

    @Configuration
    @ConditionalOnConsulEnabled
    @ConditionalOnMissingBean(RateLimiter.class)
    @ConditionalOnProperty(prefix = PREFIX, name = "repository", havingValue = "CONSUL")
    public static class ConsulConfiguration {

        @Bean
        public RateLimiter consultRateLimiter(final RateLimiterErrorHandler rateLimiterErrorHandler,
                                              final ConsulClient consulClient, final ObjectMapper objectMapper) {
            return new ConsulRateLimiter(rateLimiterErrorHandler, consulClient, objectMapper);
        }

    }

    @Configuration
    @ConditionalOnMissingBean(RateLimiter.class)
    @ConditionalOnClass({JCache.class, Cache.class})
    @ConditionalOnProperty(prefix = PREFIX, name = "repository", havingValue = "BUCKET4J_JCACHE")
    public static class Bucket4jJCacheConfiguration {

        @Bean
        public RateLimiter jCache4jHazelcastRateLimiter(@Qualifier("RateLimit") final Cache<String, GridBucketState> cache) {
            return new Bucket4jJCacheRateLimiter(cache);
        }
    }

    @Configuration
    @ConditionalOnMissingBean(RateLimiter.class)
    @ConditionalOnClass({Hazelcast.class, IMap.class})
    @ConditionalOnProperty(prefix = PREFIX, name = "repository", havingValue = "BUCKET4J_HAZELCAST")
    public static class Bucket4jHazelcastConfiguration {

        @Bean
        public RateLimiter bucket4jHazelcastRateLimiter(@Qualifier("RateLimit") final IMap<String, GridBucketState> rateLimit) {
            return new Bucket4jHazelcastRateLimiter(rateLimit);
        }
    }

    @Configuration
    @ConditionalOnMissingBean(RateLimiter.class)
    @ConditionalOnClass({Ignite.class, IgniteCache.class})
    @ConditionalOnProperty(prefix = PREFIX, name = "repository", havingValue = "BUCKET4J_IGNITE")
    public static class Bucket4jIgniteConfiguration {

        @Bean
        public RateLimiter bucket4jIgniteRateLimiter(@Qualifier("RateLimit") final IgniteCache<String, GridBucketState> cache) {
            return new Bucket4jIgniteRateLimiter(cache);
        }
    }

    @Configuration
    @ConditionalOnMissingBean(RateLimiter.class)
    @ConditionalOnClass({Infinispan.class, ReadWriteMap.class})
    @ConditionalOnProperty(prefix = PREFIX, name = "repository", havingValue = "BUCKET4J_INFINISPAN")
    public static class Bucket4jInfinispanConfiguration {

        @Bean
        public RateLimiter bucket4jInfinispanRateLimiter(@Qualifier("RateLimit") final ReadWriteMap<String, GridBucketState> readWriteMap) {
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
        public RateLimiter springDataRateLimiter(final RateLimiterErrorHandler rateLimiterErrorHandler,
                                                 final RateLimiterRepository rateLimiterRepository) {
            return new JpaRateLimiter(rateLimiterErrorHandler, rateLimiterRepository);
        }

    }

}
