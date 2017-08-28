/*
 * Copyright 2012-2017 the original author or authors.
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
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.RateLimiter;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.properties.RateLimitProperties;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.repository.ConsulRateLimiter;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.repository.InMemoryRateLimiter;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.repository.RedisRateLimiter;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.filters.RateLimitFilter;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.consul.ConditionalOnConsulEnabled;
import org.springframework.cloud.netflix.zuul.filters.RouteLocator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;

import static com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.properties.RateLimitProperties.PREFIX;

/**
 * @author Marcos Barbero
 */
@Configuration
@EnableConfigurationProperties(RateLimitProperties.class)
@ConditionalOnProperty(prefix = PREFIX, name = "enabled", havingValue = "true")
public class RateLimitAutoConfiguration {

    @Bean
    public RateLimitFilter rateLimiterFilter(final RateLimiter rateLimiter,
                                             final RateLimitProperties rateLimitProperties,
                                             final RouteLocator routeLocator) {
        return new RateLimitFilter(rateLimiter, rateLimitProperties, routeLocator);
    }

    @ConditionalOnClass(RedisTemplate.class)
    @ConditionalOnMissingBean(RateLimiter.class)
    @ConditionalOnProperty(prefix = PREFIX, name = "strategy", havingValue = "REDIS")
    public static class RedisConfiguration {

        @Bean("rateLimiterRedisTemplate")
        public StringRedisTemplate redisTemplate(final RedisConnectionFactory connectionFactory) {
            return new StringRedisTemplate(connectionFactory);
        }

        @Bean
        public RateLimiter redisRateLimiter(@Qualifier("rateLimiterRedisTemplate") final RedisTemplate redisTemplate) {
            return new RedisRateLimiter(redisTemplate);
        }
    }

    @ConditionalOnConsulEnabled
    @ConditionalOnMissingBean(RateLimiter.class)
    @ConditionalOnMissingClass("org.springframework.data.redis.core.RedisTemplate")
    @ConditionalOnProperty(prefix = PREFIX , name = "strategy", havingValue = "CONSUL")
    public static class ConsulConfiguration {

        @Bean
        public RateLimiter consultRateLimiter(final ConsulClient consulClient, final ObjectMapper objectMapper) {
            return new ConsulRateLimiter(consulClient, objectMapper);
        }

    }

    @ConditionalOnMissingBean(RateLimiter.class)
    @ConditionalOnMissingClass({"org.springframework.data.redis.core.RedisTemplate",
            "com.ecwid.consul.v1.ConsulClient"})
    public static class InMemoryConfiguration {

        @Bean
        public RateLimiter inMemoryRateLimiter() {
            return new InMemoryRateLimiter();
        }
    }

}
