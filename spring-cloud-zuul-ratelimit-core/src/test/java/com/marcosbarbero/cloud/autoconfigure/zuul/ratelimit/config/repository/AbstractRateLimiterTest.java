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
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class AbstractRateLimiterTest {

    private AbstractRateLimiter target;

    @Mock
    private RateLimiterErrorHandler rateLimiterErrorHandler;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        target = Mockito.mock(AbstractRateLimiter.class, Mockito.CALLS_REAL_METHODS);
        Field rateLimiterErrorHandler = AbstractRateLimiter.class.getDeclaredField("rateLimiterErrorHandler");
        boolean accessible = rateLimiterErrorHandler.isAccessible();
        rateLimiterErrorHandler.setAccessible(true);
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(rateLimiterErrorHandler, rateLimiterErrorHandler.getModifiers() & ~Modifier.FINAL);
        rateLimiterErrorHandler.set(target, this.rateLimiterErrorHandler);
        rateLimiterErrorHandler.setAccessible(accessible);
        modifiersField.setInt(rateLimiterErrorHandler, rateLimiterErrorHandler.getModifiers() & Modifier.FINAL);
    }

    @Test
    public void testConsumeGetRateException() {
        when(target.getRate(any())).thenThrow(new RuntimeException());
        Policy policy = new Policy();
        policy.setLimit(100L);
        target.consume(policy, "key", 0L);
        verify(rateLimiterErrorHandler).handleFetchError(matches("key"), any());
    }

    @Test
    public void testConsumeSaveRateException() {
        doThrow(new RuntimeException()).when(target).saveRate(any());
        Policy policy = new Policy();
        policy.setLimit(100L);
        target.consume(policy, "key", 0L);
        verify(rateLimiterErrorHandler).handleSaveError(matches("key"), any());
    }
}