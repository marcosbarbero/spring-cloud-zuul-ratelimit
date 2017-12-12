package com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.repository;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.matches;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.properties.RateLimitProperties.Policy;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class AbstractRateLimiterTest {

    private AbstractRateLimiter abstractRateLimiter;
    private RateLimiterErrorHandler rateLimiterErrorHandler;

    @Before
    public void setUp() throws Exception {
        abstractRateLimiter = Mockito.mock(AbstractRateLimiter.class, Mockito.CALLS_REAL_METHODS);
        Field rateLimiterErrorHandler = AbstractRateLimiter.class.getDeclaredField("rateLimiterErrorHandler");
        boolean accessible = rateLimiterErrorHandler.isAccessible();
        rateLimiterErrorHandler.setAccessible(true);
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(rateLimiterErrorHandler, rateLimiterErrorHandler.getModifiers() & ~Modifier.FINAL);
        this.rateLimiterErrorHandler = Mockito.mock(RateLimiterErrorHandler.class);
        rateLimiterErrorHandler.set(abstractRateLimiter, this.rateLimiterErrorHandler);
        rateLimiterErrorHandler.setAccessible(accessible);
        modifiersField.setInt(rateLimiterErrorHandler, rateLimiterErrorHandler.getModifiers() & Modifier.FINAL);
    }

    @Test
    public void testConsumeGetRateException() {
        when(abstractRateLimiter.getRate(any())).thenThrow(new RuntimeException());
        Policy policy = new Policy();
        policy.setLimit(100L);
        abstractRateLimiter.consume(policy, "key", 0L);
        verify(rateLimiterErrorHandler).handleFetchError(matches("key"), any());
    }

    @Test
    public void testConsumeSaveRateException() {
        doThrow(new RuntimeException()).when(abstractRateLimiter).saveRate(any());
        Policy policy = new Policy();
        policy.setLimit(100L);
        abstractRateLimiter.consume(policy, "key", 0L);
        verify(rateLimiterErrorHandler).handleSaveError(matches("key"), any());
    }
}