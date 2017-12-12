package com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.repository;

public interface RateLimiterErrorHandler {

    void handleSaveError(String key, Exception e);
    void handleFetchError(String key, Exception e);
    void handleError(String msg, Exception e);
}
