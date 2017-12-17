package com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.filters.pre;

import static org.mockito.Mockito.mock;

import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.repository.RateLimiterErrorHandler;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.repository.InMemoryRateLimiter;
import org.junit.Before;

/**
 * @author Marcos Barbero
 * @since 2017-06-23
 */
public class InMemoryRateLimitPreFilterTest extends BaseRateLimitPreFilterTest {

    @Before
    @Override
    public void setUp() {
        RateLimiterErrorHandler rateLimiterErrorHandler = mock(RateLimiterErrorHandler.class);
        InMemoryRateLimiter rateLimiter = new InMemoryRateLimiter(rateLimiterErrorHandler);
        this.setRateLimiter(rateLimiter);
        super.setUp();
    }
}
