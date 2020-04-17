package com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.repository;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.matches;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.collect.Maps;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.Rate;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.properties.RateLimitProperties.Policy;
import java.time.Duration;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

@SuppressWarnings("unchecked")
public class RedisRateLimiterTest extends BaseRateLimiterTest {

    @Mock
    private RateLimiterErrorHandler rateLimiterErrorHandler;
    @Mock
    private StringRedisTemplate redisTemplate;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        Map<String, BoundValueOperations<String, String>> map = Maps.newHashMap();
        Map<String, Long> longMap = Maps.newHashMap();

        when(this.redisTemplate.boundValueOps(any())).thenAnswer(invocation -> {
            String key = invocation.getArgument(0);
            BoundValueOperations<String, String> mock = map.computeIfAbsent(key, k -> Mockito.mock(BoundValueOperations.class));
            when(mock.increment(anyLong())).thenAnswer(invocationOnMock -> {
                long value = invocationOnMock.getArgument(0);
                return longMap.compute(key, (k, v) -> ((v != null) ? v : 0L) + value);
            });
            return mock;
        });
        when(this.redisTemplate.opsForValue()).thenAnswer(invocation -> {
            ValueOperations<String, String> mock = mock(ValueOperations.class);
            when(mock.increment(any(), anyLong())).thenAnswer(invocationOnMock -> {
                String key = invocationOnMock.getArgument(0);
                long value = invocationOnMock.getArgument(1);
                return longMap.compute(key, (k, v) -> ((v != null) ? v : 0L) + value);
            });
            return mock;
        });

        this.target = new RedisRateLimiter(this.rateLimiterErrorHandler, this.redisTemplate);
    }

    @Test
    public void testConsumeRemainingLimitException() {
        ValueOperations<String, String> ops = mock(ValueOperations.class);
        when(ops.setIfAbsent(anyString(), anyString(), anyLong(), any())).thenReturn(false);
        doReturn(ops).when(redisTemplate).opsForValue();
        doThrow(new RuntimeException()).when(ops).increment(anyString(), anyLong());

        Policy policy = new Policy();
        policy.setLimit(100L);
        target.consume(policy, "key", 0L);
        verify(redisTemplate.opsForValue()).setIfAbsent(anyString(), anyString(), anyLong(), any());
        verify(redisTemplate.opsForValue()).increment(anyString(), anyLong());
        verify(rateLimiterErrorHandler).handleError(matches(".* key, .*"), any());
    }

    @Test
    public void testConsumeRemainingQuotaLimitException() {
        ValueOperations<String, String> ops = mock(ValueOperations.class);
        when(ops.setIfAbsent(anyString(), anyString(), anyLong(), any())).thenReturn(false);
        doReturn(ops).when(redisTemplate).opsForValue();
        doThrow(new RuntimeException()).when(ops).increment(anyString(), anyLong());

        Policy policy = new Policy();
        policy.setQuota(Duration.ofSeconds(100));
        target.consume(policy, "key", 0L);
        verify(redisTemplate.opsForValue()).setIfAbsent(anyString(), anyString(), anyLong(), any());
        verify(redisTemplate.opsForValue()).increment(anyString(), anyLong());
        verify(rateLimiterErrorHandler).handleError(matches(".* key-quota, .*"), any());
    }

    @Test
    public void testConsumeGetExpireException() {
        ValueOperations<String, String> ops = mock(ValueOperations.class);
        when(ops.setIfAbsent(anyString(), anyString(), anyLong(), any())).thenReturn(false);
        doReturn(ops).when(redisTemplate).opsForValue();
        doThrow(new RuntimeException()).when(ops).increment(anyString(), anyLong());

        Policy policy = new Policy();
        policy.setLimit(100L);
        policy.setQuota(Duration.ofSeconds(50));
        target.consume(policy, "key", 0L);
        verify(redisTemplate.opsForValue(), times(2)).setIfAbsent(anyString(), anyString(), anyLong(), any());
        verify(redisTemplate.opsForValue(), times(2)).increment(anyString(), anyLong());
        verify(rateLimiterErrorHandler).handleError(matches(".* key, .*"), any());
        verify(rateLimiterErrorHandler).handleError(matches(".* key-quota, .*"), any());
    }

    @Test
    public void testConsumeExpireException() {
        ValueOperations<String, String> ops = mock(ValueOperations.class);
        doThrow(new RuntimeException()).when(ops).setIfAbsent(anyString(), anyString(), anyLong(), any());
        when(ops.increment(anyString(), anyLong())).thenReturn(0L);
        doReturn(ops).when(redisTemplate).opsForValue();
        Policy policy = new Policy();
        policy.setLimit(100L);
        target.consume(policy, "key", 0L);
        verify(redisTemplate.opsForValue()).setIfAbsent(anyString(), anyString(), anyLong(), any());
        verify(redisTemplate.opsForValue(), never()).increment(any(), anyLong());
        verify(rateLimiterErrorHandler).handleError(matches(".* key, .*"), any());
    }

    @Test
    public void testConsumeSetKey() {
        ValueOperations<String, String> ops = mock(ValueOperations.class);
        when(ops.setIfAbsent(anyString(), anyString(), anyLong(), any())).thenReturn(true);
        doReturn(ops).when(redisTemplate).opsForValue();
        Policy policy = new Policy();
        policy.setLimit(20L);
        target.consume(policy, "key", 0L);
        verify(redisTemplate.opsForValue()).setIfAbsent(anyString(), anyString(), anyLong(), any());
        verify(redisTemplate.opsForValue(), never()).increment(any(), anyLong());
        verify(rateLimiterErrorHandler, never()).handleError(any(), any());
    }

    @Test // https://github.com/marcosbarbero/spring-cloud-zuul-ratelimit/issues/311
    public void testConsume_negativeTTL() {
        when(redisTemplate.getExpire("key")).thenReturn(-1L);

        Policy policy = new Policy();
        policy.setLimit(10L);
        policy.setQuota(Duration.ofSeconds(1));
        policy.setRefreshInterval(Duration.ofSeconds(2));

        Rate rate = target.consume(policy, "key", null);
        assertThat(rate.getRemaining()).isEqualTo(9L);
        assertThat(rate.getRemainingQuota()).isEqualTo(1000L);

        verify(redisTemplate).expire("key", 1, SECONDS);
    }
}