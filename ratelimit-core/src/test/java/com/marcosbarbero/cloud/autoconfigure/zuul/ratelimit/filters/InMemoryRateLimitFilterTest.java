package com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.filters;

import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.Policy;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.RateLimitProperties;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.RateLimiter;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.repository.InMemoryRateLimiter;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.filters.commons.TestRouteLocator;
import com.netflix.zuul.context.RequestContext;

import org.junit.Before;
import org.junit.Test;
import org.springframework.cloud.netflix.zuul.filters.Route;
import org.springframework.cloud.netflix.zuul.filters.RouteLocator;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.springframework.http.HttpStatus.TOO_MANY_REQUESTS;

/**
 * @author Marcos Barbero
 * @since 2017-06-23
 */
public class InMemoryRateLimitFilterTest {

    private RateLimitFilter filter;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    private RequestContext context = RequestContext.getCurrentContext();
    private RateLimiter rateLimiter = new InMemoryRateLimiter();

    private Route createRoute(String id, String path) {
        return new Route(id, path, null, null, false, Collections.emptySet());
    }

    private RateLimitProperties properties() {
        RateLimitProperties properties = new RateLimitProperties();
        properties.setEnabled(true);
        properties.setBehindProxy(false);

        Map<String, Policy> policies = new HashMap<>();

        Policy policy = new Policy();
        policy.setLimit(2L);
        policy.setRefreshInterval(15L);
        policy.setType(asList(Policy.Type.ORIGIN));

        policies.put("serviceA", policy);
        properties.setPolicies(policies);

        return properties;
    }

    @Before
    public void setUp() {
        RouteLocator routeLocator = new TestRouteLocator(asList("ignored"),
                asList(createRoute("serviceA", "/serviceA"), createRoute("serviceB", "/serviceB")));

        this.request = new MockHttpServletRequest();
        this.response = new MockHttpServletResponse();
        this.filter = new RateLimitFilter(this.rateLimiter, this.properties(), routeLocator);
        this.context.clear();
        this.context.setRequest(this.request);
        this.context.setResponse(this.response);
    }

    @Test
    public void testRateLimit() throws Exception {
        this.request.setRequestURI("/serviceA");
        this.request.setRemoteAddr("10.0.0.100");

        assertTrue(this.filter.shouldFilter());

        this.filter.run();

        TimeUnit.SECONDS.sleep(5);

        this.filter.run();

        String remaining = this.response.getHeader(RateLimitFilter.Headers.REMAINING);
        assertEquals(remaining, "0");

        TimeUnit.SECONDS.sleep(10);

        this.filter.run();
        remaining = this.response.getHeader(RateLimitFilter.Headers.REMAINING);
        assertEquals(remaining, "1");
    }

    @Test
    public void testRateLimitExceedCapacity() throws Exception {
        this.request.setRequestURI("/serviceA");
        this.request.setRemoteAddr("10.0.0.100");

        assertTrue(this.filter.shouldFilter());

        try {
            for (int i = 0; i <= 3; i++) {
                this.filter.run();
            }
        } catch (Exception e) {
            // do nothing
        }

        String exceeded = (String) this.context.get("rateLimitExceeded");
        assertTrue("RateLimit Exceeded", Boolean.valueOf(exceeded));
        assertEquals("Too many requests", this.context.getResponseStatusCode(), TOO_MANY_REQUESTS.value());
    }

    @Test
    public void testNoRateLimit() throws Exception {
        this.request.setRequestURI("/serviceB");
        this.request.setRemoteAddr("127.0.0.1");
        assertFalse(this.filter.shouldFilter());
    }


}
