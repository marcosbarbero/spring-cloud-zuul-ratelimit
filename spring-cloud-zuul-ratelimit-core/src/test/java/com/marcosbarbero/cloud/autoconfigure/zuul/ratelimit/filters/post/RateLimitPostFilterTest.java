package com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.filters.post;

import static org.assertj.core.api.Assertions.assertThat;

import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.RateLimitKeyGenerator;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.RateLimiter;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.properties.RateLimitProperties;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.filters.RateLimitPostFilter;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.cloud.netflix.zuul.filters.RouteLocator;
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;
import org.springframework.web.util.UrlPathHelper;

public class RateLimitPostFilterTest {

    private RateLimitPostFilter target;

    @Mock
    private RouteLocator routeLocator;
    @Mock
    private RateLimiter rateLimiter;
    @Mock
    private RateLimitKeyGenerator rateLimitKeyGenerator;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        RateLimitProperties rateLimitProperties = new RateLimitProperties();
        UrlPathHelper urlPathHelper = new UrlPathHelper();
        target = new RateLimitPostFilter(rateLimitProperties, routeLocator, urlPathHelper, rateLimiter, rateLimitKeyGenerator);
    }

    @Test
    public void testFilterType() {
        assertThat(target.filterType()).isEqualTo(FilterConstants.POST_TYPE);
    }

    @Test
    public void testFilterOrder() {
        assertThat(target.filterOrder()).isEqualTo(FilterConstants.SEND_RESPONSE_FILTER_ORDER - 10);
    }

    @Test
    public void testShouldFilterOnDisabledProperty() {
        assertThat(target.shouldFilter()).isEqualTo(false);
    }
}