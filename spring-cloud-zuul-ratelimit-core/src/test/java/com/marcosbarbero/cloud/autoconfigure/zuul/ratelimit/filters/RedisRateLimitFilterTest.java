package com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.filters;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.repository.RedisRateLimiter;
import java.util.concurrent.TimeUnit;
import org.junit.Before;
import org.junit.Test;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * @author Marcos Barbero
 * @since 2017-06-30
 */
public class RedisRateLimitFilterTest extends BaseRateLimitFilterTest {

    private RedisTemplate redisTemplate;

    @Before
    @Override
    public void setUp() {
        redisTemplate = mock(RedisTemplate.class);
        this.setRateLimiter(new RedisRateLimiter(this.redisTemplate));
        super.setUp();
    }

    @Test
    @Override
    @SuppressWarnings("unchecked")
    public void testRateLimitExceedCapacity() throws Exception {
        BoundValueOperations ops = mock(BoundValueOperations.class);
        when(this.redisTemplate.boundValueOps(anyString())).thenReturn(ops);
        when(ops.increment(anyLong())).thenReturn(3L);
        super.testRateLimitExceedCapacity();
    }

    @Test
    @Override
    @SuppressWarnings("unchecked")
    public void testRateLimit() throws Exception {
        BoundValueOperations ops = mock(BoundValueOperations.class);
        when(this.redisTemplate.boundValueOps(anyString())).thenReturn(ops);
        when(ops.increment(anyLong())).thenReturn(2L);


        this.request.setRequestURI("/serviceA");
        this.request.setRemoteAddr("10.0.0.100");

        assertTrue(this.filter.shouldFilter());

        for (int i = 0; i < 2; i++) {
            this.filter.run();
        }

        String remaining = this.response.getHeader(RateLimitFilter.Headers.REMAINING);
        assertEquals("0", remaining);

        TimeUnit.SECONDS.sleep(2);

        when(ops.increment(anyLong())).thenReturn(1L);
        this.filter.run();
        remaining = this.response.getHeader(RateLimitFilter.Headers.REMAINING);
        assertEquals(remaining, "1");
    }
}
