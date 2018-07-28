package com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.X_FORWARDED_FOR_HEADER;

import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.properties.RateLimitProperties;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.properties.RateLimitProperties.Policy;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.properties.RateLimitProperties.Policy.MatchType;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.properties.RateLimitType;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.support.DefaultRateLimitKeyGenerator;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.support.DefaultRateLimitUtils;
import java.util.Collections;
import javax.servlet.http.HttpServletRequest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.cloud.netflix.zuul.filters.Route;

public class DefaultRateLimitKeyGeneratorTest {

    private DefaultRateLimitKeyGenerator target;

    @Mock
    private HttpServletRequest httpServletRequest;

    private Route route = new Route("id", "/**", null, "/id", null, Collections.emptySet());
    private RateLimitProperties properties;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        when(httpServletRequest.getRemoteAddr()).thenReturn("remote");
        properties = new RateLimitProperties();
        properties.setKeyPrefix("key-prefix");
        RateLimitUtils rateLimitUtils = new DefaultRateLimitUtils(properties);
        target = new DefaultRateLimitKeyGenerator(properties, rateLimitUtils);
    }

    @Test
    public void testKeyEmptyTypes() {
        Policy policy = new Policy();

        String key = target.key(httpServletRequest, null, policy);
        assertThat(key).isEqualTo("key-prefix");
    }

    @Test
    public void testKeyUrlNullRoute() {
        Policy policy = new Policy();
        policy.getType().add(new MatchType(RateLimitType.URL, null));

        String key = target.key(httpServletRequest, null, policy);
        assertThat(key).isEqualTo("key-prefix");
    }

    @Test
    public void testKeyUrl() {
        Policy policy = new Policy();
        policy.getType().add(new MatchType(RateLimitType.URL, null));

        String key = target.key(httpServletRequest, route, policy);
        assertThat(key).isEqualTo("key-prefix:id:/**");
    }

    @Test
    public void testKeyUrlWithMatcher() {
        Policy policy = new Policy();
        policy.getType().add(new MatchType(RateLimitType.URL, "matcherURL"));

        String key = target.key(httpServletRequest, route, policy);
        assertThat(key).isEqualTo("key-prefix:id:/**:matcherURL");
    }

    @Test
    public void testKeyOrigin() {
        Policy policy = new Policy();
        policy.getType().add(new MatchType(RateLimitType.ORIGIN, null));

        String key = target.key(httpServletRequest, route, policy);
        assertThat(key).isEqualTo("key-prefix:id:remote");
    }

    @Test
    public void testKeyOriginWithMatcher() {
        Policy policy = new Policy();
        policy.getType().add(new MatchType(RateLimitType.ORIGIN, "matcherOrigin"));

        String key = target.key(httpServletRequest, route, policy);
        assertThat(key).isEqualTo("key-prefix:id:remote:matcherOrigin");
    }

    @Test
    public void testKeyOriginBehindProxyNullHeader() {
        Policy policy = new Policy();
        policy.getType().add(new MatchType(RateLimitType.ORIGIN, null));
        properties.setBehindProxy(true);

        String key = target.key(httpServletRequest, route, policy);
        assertThat(key).isEqualTo("key-prefix:id:remote");
    }

    @Test
    public void testKeyOriginBehindProxy() {
        Policy policy = new Policy();
        policy.getType().add(new MatchType(RateLimitType.ORIGIN, null));
        properties.setBehindProxy(true);
        when(httpServletRequest.getHeader(X_FORWARDED_FOR_HEADER)).thenReturn("headerAddress");

        String key = target.key(httpServletRequest, route, policy);
        assertThat(key).isEqualTo("key-prefix:id:headerAddress");
    }

    @Test
    public void testKeyOriginBehindProxyWithMultipleXForwardedFor() {
        Policy policy = new Policy();
        policy.getType().add(new MatchType(RateLimitType.ORIGIN, null));
        properties.setBehindProxy(true);
        when(httpServletRequest.getHeader(X_FORWARDED_FOR_HEADER)).thenReturn("1stHeaderAddress, 2ndAddressHeader");

        String key = target.key(httpServletRequest, route, policy);
        assertThat(key).isEqualTo("key-prefix:id:1stHeaderAddress");
    }

    @Test
    public void testKeyUserNullPrincipal() {
        Policy policy = new Policy();
        policy.getType().add(new MatchType(RateLimitType.USER, null));

        String key = target.key(httpServletRequest, route, policy);
        assertThat(key).isEqualTo("key-prefix:id:anonymous");
    }

    @Test
    public void testKeyUser() {
        Policy policy = new Policy();
        policy.getType().add(new MatchType(RateLimitType.USER, null));
        when(httpServletRequest.getRemoteUser()).thenReturn("user");

        String key = target.key(httpServletRequest, route, policy);
        assertThat(key).isEqualTo("key-prefix:id:user");
    }

    @Test
    public void testKeyUserWithMatcher() {
        Policy policy = new Policy();
        policy.getType().add(new MatchType(RateLimitType.USER, "matcherUser"));
        when(httpServletRequest.getRemoteUser()).thenReturn("user");

        String key = target.key(httpServletRequest, route, policy);
        assertThat(key).isEqualTo("key-prefix:id:user:matcherUser");
    }
}