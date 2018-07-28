package com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.support;

import static org.assertj.core.api.Assertions.assertThat;

import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.properties.RateLimitProperties.Policy.MatchType;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.properties.RateLimitType;
import org.junit.Before;
import org.junit.Test;

public class StringToMatchTypeConverterTest {

    private StringToMatchTypeConverter target;

    @Before
    public void setUp() {
        target = new StringToMatchTypeConverter();
    }

    @Test
    public void testConvertStringTypeOnly() {
        MatchType matchType = target.convert("url");
        assertThat(matchType.getType()).isEqualByComparingTo(RateLimitType.URL);
        assertThat(matchType.getMatcher()).isNull();
    }

    @Test
    public void testConvertStringTypeWithMatcher() {
        MatchType matchType = target.convert("url=/api");
        assertThat(matchType.getType()).isEqualByComparingTo(RateLimitType.URL);
        assertThat(matchType.getMatcher()).isEqualTo("/api");
    }
}