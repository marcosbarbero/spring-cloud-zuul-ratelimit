package com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.support;

import static org.assertj.core.api.Assertions.assertThat;

import com.netflix.zuul.exception.ZuulException;
import com.netflix.zuul.monitoring.CounterFactory;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class RateLimitExceededExceptionTest {

    private RateLimitExceededException target;

    @Mock
    private CounterFactory counterFactory;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        CounterFactory.initialize(counterFactory);
        target = new RateLimitExceededException();
    }

    @Test
    public void testExceptionInfo() {
        Throwable cause = target.getCause();
        assertThat(cause).isInstanceOf(ZuulException.class);

        ZuulException zuulException = (ZuulException) cause;
        assertThat(zuulException.getMessage()).isEqualTo("429");
    }
}