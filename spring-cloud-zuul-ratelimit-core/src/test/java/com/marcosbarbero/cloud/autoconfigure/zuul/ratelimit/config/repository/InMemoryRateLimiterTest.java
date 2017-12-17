package com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.repository;

import org.junit.Before;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class InMemoryRateLimiterTest extends BaseRateLimiterTest {

    @Mock
    private RateLimiterErrorHandler rateLimiterErrorHandler;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        target = new InMemoryRateLimiter(rateLimiterErrorHandler);
    }
}