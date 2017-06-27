/*
 * Copyright 2012-2017 the original author or authors.
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

import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.properties.Policy;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.Rate;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.RateLimiter;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.concurrent.TimeUnit.SECONDS;

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
        this.update(rate);
        this.repository.put(key, rate);
        return rate;
    }

    private void update(Rate rate) {
        if (rate.getReset() > 0) {
            Long reset = rate.getExpiration().getTime() - System.currentTimeMillis();
            rate.setReset(reset);
        }
        rate.setRemaining(Math.max(-1, rate.getRemaining() - 1));
    }

    private Rate create(Policy policy, String key) {
        Rate rate = this.repository.get(key);
        if (isExpired(rate)) {
            rate = new Rate();

            final Long limit = policy.getLimit();
            final Long refreshInterval = SECONDS.toMillis(policy.getRefreshInterval());
            final Date expiration = new Date(System.currentTimeMillis() + refreshInterval);

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
