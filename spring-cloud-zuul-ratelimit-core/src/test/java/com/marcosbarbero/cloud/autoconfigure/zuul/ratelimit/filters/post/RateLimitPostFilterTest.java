package com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.filters.post;

import static com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.support.RateLimitConstants.REQUEST_START_TIME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import com.google.common.collect.Lists;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.RateLimitKeyGenerator;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.RateLimitUtils;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.RateLimiter;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.properties.RateLimitProperties;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.properties.RateLimitProperties.Policy;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.filters.RateLimitPostFilter;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.support.DefaultRateLimitUtils;
import com.netflix.zuul.context.RequestContext;
import javax.servlet.http.HttpServletRequest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.cloud.netflix.zuul.filters.RouteLocator;
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.util.UrlPathHelper;

public class RateLimitPostFilterTest {

    private RateLimitPostFilter target;

    @Mock
    private RouteLocator routeLocator;
    @Mock
    private RateLimiter rateLimiter;
    @Mock
    private RateLimitKeyGenerator rateLimitKeyGenerator;
    @Mock
    private RequestAttributes requestAttributes;
    @Mock
    private HttpServletRequest httpServletRequest;

    private RateLimitProperties rateLimitProperties = new RateLimitProperties();

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        when(httpServletRequest.getContextPath()).thenReturn("/servicea/test");
        when(httpServletRequest.getRequestURI()).thenReturn("/servicea/test");
        RequestContext requestContext = new RequestContext();
        requestContext.setRequest(httpServletRequest);
        RequestContext.testSetCurrentContext(requestContext);
        RequestContextHolder.setRequestAttributes(requestAttributes);
        rateLimitProperties = new RateLimitProperties();
        UrlPathHelper urlPathHelper = new UrlPathHelper();
        RateLimitUtils rateLimitUtils = new DefaultRateLimitUtils(rateLimitProperties);
        target = new RateLimitPostFilter(rateLimitProperties, routeLocator, urlPathHelper, rateLimiter, rateLimitKeyGenerator, rateLimitUtils);
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

    @Test
    public void testShouldFilterOnNoPolicy() {
        rateLimitProperties.setEnabled(true);

        assertThat(target.shouldFilter()).isEqualTo(false);
    }

    @Test
    public void testShouldFilterOnNullStartTime() {
        rateLimitProperties.setEnabled(true);
        Policy defaultPolicy = new Policy();
        rateLimitProperties.setDefaultPolicy(defaultPolicy);

        assertThat(target.shouldFilter()).isEqualTo(false);
    }

    @Test
    public void testShouldFilter() {
        rateLimitProperties.setEnabled(true);
        when(httpServletRequest.getAttribute(REQUEST_START_TIME)).thenReturn(System.currentTimeMillis());
        Policy defaultPolicy = new Policy();
        rateLimitProperties.setDefaultPolicyList(Lists.newArrayList(defaultPolicy));

        assertThat(target.shouldFilter()).isEqualTo(true);
    }

    @Test
    public void testRunNoPolicy() {
        target.run();
        verifyZeroInteractions(rateLimiter);
    }

    @Test
    public void testRun() {
        rateLimitProperties.setEnabled(true);
        when(httpServletRequest.getAttribute(REQUEST_START_TIME)).thenReturn(System.currentTimeMillis());
        Policy defaultPolicy = new Policy();
        defaultPolicy.setQuota(2L);
        rateLimitProperties.setDefaultPolicyList(Lists.newArrayList(defaultPolicy));
        when(rateLimitKeyGenerator.key(any(), any(), any())).thenReturn("generatedKey");

        target.run();
        verify(rateLimiter).consume(eq(defaultPolicy), eq("generatedKey"), anyLong());
    }
}