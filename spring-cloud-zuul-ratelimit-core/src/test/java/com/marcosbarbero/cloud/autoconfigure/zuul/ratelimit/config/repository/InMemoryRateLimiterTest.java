package com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.repository;

import org.junit.Before;

public class InMemoryRateLimiterTest extends BaseRateLimiterTest {

    @Before
    public void setUp() {
        target = new InMemoryRateLimiter();
    }
}