package com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.filters.pre;

import static com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.support.RateLimitConstants.HEADER_LIMIT;
import static com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.support.RateLimitConstants.HEADER_QUOTA;
import static com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.support.RateLimitConstants.HEADER_REMAINING;
import static com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.support.RateLimitConstants.HEADER_REMAINING_QUOTA;
import static com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.support.RateLimitConstants.HEADER_RESET;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.HttpStatus.TOO_MANY_REQUESTS;

import com.google.common.collect.Lists;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.RateLimitKeyGenerator;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.RateLimitUtils;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.RateLimiter;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.properties.RateLimitProperties;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.properties.RateLimitProperties.Policy;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.properties.RateLimitProperties.Policy.MatchType;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.properties.RateLimitType;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.properties.ResponseHeadersVerbosity;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.filters.RateLimitPreFilter;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.filters.commons.TestRouteLocator;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.support.DefaultRateLimitKeyGenerator;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.support.DefaultRateLimitUtils;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.monitoring.CounterFactory;
import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.cloud.netflix.zuul.filters.Route;
import org.springframework.cloud.netflix.zuul.filters.RouteLocator;
import org.springframework.cloud.netflix.zuul.metrics.EmptyCounterFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.util.UrlPathHelper;

/**
 * @author Marcos Barbero
 * @since 2017-06-30
 */
public abstract class BaseRateLimitPreFilterTest {
    @Mock
    private RequestAttributes requestAttributes;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    MockHttpServletRequest request;
    MockHttpServletResponse response;

    RateLimitPreFilter filter;
    RateLimitProperties properties;

    private RequestContext context;
    private RateLimiter rateLimiter;

    private Route createRoute(String id) {
        return new Route(id, "", id, "/" + id, null, Collections.emptySet());
    }

    private RateLimitProperties properties() {
        RateLimitProperties properties = new RateLimitProperties();
        properties.setEnabled(true);
        properties.setBehindProxy(true);

        Map<String, List<Policy>> policies = new HashMap<>();

        Policy policy = new Policy();
        policy.setLimit(2L);
        policy.setQuota(Duration.ofSeconds(2));
        policy.setRefreshInterval(Duration.ofSeconds(2));
        policy.getType().add(new MatchType(RateLimitType.ORIGIN, null));
        policy.getType().add(new MatchType(RateLimitType.URL, null));
        policy.getType().add(new MatchType(RateLimitType.USER, null));
        policy.getType().add(new MatchType(RateLimitType.HTTP_METHOD, null));

        policies.put("serviceA", Lists.newArrayList(policy));
        properties.setPolicyList(policies);

        return properties;
    }

    private RouteLocator routeLocator() {
        return new TestRouteLocator(Collections.singletonList("ignored"),
            asList(createRoute("serviceA"), createRoute("serviceB")));
    }

