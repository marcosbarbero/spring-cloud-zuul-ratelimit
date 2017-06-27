package com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.repository;

import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.Policy;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.Rate;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.RateLimiter;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In memory rate limiter configuration for dev environment.
 *
 * @author Marcos Barbero
 * @since 2017-06-23
 */
public class InMemoryRateLimiter implements RateLimiter {

    private Map<String, Rate> repository = new ConcurrentHashMap<>();

    @Override
    public synchronized Rate consume(final Policy policy, final String key) {
        Rate rate = this.create(policy, key);
        this.replenish(rate);
        this.repository.put(key, rate);
        return rate;
    }

    private void replenish(Rate rate) {
        if (rate.getReset() > 0) {
            Long reset = rate.getExpiration().getTime() - System.currentTimeMillis();
            rate.setReset(reset);
        }
        rate.setRemaining(rate.getRemaining() - 1);
    }

    private Rate create(Policy policy, String key) {
        Rate rate = this.repository.get(key);
        if (isExpired(rate)) {
            rate = new Rate();

            final Long limit = policy.getLimit();
            final Long refreshInterval = policy.getRefreshInterval();
            final Date expiration = new Date(System.currentTimeMillis() + (refreshInterval * 1000L));

            rate.setExpiration(expiration);
            rate.setLimit(limit);
            rate.setRemaining(limit);
            rate.setReset(refreshInterval);
        }
        return rate;
    }

    private boolean isExpired(Rate rate) {
        return rate == null || (rate.getExpiration().getTime() < System.currentTimeMillis());
    }
}
