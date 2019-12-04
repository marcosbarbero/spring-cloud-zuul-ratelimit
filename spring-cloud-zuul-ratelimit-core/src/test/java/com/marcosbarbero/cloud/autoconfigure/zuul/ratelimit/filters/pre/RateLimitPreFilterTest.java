package com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.filters.pre;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.collect.Lists;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.Rate;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.RateLimitKeyGenerator;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.RateLimitUtils;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.RateLimiter;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.properties.RateLimitProperties;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.properties.RateLimitProperties.Policy;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.properties.RateLimitProperties.Policy.MatchType;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.properties.RateLimitType;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.filters.RateLimitPreFilter;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.filters.RateLimitPreFilter.RateLimitEvent;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.filters.commons.TestRouteLocator;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.support.DefaultRateLimitUtils;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.support.RateLimitExceededException;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.monitoring.CounterFactory;
import java.util.Collections;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.cloud.netflix.zuul.filters.Route;
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;
import org.springframework.cloud.netflix.zuul.metrics.EmptyCounterFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.util.UrlPathHelper;

public class RateLimitPreFilterTest {

    private RateLimitPreFilter target;

    @Mock
    private RateLimiter rateLimiter;
    @Mock
    private RateLimitKeyGenerator rateLimitKeyGenerator;
    @Mock
    private RequestAttributes requestAttributes;
    @Mock
    private HttpServletRequest httpServletRequest;
    @Mock
    private HttpServletResponse httpServletResponse;
    @Mock
    private ApplicationEventPublisher eventPublisher;
    @Captor
    protected ArgumentCaptor<RateLimitEvent> rateLimitEventCaptor;

    private RateLimitProperties rateLimitProperties = new RateLimitProperties();

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        CounterFactory.initialize(new EmptyCounterFactory());
        
        when(this.httpServletRequest.getContextPath()).thenReturn("");
        when(this.httpServletRequest.getRequestURI()).thenReturn("/servicea/test");
        RequestContext requestContext = new RequestContext();
        requestContext.setRequest(this.httpServletRequest);
        requestContext.setResponse(this.httpServletResponse);
        RequestContext.testSetCurrentContext(requestContext);
        RequestContextHolder.setRequestAttributes(this.requestAttributes);
        this.rateLimitProperties = new RateLimitProperties();
        this.rateLimitProperties.setAddResponseHeaders(false);
        UrlPathHelper urlPathHelper = new UrlPathHelper();
        RateLimitUtils rateLimitUtils = new DefaultRateLimitUtils(this.rateLimitProperties);
        Route route = new Route("servicea", "/test", "servicea", "/servicea", null, Collections.emptySet());
        TestRouteLocator routeLocator = new TestRouteLocator(Collections.emptyList(), Lists.newArrayList(route));
        this.target = new RateLimitPreFilter(this.rateLimitProperties, routeLocator, urlPathHelper, this.rateLimiter, this.rateLimitKeyGenerator, rateLimitUtils, this.eventPublisher);
    }

    @Test
    public void testFilterType() {
        assertThat(target.filterType()).isEqualTo(FilterConstants.PRE_TYPE);
    }

    @Test
    public void testFilterOrder() {
        assertThat(target.filterOrder()).isEqualTo(FilterConstants.FORM_BODY_WRAPPER_FILTER_ORDER);
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
    public void testShouldFilterOnNonMatchingPolicyType() {
        rateLimitProperties.setEnabled(true);
        Policy servicebPolicy = new Policy();
        MatchType matchType = new MatchType(RateLimitType.URL, "other");
        servicebPolicy.getType().add(matchType);
        rateLimitProperties.getPolicyList().put("servicea", Lists.newArrayList(servicebPolicy));

        assertThat(target.shouldFilter()).isEqualTo(false);
    }

    @Test
    public void testShouldFilter() {
        rateLimitProperties.setEnabled(true);
        Policy defaultPolicy = new Policy();
        rateLimitProperties.setDefaultPolicyList(Lists.newArrayList(defaultPolicy));

        assertThat(target.shouldFilter()).isEqualTo(true);
    }

    @Test
    public void testShouldFireRateLimitEvent() {
        this.rateLimitProperties.setEnabled(true);

        Policy policy = new Policy();
        policy.setLimit(1L);
        MatchType matchType = new MatchType(RateLimitType.URL, "/test");
        policy.getType().add(matchType);
        this.rateLimitProperties.getPolicyList().put("servicea", Lists.newArrayList(policy));

        String key = "rate-limit-application_servicea_127.0.0.1";
        when(this.rateLimitKeyGenerator.key(any(), any(), eq(policy))).thenReturn(key);
        Rate rate = new Rate(key, -1L, null, 60L, null);
        when(this.rateLimiter.consume(policy, key, null)).thenReturn(rate);

        assertThat(target.shouldFilter()).isEqualTo(true);

        assertThrows(RateLimitExceededException.class,() -> this.target.run());

        verify(this.eventPublisher).publishEvent(this.rateLimitEventCaptor.capture());
        RateLimitEvent rateLimitEvent = this.rateLimitEventCaptor.getValue();
        assertNotNull(rateLimitEvent);
        assertEquals(policy, rateLimitEvent.getPolicy());
    }
}