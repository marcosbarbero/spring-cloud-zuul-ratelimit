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
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.properties.Policy;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.Assert;

/**
 * @author Marcos Barbero
 */
public class RedisRateLimiter implements RateLimiter {
    private final RedisTemplate template;

    public RedisRateLimiter(final RedisTemplate template) {
        Assert.notNull(template, "RedisTemplate cannot be null");
        this.template = template;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Rate consume(final Policy policy, final String key) {
        final Long limit = policy.getLimit();
        final Long refreshInterval = policy.getRefreshInterval();
        final Long current = this.template.boundValueOps(key).increment(1L);
        Long expire = this.template.getExpire(key);
        if (isExpired(expire)) {
            this.template.expire(key, refreshInterval, SECONDS);
            expire = refreshInterval;
        }
        return new Rate(limit, Math.max(-1, limit - current), SECONDS.toMillis(expire), null);
    }

    private static boolean isExpired(Long expire) {
        return expire == null || expire == -1;
    }
}
