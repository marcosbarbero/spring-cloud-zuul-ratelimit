package com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.filters.pre;

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
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.filters.commons.TestRouteLocator;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.support.DefaultRateLimitUtils;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.support.RateLimitExceededEvent;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.support.RateLimitExceededException;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.monitoring.CounterFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class RateLimitPreFilterTest {

    final static String LOCALHOST = "127.0.0.1";

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
    protected ArgumentCaptor<RateLimitExceededEvent> rateLimitEventCaptor;

    private RateLimitProperties rateLimitProperties = new RateLimitProperties();

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        CounterFactory.initialize(new EmptyCounterFactory());

        when(httpServletRequest.getContextPath()).thenReturn("");
        when(httpServletRequest.getRequestURI()).thenReturn("/servicea/test");
        when(httpServletRequest.getRemoteAddr()).thenReturn(LOCALHOST);
        RequestContext requestContext = new RequestContext();
        requestContext.setRequest(httpServletRequest);
        requestContext.setResponse(httpServletResponse);
        RequestContext.testSetCurrentContext(requestContext);
        RequestContextHolder.setRequestAttributes(requestAttributes);
        rateLimitProperties = new RateLimitProperties();
        rateLimitProperties.setAddResponseHeaders(false);
        UrlPathHelper urlPathHelper = new UrlPathHelper();
        RateLimitUtils rateLimitUtils = new DefaultRateLimitUtils(rateLimitProperties);
        Route route = new Route("servicea", "/test", "servicea", "/servicea", null, Collections.emptySet());
        TestRouteLocator routeLocator = new TestRouteLocator(Collections.emptyList(), Lists.newArrayList(route));
        target = new RateLimitPreFilter(rateLimitProperties, routeLocator, urlPathHelper, rateLimiter, rateLimitKeyGenerator, rateLimitUtils, eventPublisher);
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
    public void testShouldNotFilterWhenByPassLocation() {
        rateLimitProperties.setEnabled(true);
        rateLimitProperties.getLocation().getBypass().add(LOCALHOST);
        assertThat(target.shouldFilter()).isEqualTo(false);
    }

    @Test
    public void testShouldDenyRequestLocation() {
        rateLimitProperties.setEnabled(true);
        rateLimitProperties.getLocation().getDeny().add(LOCALHOST);
        assertThrows(RateLimitExceededException.class, () -> target.shouldFilter());
    }

    @Test
    public void testShouldDeprecatedDenyRequest() {
        rateLimitProperties.setEnabled(true);
        rateLimitProperties.getDenyRequest().getOrigins().add(LOCALHOST);
        assertThrows(RateLimitExceededException.class, () -> target.shouldFilter());
    }

    @Test
    public void testShouldFireRateLimitEvent() {
        rateLimitProperties.setEnabled(true);

        Policy policy = new Policy();
        policy.setLimit(1L);
        MatchType matchType = new MatchType(RateLimitType.URL, "/test");
        policy.getType().add(matchType);
        rateLimitProperties.getPolicyList().put("servicea", Lists.newArrayList(policy));

        String key = "rate-limit-application_servicea_127.0.0.1";
        when(rateLimitKeyGenerator.key(any(), any(), eq(policy))).thenReturn(key);
        Rate rate = new Rate(key, -1L, null, 60L, null);
        when(rateLimiter.consume(policy, key, null)).thenReturn(rate);

        assertThat(target.shouldFilter()).isEqualTo(true);

        assertThrows(RateLimitExceededException.class,() -> target.run());

        verify(eventPublisher).publishEvent(rateLimitEventCaptor.capture());
        RateLimitExceededEvent rateLimitEvent = rateLimitEventCaptor.getValue();
        assertNotNull(rateLimitEvent);
        assertEquals(policy, rateLimitEvent.getPolicy());
    }
}