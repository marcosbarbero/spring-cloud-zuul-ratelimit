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

import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.Rate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Objects;

import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * @author Marcos Barbero
 * @author Liel Chayoun
 */
@Slf4j
@RequiredArgsConstructor
@SuppressWarnings("unchecked")
public class RedisRateLimiter extends AbstractCacheRateLimiter {

    private final RateLimiterErrorHandler rateLimiterErrorHandler;
    private final RedisTemplate redisTemplate;

    @Override
    protected void calcRemainingLimit(Long limit, Long refreshInterval,
                                      Long requestTime, String key, Rate rate) {
        if (Objects.nonNull(limit)) {
            long usage = requestTime == null ? 1L : 0L;
            Long remaining = calcRemaining(limit, refreshInterval, usage, key, rate);
            rate.setRemaining(remaining);
        }
    }

    @Override
    protected void calcRemainingQuota(Long quota, Long refreshInterval,
                                      Long requestTime, String key, Rate rate) {
        if (Objects.nonNull(quota)) {
            String quotaKey = key + QUOTA_SUFFIX;
            long usage = requestTime != null ? requestTime : 0L;
            Long remaining = calcRemaining(quota, refreshInterval, usage, quotaKey, rate);
            rate.setRemainingQuota(remaining);
        }
    }

    private Long calcRemaining(Long limit, Long refreshInterval, long usage,
                               String key, Rate rate) {
        rate.setReset(SECONDS.toMillis(refreshInterval));
        Long current = 0L;
        try {
            current = redisTemplate.opsForValue().increment(key, usage);
            // Redis returns 1 when the key is incremented for the first time, and the expiration time is set
            if (current != null && current.equals(1L)) {
                handleExpiration(key, refreshInterval);
            }
        } catch (RuntimeException e) {
            String msg = "Failed retrieving rate for " + key + ", will return the current value";
            rateLimiterErrorHandler.handleError(msg, e);
        }
        return Math.max(-1, limit - current);
    }

    private void handleExpiration(String key, Long refreshInterval) {
        try {
            this.redisTemplate.expire(key, refreshInterval, SECONDS);
        } catch (RuntimeException e) {
            String msg = "Failed retrieving expiration for " + key + ", will reset now";
            rateLimiterErrorHandler.handleError(msg, e);
        }
    }
}
