package com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class DefaultRateLimiterErrorHandlerTest {

    private DefaultRateLimiterErrorHandler target;

    @BeforeEach
    public void setUp() {
        target = new DefaultRateLimiterErrorHandler();
    }

    @Test
    public void testHandleSaveErrorShouldNotThrowException() {
        target.handleSaveError("key", new Exception());
    }

    @Test
    public void testHandleFetchErrorShouldNotThrowException() {
        target.handleFetchError("key", new Exception());
    }

    @Test
    public void testHandleErrorShouldNotThrowException() {
        target.handleError("msg", new Exception());
    }
}