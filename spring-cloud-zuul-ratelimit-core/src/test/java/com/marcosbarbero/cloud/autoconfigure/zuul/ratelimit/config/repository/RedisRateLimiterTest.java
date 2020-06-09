package com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.repository;

import com.google.common.collect.Maps;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.properties.RateLimitProperties.Policy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SuppressWarnings("unchecked")
public class RedisRateLimiterTest extends BaseRateLimiterTest {

    @Mock
    private RateLimiterErrorHandler rateLimiterErrorHandler;
    @Mock
    private StringRedisTemplate redisTemplate;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        doReturn(1L, 2L)
                .when(redisTemplate).execute(any(), anyList(), anyString(), anyString());

        this.target = new RedisRateLimiter(this.rateLimiterErrorHandler, this.redisTemplate);
    }

    @Test
    @Disabled
    public void testConsumeOnlyQuota() {
        // disabling in favor of integration tests
    }

    @Test
    @Disabled
    public void testConsume() {
        // disabling in favor of integration tests
    }

    @Test
    public void testConsumeRemainingLimitException() {
        doThrow(new RuntimeException()).when(redisTemplate).execute(any(), anyList(), anyString(), anyString());

        Policy policy = new Policy();
        policy.setLimit(100L);
        target.consume(policy, "key", 0L);
        verify(rateLimiterErrorHandler).handleError(matches(".* key, .*"), any());
    }

    @Test
    public void testConsumeRemainingQuotaLimitException() {
        doThrow(new RuntimeException()).when(redisTemplate).execute(any(), anyList(), anyString(), anyString());

        Policy policy = new Policy();
        policy.setQuota(Duration.ofSeconds(100));
        target.consume(policy, "key", 0L);
        verify(rateLimiterErrorHandler).handleError(matches(".* key-quota, .*"), any());
    }

    @Test
    public void testConsumeGetExpireException() {
        doThrow(new RuntimeException()).when(redisTemplate).execute(any(), anyList(), anyString(), anyString());

        Policy policy = new Policy();
        policy.setLimit(100L);
        policy.setQuota(Duration.ofSeconds(50));
        target.consume(policy, "key", 0L);
        verify(rateLimiterErrorHandler).handleError(matches(".* key, .*"), any());
        verify(rateLimiterErrorHandler).handleError(matches(".* key-quota, .*"), any());
    }

    @Test
    public void testConsumeExpireException() {
        doThrow(new RuntimeException()).when(redisTemplate).execute(any(), anyList(), anyString(), anyString());

        Policy policy = new Policy();
        policy.setLimit(100L);
        target.consume(policy, "key", 0L);
        verify(rateLimiterErrorHandler).handleError(matches(".* key, .*"), any());
    }

    @Test
    public void testConsumeSetKey() {
        doReturn(1L, 2L)
                .when(redisTemplate).execute(any(), anyList(), anyString(), anyString());

        Policy policy = new Policy();
        policy.setLimit(20L);
        target.consume(policy, "key", 0L);

        verify(redisTemplate).execute(any(), anyList(), anyString(), anyString());
        verify(rateLimiterErrorHandler, never()).handleError(any(), any());
    }
}