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

import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.Rate;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.RateLimiter;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.properties.RateLimitProperties.Policy;

import org.springframework.data.redis.core.RedisTemplate;

import lombok.RequiredArgsConstructor;

import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * @author Marcos Barbero
 * @author Liel Chayoun
 */
@RequiredArgsConstructor
@SuppressWarnings("unchecked")
public class RedisRateLimiter implements RateLimiter {

    private static final String QUOTA_SUFFIX = "-quota";

    private final RedisTemplate redisTemplate;

    @Override
    public Rate consume(final Policy policy, final String key, final Long requestTime) {
        final Long refreshInterval = policy.getRefreshInterval();
        final Long quota = policy.getQuota() != null ? SECONDS.toMillis(policy.getQuota()) : null;
        final Rate rate = new Rate(key, policy.getLimit(), quota, null, null);

        calcRemainingLimit(policy.getLimit(), refreshInterval, requestTime, key, rate);
        calcRemainingQuota(quota, refreshInterval, requestTime, key, rate);

        return rate;
    }

    private void calcRemainingLimit(Long limit, Long refreshInterval,
                                    Long requestTime, String key, Rate rate) {
        if (limit != null) {
            handleExpiration(key, refreshInterval, rate);
            long usage = requestTime == null ? 1L : 0L;
            Long current = this.redisTemplate.boundValueOps(key).increment(usage);
            rate.setRemaining(Math.max(-1, limit - current));
        }
    }

    private void calcRemainingQuota(Long quota, Long refreshInterval,
                                    Long requestTime, String key, Rate rate) {
        if (quota != null) {
            String quotaKey = key + QUOTA_SUFFIX;
            handleExpiration(quotaKey, refreshInterval, rate);
            Long usage = requestTime != null ? requestTime : 0L;
            Long current = this.redisTemplate.boundValueOps(quotaKey).increment(usage);
            rate.setRemainingQuota(Math.max(-1, quota - current));
        }
    }

    private void handleExpiration(String key, Long refreshInterval, Rate rate) {
        Long expire = this.redisTemplate.getExpire(key);
        if (expire == null || expire == -1) {
            this.redisTemplate.expire(key, refreshInterval, SECONDS);
            expire = refreshInterval;
        }
        rate.setReset(SECONDS.toMillis(expire));
    }
}
