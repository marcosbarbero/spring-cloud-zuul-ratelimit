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

package com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.repository.bucket4j;

import static java.util.concurrent.TimeUnit.NANOSECONDS;

import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.Rate;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.repository.AbstractCacheRateLimiter;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.ConfigurationBuilder;
import io.github.bucket4j.ConsumptionProbe;
import io.github.bucket4j.Extension;
import io.github.bucket4j.grid.ProxyManager;
import java.time.Duration;
import java.util.function.Supplier;

/**
 * Bucket4j rate limiter configuration.
 *
 * @author Liel Chayoun
 * @since 2018-04-06
 */
public abstract class AbstractBucket4jRateLimiter<T extends ConfigurationBuilder<T>, E extends Extension<T>> extends AbstractCacheRateLimiter {

    private final Class<E> extension;
    private ProxyManager<String> buckets;

    public AbstractBucket4jRateLimiter(Class<E> extension) {
        this.extension = extension;
    }

    public void init() {
        buckets = getProxyManager(getExtension());
    }

    private E getExtension() {
        return Bucket4j.extension(extension);
    }

    protected abstract ProxyManager<String> getProxyManager(E extension);

    private Bucket getQuotaBucket(String key, Long quota, Long refreshInterval) {
        return buckets.getProxy(key + QUOTA_SUFFIX, getBucketConfiguration(quota, refreshInterval));
    }

    private Bucket getLimitBucket(String key, Long limit, Long refreshInterval) {
        return buckets.getProxy(key, getBucketConfiguration(limit, refreshInterval));
    }

    private Supplier<BucketConfiguration> getBucketConfiguration(Long capacity, Long period) {
        return () -> getExtension().builder()
            .addLimit(Bandwidth.simple(capacity, Duration.ofSeconds(period)))
            .buildConfiguration();
    }

    @Override
    protected void calcRemainingLimit(Long limit, Long refreshInterval, Long requestTime, String key, Rate rate) {
        if (limit != null) {
            Bucket limitBucket = getLimitBucket(key, limit, refreshInterval);
            if (requestTime == null) {
                ConsumptionProbe limitConsumptionProbe = limitBucket.tryConsumeAndReturnRemaining(1);
                long nanosToWaitForRefill = limitConsumptionProbe.getNanosToWaitForRefill();
                rate.setReset(NANOSECONDS.toMillis(nanosToWaitForRefill));
                if (limitConsumptionProbe.isConsumed()) {
                    rate.setRemaining(limitConsumptionProbe.getRemainingTokens());
                } else {
                    rate.setRemaining(-1L);
                    limitBucket.tryConsumeAsMuchAsPossible(1);
                }
            } else {
                long availableTokens = limitBucket.getAvailableTokens();
                rate.setRemaining(availableTokens > 0 ? availableTokens : -1);
            }
        }
    }

    @Override
    protected void calcRemainingQuota(Long quota, Long refreshInterval, Long requestTime, String key, Rate rate) {
        if (quota != null) {
            Bucket quotaBucket = getQuotaBucket(key, quota, refreshInterval);
            if (requestTime != null) {
                ConsumptionProbe quotaConsumptionProbe = quotaBucket.tryConsumeAndReturnRemaining(requestTime);
                long nanosToWaitForRefill = quotaConsumptionProbe.getNanosToWaitForRefill();
                rate.setReset(NANOSECONDS.toMillis(nanosToWaitForRefill));
                if (quotaConsumptionProbe.isConsumed()) {
                    rate.setRemainingQuota(quotaConsumptionProbe.getRemainingTokens());
                } else {
                    rate.setRemainingQuota(-1L);
                    quotaBucket.tryConsumeAsMuchAsPossible(requestTime);
                }
            } else {
                long availableTokens = quotaBucket.getAvailableTokens();
                rate.setRemainingQuota(availableTokens > 0 ? availableTokens : -1);
            }
        }
    }
}
