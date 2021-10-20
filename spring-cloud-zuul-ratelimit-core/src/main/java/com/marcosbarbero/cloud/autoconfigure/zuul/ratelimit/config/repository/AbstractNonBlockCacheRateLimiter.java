package com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.repository;

import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.Rate;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.RateLimiter;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.properties.RateLimitProperties;

import java.time.Duration;

/**
 * @author mohamed fawzy
 */
public abstract class AbstractNonBlockCacheRateLimiter implements RateLimiter {

    @Override
    public Rate consume(RateLimitProperties.Policy policy, String key, Long requestTime) {
        final Duration refreshInterval = policy.getRefreshInterval();
        final Long quota = policy.getQuota() != null ? policy.getQuota().toMillis() : null;
        final Rate rate = new Rate(key, policy.getLimit(), quota, null, null);

        calcRemainingLimit(policy.getLimit(), refreshInterval, requestTime, key, rate);
        calcRemainingQuota(quota, refreshInterval, requestTime, key, rate);

        return rate;
    }

    protected abstract void calcRemainingLimit(Long limit, Duration refreshInterval, Long requestTime, String key, Rate rate);

    protected abstract void calcRemainingQuota(Long quota, Duration refreshInterval, Long requestTime, String key, Rate rate);
}

