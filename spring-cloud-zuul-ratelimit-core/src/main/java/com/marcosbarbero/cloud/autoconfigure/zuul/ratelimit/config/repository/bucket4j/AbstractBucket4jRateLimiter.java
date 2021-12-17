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
import io.github.bucket4j.ConsumptionProbe;
import io.github.bucket4j.distributed.proxy.ProxyManager;

import java.time.Duration;
import java.util.function.Supplier;

/**
 * Bucket4j rate limiter configuration.
 *
 * @author Liel Chayoun
 * @since 2018-04-06
 */
abstract class AbstractBucket4jRateLimiter extends AbstractCacheRateLimiter {

    private ProxyManager<String> buckets;

    void init() {
        buckets = getProxyManager();
    }

    protected abstract ProxyManager<String> getProxyManager();

    private Bucket getQuotaBucket(String key, Long quota, Duration refreshInterval) {
        return buckets.builder().build(key + QUOTA_SUFFIX, getBucketConfiguration(quota, refreshInterval));
    }

    private Bucket getLimitBucket(String key, Long limit, Duration refreshInterval) {
        return buckets.builder().build(key, getBucketConfiguration(limit, refreshInterval));
    }

    private Supplier<BucketConfiguration> getBucketConfiguration(Long capacity, Duration period) {
        return () -> Bucket4j.configurationBuilder().addLimit(Bandwidth.simple(capacity, period)).build();
    }

    private void setRemaining(Rate rate, long remaining, boolean isQuota) {
        if (isQuota) {
            rate.setRemainingQuota(remaining);
        } else {
            rate.setRemaining(remaining);
        }
    }

    private void calcAndSetRemainingBucket(Long consume, Rate rate, Bucket bucket, boolean isQuota) {
        ConsumptionProbe consumptionProbe = bucket.tryConsumeAndReturnRemaining(consume);
        long nanosToWaitForRefill = consumptionProbe.getNanosToWaitForRefill();
        rate.setReset(NANOSECONDS.toMillis(nanosToWaitForRefill));
        if (consumptionProbe.isConsumed()) {
            long remainingTokens = consumptionProbe.getRemainingTokens();
            setRemaining(rate, remainingTokens, isQuota);
        } else {
            setRemaining(rate, -1L, isQuota);
            bucket.tryConsumeAsMuchAsPossible(consume);
        }
    }

    private void calcAndSetRemainingBucket(Bucket bucket, Rate rate, boolean isQuota) {
        long availableTokens = bucket.getAvailableTokens();
        long remaining = availableTokens > 0 ? availableTokens : -1;
        setRemaining(rate, remaining, isQuota);
    }

    @Override
    protected void calcRemainingLimit(final Long limit, final Duration refreshInterval, final Long requestTime,
                                      final String key, final Rate rate) {
        if (limit == null) {
            return;
        }
        Bucket bucket = getLimitBucket(key, limit, refreshInterval);
        if (requestTime == null) {
            calcAndSetRemainingBucket(1L, rate, bucket, false);
        } else {
            calcAndSetRemainingBucket(bucket, rate, false);
        }
    }

    @Override
    protected void calcRemainingQuota(final Long quota, final  Duration refreshInterval, final Long requestTime,
                                      final String key, final Rate rate) {
        if (quota == null) {
            return;
        }
        Bucket bucket = getQuotaBucket(key, quota, refreshInterval);
        if (requestTime != null) {
            calcAndSetRemainingBucket(requestTime, rate, bucket, true);
        } else {
            calcAndSetRemainingBucket(bucket, rate, true);
        }
    }
}
