package com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.repository;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.matches;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.collect.Maps;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.properties.RateLimitProperties.Policy;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.RedisTemplate;

@SuppressWarnings("unchecked")
public class RedisRateLimiterTest extends BaseRateLimiterTest {

    @Mock
    private RateLimiterErrorHandler rateLimiterErrorHandler;
    @Mock
    private RedisTemplate redisTemplate;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        Map<String, BoundValueOperations> map = Maps.newHashMap();
        Map<String, Long> longMap = Maps.newHashMap();
        when(redisTemplate.boundValueOps(any())).thenAnswer(invocation -> {
            String key = invocation.getArgument(0);
            BoundValueOperations mock = map.computeIfAbsent(key, k -> Mockito.mock(BoundValueOperations.class));
            when(mock.increment(anyLong())).thenAnswer(invocationOnMock -> {
                long value = invocationOnMock.getArgument(0);
                return longMap.compute(key, (k, v) -> ((v != null) ? v : 0L) + value);
            });
            return mock;
        });
        target = new RedisRateLimiter(rateLimiterErrorHandler, redisTemplate);
    }

    @Test
    public void testConsumeRemainingLimitException() {
        doThrow(new RuntimeException()).when(redisTemplate).boundValueOps("key");
        Policy policy = new Policy();
        policy.setLimit(100L);
        target.consume(policy, "key", 0L);
        verify(rateLimiterErrorHandler).handleError(matches(".* key, .*"), any());
    }

    @Test
    public void testConsumeRemainingQuotaLimitException() {
        doThrow(new RuntimeException()).when(redisTemplate).boundValueOps("key-quota");
        Policy policy = new Policy();
        policy.setQuota(100L);
        target.consume(policy, "key", 0L);
        verify(rateLimiterErrorHandler).handleError(matches(".* key-quota, .*"), any());
    }

    @Test
    public void testConsumeGetExpireException() {
        doThrow(new RuntimeException()).when(redisTemplate).getExpire("key");
        Policy policy = new Policy();
        policy.setLimit(100L);
        target.consume(policy, "key", 0L);
        verify(rateLimiterErrorHandler).handleError(matches(".* key, .*"), any());
    }

    @Test
    public void testConsumeExpireException() {
        when(redisTemplate.getExpire("key")).thenReturn(null);
        doThrow(new RuntimeException()).when(redisTemplate).expire(matches("key"), anyLong(), any());
        Policy policy = new Policy();
        policy.setLimit(100L);
        target.consume(policy, "key", 0L);
        verify(rateLimiterErrorHandler).handleError(matches(".* key, .*"), any());
    }
}