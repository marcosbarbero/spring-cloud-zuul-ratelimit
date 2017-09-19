package com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.filters.pre;

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
        InMemoryRateLimiter rateLimiter = new InMemoryRateLimiter();
        this.setRateLimiter(rateLimiter);
        super.setUp();
    }
}