    void setRateLimiter(RateLimiter rateLimiter) {
        this.rateLimiter = rateLimiter;
    }

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        CounterFactory.initialize(new EmptyCounterFactory());
        this.request = new MockHttpServletRequest();
        this.response = new MockHttpServletResponse();
        properties = this.properties();
        RateLimitUtils rateLimitUtils = new DefaultRateLimitUtils(properties);
        RateLimitKeyGenerator rateLimitKeyGenerator = new DefaultRateLimitKeyGenerator(properties,
            rateLimitUtils);
        UrlPathHelper urlPathHelper = new UrlPathHelper();
        this.filter = new RateLimitPreFilter(properties, this.routeLocator(), urlPathHelper, this.rateLimiter,
            rateLimitKeyGenerator, rateLimitUtils, eventPublisher);
        this.context = new RequestContext();
        RequestContext.testSetCurrentContext(this.context);
        RequestContextHolder.setRequestAttributes(requestAttributes);
        this.context.clear();
        this.context.setRequest(this.request);
        this.context.setResponse(this.response);
    }

    @Test
    public void testRateLimit() throws Exception {
        this.request.setRequestURI("/serviceA");
        this.request.setRemoteAddr("10.0.0.100");
        this.request.setMethod("GET");

        assertTrue(this.filter.shouldFilter());

        for (int i = 0; i < 2; i++) {
            this.filter.run();
        }

        String key = "-null_serviceA_10.0.0.100_anonymous_GET";
        String remaining = this.response.getHeader(HEADER_REMAINING + key);
        assertEquals("0", remaining);

        TimeUnit.SECONDS.sleep(3);

        this.filter.run();
        remaining = this.response.getHeader(HEADER_REMAINING + key);
        assertEquals("1", remaining);
    }

    @Test
    public void testRateLimitExceedCapacity() throws Exception {
        this.request.setRequestURI("/serviceA");
        this.request.setRemoteAddr("10.0.0.100");
        this.request.addHeader("X-FORWARDED-FOR", "10.0.0.1");
        this.request.setMethod("GET");

        assertTrue(this.filter.shouldFilter());

        try {
            for (int i = 0; i <= 3; i++) {
                this.filter.run();
            }
        } catch (Exception e) {
            // do nothing
        }

        String exceeded = (String) this.context.get("rateLimitExceeded");
        assertTrue(Boolean.valueOf(exceeded), "RateLimit Exceeded");
        assertEquals(TOO_MANY_REQUESTS.value(), this.context.getResponseStatusCode(), "Too many requests");
    }

    @Test
    public void testNoRateLimitService() {
        this.request.setRequestURI("/serviceZ");
        this.request.setRemoteAddr("10.0.0.100");
        this.request.setMethod("GET");

        assertFalse(this.filter.shouldFilter());

        try {
            for (int i = 0; i <= 3; i++) {
                this.filter.run();
            }
        } catch (Exception e) {
            // do nothing
        }

        String exceeded = (String) this.context.get("rateLimitExceeded");
        assertFalse(Boolean.valueOf(exceeded), "RateLimit not exceeded");
    }

    @Test
    public void testNoRateLimit() {
        this.request.setRequestURI("/serviceB");
        this.request.setRemoteAddr("127.0.0.1");
        this.request.setMethod("GET");
        assertFalse(this.filter.shouldFilter());
    }

    @ParameterizedTest
    @EnumSource(value = ResponseHeadersVerbosity.class)
    void testReturnHeaders(ResponseHeadersVerbosity verbosity) {
        request.setRequestURI("/serviceA");
        request.setRemoteAddr("10.0.0.100");
        request.setMethod("GET");

        properties.setResponseHeaders(verbosity);

        assertTrue(this.filter.shouldFilter());

        String key = "-null_serviceA_10.0.0.100_anonymous_GET";

        for (int i = 0; i < 2; i++) {
            this.filter.run();
            assertHeaders(key, verbosity);
        }
    }

    void assertHeaders(final String key, ResponseHeadersVerbosity headersVerbosity) {
        Collection<String> headerNames = response.getHeaderNames();

        switch (headersVerbosity) {
        case NONE:
          assertTrue(headerNames.stream().noneMatch(header -> header.startsWith("X-RateLimit-")), "There should be no rate-limit headers");
          break;
        case STANDARD:
          assertAll("Rate-limit headers should not contain any other information",
              () -> headerNames.contains(HEADER_QUOTA),
              () -> headerNames.contains(HEADER_REMAINING_QUOTA),
              () -> headerNames.contains(HEADER_LIMIT),
              () -> headerNames.contains(HEADER_REMAINING),
              () -> headerNames.contains(HEADER_RESET)
          );
          break;
        case VERBOSE:
          assertAll("Rate-limit headers should contain key information",
              () -> headerNames.contains(HEADER_QUOTA + key),
              () -> headerNames.contains(HEADER_REMAINING_QUOTA + key),
              () -> headerNames.contains(HEADER_LIMIT + key),
              () -> headerNames.contains(HEADER_REMAINING + key),
              () -> headerNames.contains(HEADER_RESET + key)
          );
        }
    }
}
