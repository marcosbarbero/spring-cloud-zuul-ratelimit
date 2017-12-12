package com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.repository;

public interface IRateLimiterErrorHandler {

    void handleSaveError(String key, Exception e);
    void handleFetchError(String key, Exception e);
    void handleError(String msg, Exception e);
}
