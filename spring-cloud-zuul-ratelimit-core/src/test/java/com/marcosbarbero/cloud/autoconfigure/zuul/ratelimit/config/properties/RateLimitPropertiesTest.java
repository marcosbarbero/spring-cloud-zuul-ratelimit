package com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.properties.RateLimitProperties.Policy;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = RateLimitPropertiesTest.TestConfiguration.class)
@ActiveProfiles({"test-duration", "test-location"})
class RateLimitPropertiesTest {
    @Autowired
    private RateLimitProperties properties;

    @Test
    void should_populate_policies() {
        Map<String, List<Policy>> policyList = this.properties.getPolicyList();
        assertThat(policyList).containsKeys("defaultValues", "withoutUnit", "withSeconds", "withMinutes");
        assertAll("Should populate policies list.",
                () -> assertThat(this.properties.getPolicies("defaultValues")).isNotEmpty(),
                () -> assertThat(this.properties.getPolicies("withoutUnit")).isNotEmpty(),
                () -> assertThat(this.properties.getPolicies("withSeconds")).isNotEmpty(),
                () -> assertThat(this.properties.getPolicies("withMinutes")).isNotEmpty()
        );
    }

    @ParameterizedTest(name = "[{index}] Service \"{0}\" should have: refreshInterval = {1} and quota = {2}.")
    @MethodSource("refreshIntervalDs")
    void should_populate_refreshinterval(String serviceId, Duration expectedRefreshInterval, Duration expectedQuota) {
        List<Policy> policies = this.properties.getPolicies(serviceId);
        assertThat(policies).hasSize(1);
        assertEquals(policies.get(0).getRefreshInterval(), expectedRefreshInterval);
        assertEquals(policies.get(0).getQuota(), expectedQuota);
    }

    private static Stream<Arguments> refreshIntervalDs() {
        return Stream.of(
                Arguments.of("defaultValues", Duration.ofSeconds(60), null),
                Arguments.of("withoutUnit", Duration.ofSeconds(2), Duration.ofSeconds(2)),
                Arguments.of("withSeconds", Duration.ofSeconds(30), Duration.ofSeconds(30)),
                Arguments.of("withMinutes", Duration.ofMinutes(1), Duration.ofMinutes(1))
        );
    }

    @Test
    void should_populate_location() {
        final var location = properties.getLocation();
        assertAll("Should populate location deny and bypass.",
                () -> assertThat(properties.getLocation().getBypass()).containsExactly("127.0.0.1"),
                () -> assertThat(properties.getLocation().getDeny()).containsExactly("126.0.0.1", "125.0.0.1")
        );
    }

    @EnableConfigurationProperties(RateLimitProperties.class)
    public static class TestConfiguration {
        // nothing
    }
}
