package com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.repository;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

import com.google.common.collect.Maps;
import java.util.Map;
import org.junit.Before;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.RedisTemplate;

public class RedisRateLimiterTest extends BaseRateLimiterTest {

    @Mock
    private IRateLimiterErrorHandler rateLimiterErrorHandler;
    @Mock
    private RedisTemplate redisTemplate;

    @Before
    @SuppressWarnings("unchecked")
    public void setUp() throws Exception {
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

}