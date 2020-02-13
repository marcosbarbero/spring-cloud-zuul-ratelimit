package com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.Rate;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.RateLimiter;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.properties.RateLimitProperties.Policy;
import java.time.Duration;
import org.junit.jupiter.api.Test;

public abstract class BaseRateLimiterTest {

    protected RateLimiter target;

    @Test
    public void testConsumeOnlyLimit() {
        Policy policy = new Policy();
        policy.setLimit(10L);
        policy.setRefreshInterval(Duration.ofSeconds(2));

        Rate rate = target.consume(policy, "key", null);
        assertThat(rate.getRemaining()).isEqualTo(9L);
        assertThat(rate.getRemainingQuota()).isNull();
    }

    @Test
    public void testConsumeOnlyQuota() {
        Policy policy = new Policy();
        policy.setQuota(Duration.ofSeconds(1));
        policy.setRefreshInterval(Duration.ofSeconds(2));

        Rate rate = target.consume(policy, "key", 800L);
        assertThat(rate.getRemainingQuota()).isEqualTo(200L);
        assertThat(rate.getRemaining()).isNull();
    }

    @Test
    public void testConsume() {
        Policy policy = new Policy();
        policy.setLimit(10L);
        policy.setQuota(Duration.ofSeconds(1));
        policy.setRefreshInterval(Duration.ofSeconds(2));

        Rate rate = target.consume(policy, "key", null);
        assertThat(rate.getRemaining()).isEqualTo(9L);
        assertThat(rate.getRemainingQuota()).isEqualTo(1000L);

        rate = target.consume(policy, "key", 800L);
        assertThat(rate.getRemaining()).isEqualTo(9L);
        assertThat(rate.getRemainingQuota()).isEqualTo(200L);
    }
}