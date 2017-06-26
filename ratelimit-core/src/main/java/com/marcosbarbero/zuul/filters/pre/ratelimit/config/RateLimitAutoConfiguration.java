package com.marcosbarbero.zuul.filters.pre.ratelimit.config;

import com.marcosbarbero.zuul.filters.pre.ratelimit.RateLimitFilter;
import com.marcosbarbero.zuul.filters.pre.ratelimit.config.repository.InMemoryRateLimiter;
import com.marcosbarbero.zuul.filters.pre.ratelimit.config.repository.RedisRateLimiter;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.netflix.zuul.filters.RouteLocator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;

import static com.marcosbarbero.zuul.filters.pre.ratelimit.config.RateLimitProperties.PREFIX;

/**
 * @author Marcos Barbero
 */
@Configuration
@EnableConfigurationProperties(RateLimitProperties.class)
@ConditionalOnProperty(prefix = PREFIX, name = "enabled", havingValue = "true")
public class RateLimitAutoConfiguration {

    @Bean
    public RateLimitFilter rateLimiterFilter(RateLimiter rateLimiter, RateLimitProperties rateLimitProperties,
                                             RouteLocator routeLocator) {
        return new RateLimitFilter(rateLimiter, rateLimitProperties, routeLocator);
    }

    @ConditionalOnClass(RedisTemplate.class)
    @ConditionalOnMissingBean(RateLimiter.class)
    public static class RedisConfiguration {
        @Bean
        public StringRedisTemplate redisTemplate(RedisConnectionFactory connectionFactory) {
            return new StringRedisTemplate(connectionFactory);
        }

        @Bean
        @Primary
        public RateLimiter redisRateLimiter(RedisTemplate redisTemplate) {
            return new RedisRateLimiter(redisTemplate);
        }
    }

    @ConditionalOnMissingBean(RateLimiter.class)
    @ConditionalOnMissingClass("org.springframework.data.redis.core.RedisTemplate")
    public static class InMemoryConfiguration {

        @Bean
        public RateLimiter inMemoryRateLimiter() {
            return new InMemoryRateLimiter();
        }
    }

}
