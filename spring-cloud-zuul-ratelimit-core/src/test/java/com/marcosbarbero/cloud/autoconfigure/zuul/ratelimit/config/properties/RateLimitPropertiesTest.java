package com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.properties;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class RateLimitPropertiesTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void shouldNotThrowExceptionIfPostFilterHasGreaterOrderThanPreFilter() {
        RateLimitProperties properties = new RateLimitProperties();
        properties.setPreFilterOrder(10);
        properties.setPostFilterOrder(20);

        properties.afterPropertiesSet();
    }

    @Test
    public void shouldThrowExceptionIfPostFilterHasLessOrderThanPreFilter() {
        RateLimitProperties properties = new RateLimitProperties();
        properties.setPreFilterOrder(20);
        properties.setPostFilterOrder(10);

        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Value of postFilterOrder must be greater than preFilterOrder");

        properties.afterPropertiesSet();
    }

    @Test
    public void shouldThrowExceptionIfPostFilterHasEqualOrderAsPreFilter() {
        RateLimitProperties properties = new RateLimitProperties();
        properties.setPreFilterOrder(20);
        properties.setPostFilterOrder(20);

        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Value of postFilterOrder must be greater than preFilterOrder");

        properties.afterPropertiesSet();
    }
}