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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;

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
        if (limit != null) {
            handleExpiration(key, refreshInterval, rate);
            long usage = requestTime == null ? 1L : 0L;
            Long current = 0L;
            try {
                current = this.redisTemplate.boundValueOps(key).increment(usage);
            } catch (RuntimeException e) {
                String msg = "Failed retrieving rate for " + key + ", will return limit";
                rateLimiterErrorHandler.handleError(msg, e);
            }
            rate.setRemaining(Math.max(-1, limit - current));
        }
    }

    @Override
    protected void calcRemainingQuota(Long quota, Long refreshInterval,
                                    Long requestTime, String key, Rate rate) {
        if (quota != null) {
            String quotaKey = key + QUOTA_SUFFIX;
            handleExpiration(quotaKey, refreshInterval, rate);
            Long usage = requestTime != null ? requestTime : 0L;
            Long current = 0L;
            try {
                current = this.redisTemplate.boundValueOps(quotaKey).increment(usage);
            } catch (RuntimeException e) {
                String msg = "Failed retrieving rate for " + quotaKey + ", will return quota limit";
                rateLimiterErrorHandler.handleError(msg, e);
            }
            rate.setRemainingQuota(Math.max(-1, quota - current));
        }
    }

    private void handleExpiration(String key, Long refreshInterval, Rate rate) {
        Long expire = null;
        try {
            expire = this.redisTemplate.getExpire(key);
            if (expire == null || expire == -1) {
                this.redisTemplate.expire(key, refreshInterval, SECONDS);
                expire = refreshInterval;
            }
        } catch (RuntimeException e) {
            String msg = "Failed retrieving expiration for " + key + ", will reset now";
            rateLimiterErrorHandler.handleError(msg, e);
        }
        rate.setReset(SECONDS.toMillis(expire == null ? 0L : expire));
    }
}
