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

import static java.util.concurrent.TimeUnit.SECONDS;

import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.Rate;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.RateLimiter;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.properties.RateLimitProperties.Policy;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * @author Marcos Barbero
 */
@RequiredArgsConstructor
@SuppressWarnings("unchecked")
public class RedisRateLimiter implements RateLimiter {

    private final RedisTemplate template;

    @Override
    public Rate consume(final Policy policy, final String key, final Long requestTime) {
        final Long refreshInterval = policy.getRefreshInterval();
        final Long quota = policy.getQuota() != null ? SECONDS.toMillis(policy.getQuota()) : null;
        Rate rate = new Rate(key, policy.getLimit(), quota, null, null);

        final Long limit = policy.getLimit();
        if (limit != null) {
            handleExpiration(key, refreshInterval, rate);
            long usage = requestTime == null ? 1L : 0L;
            final Long current = this.template.boundValueOps(key).increment(usage);
            rate.setRemaining(Math.max(-1, limit - current));
        }

        if (quota != null) {
            String quotaKey = key + "-quota";
            handleExpiration(quotaKey, refreshInterval, rate);
            Long usage =  requestTime != null ? requestTime : 0L;
            final Long current = this.template.boundValueOps(quotaKey).increment(usage);
            rate.setRemainingQuota(Math.max(-1, quota - current));
        }

        return rate;
    }

    private void handleExpiration(String key, Long refreshInterval, Rate rate) {
        Long expire = this.template.getExpire(key);
        if (expire == null || expire == -1) {
            this.template.expire(key, refreshInterval, SECONDS);
            expire = refreshInterval;
        }
        rate.setReset(SECONDS.toMillis(expire));
    }
}
