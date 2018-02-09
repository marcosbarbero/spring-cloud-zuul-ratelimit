/*
 * Copyright 2012-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.repository;

import static java.util.concurrent.TimeUnit.SECONDS;

import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.Rate;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.RateLimiter;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.properties.RateLimitProperties.Policy;
import java.util.Date;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Abstract implementation for {@link RateLimiter}.
 *
 * @author Liel Chayoun
 * @author Marcos Barbero
 * @since 2017-08-28
 */
@Slf4j
@RequiredArgsConstructor
public abstract class AbstractRateLimiter implements RateLimiter {

    private final RateLimiterErrorHandler rateLimiterErrorHandler;

    protected abstract Rate getRate(String key);
    protected abstract void saveRate(Rate rate);

    @Override
    public synchronized Rate consume(final Policy policy, final String key, final Long requestTime) {
        Rate rate = this.create(policy, key);
        updateRate(policy, rate, requestTime);
        try {
            saveRate(rate);
        } catch (RuntimeException e) {
            rateLimiterErrorHandler.handleSaveError(key, e);
        }
        return rate;
    }

    private Rate create(final Policy policy, final String key) {
        Rate rate = null;
        try {
            rate = this.getRate(key);
        } catch (RuntimeException e) {
            rateLimiterErrorHandler.handleFetchError(key, e);
        }

        if (!isExpired(rate)) {
            return rate;
        }

        Long limit = policy.getLimit();
        Long quota = policy.getQuota() != null ? SECONDS.toMillis(policy.getQuota()) : null;
        Long refreshInterval = SECONDS.toMillis(policy.getRefreshInterval());
        Date expiration = new Date(System.currentTimeMillis() + refreshInterval);

        return new Rate(key, limit, quota, refreshInterval, expiration);
    }

    private void updateRate(final Policy policy, final Rate rate, final Long requestTime) {
        if (rate.getReset() > 0) {
            Long reset = rate.getExpiration().getTime() - System.currentTimeMillis();
            rate.setReset(reset);
        }
        if (policy.getLimit() != null && requestTime == null) {
            rate.setRemaining(Math.max(-1, rate.getRemaining() - 1));
        }
        if (policy.getQuota() != null && requestTime != null) {
            rate.setRemainingQuota(Math.max(-1, rate.getRemainingQuota() - requestTime));
        }
    }

    private boolean isExpired(final Rate rate) {
        return rate == null || (rate.getExpiration().getTime() < System.currentTimeMillis());
    }
}
