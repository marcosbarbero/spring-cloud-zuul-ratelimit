package com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.X_FORWARDED_FOR_HEADER;

import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.properties.RateLimitProperties;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.properties.RateLimitProperties.Policy;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.properties.RateLimitProperties.Policy.MatchType;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.properties.RateLimitProperties.Policy.Type;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.support.RateLimitUtils;
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
        RateLimitUtils rateLimitUtils = new RateLimitUtils(properties);
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
        policy.getType().add(new MatchType(Type.URL, null));

        String key = target.key(httpServletRequest, null, policy);
        assertThat(key).isEqualTo("key-prefix");
    }

    @Test
    public void testKeyUrl() {
        Policy policy = new Policy();
        policy.getType().add(new MatchType(Type.URL, null));

        String key = target.key(httpServletRequest, route, policy);
        assertThat(key).isEqualTo("key-prefix:id:/**");
    }

    @Test
    public void testKeyOrigin() {
        Policy policy = new Policy();
        policy.getType().add(new MatchType(Type.ORIGIN, null));

        String key = target.key(httpServletRequest, route, policy);
        assertThat(key).isEqualTo("key-prefix:id:remote");
    }

    @Test
    public void testKeyOriginBehindProxyNullHeader() {
        Policy policy = new Policy();
        policy.getType().add(new MatchType(Type.ORIGIN, null));
        properties.setBehindProxy(true);

        String key = target.key(httpServletRequest, route, policy);
        assertThat(key).isEqualTo("key-prefix:id:remote");
    }

    @Test
    public void testKeyOriginBehindProxy() {
        Policy policy = new Policy();
        policy.getType().add(new MatchType(Type.ORIGIN, null));
        properties.setBehindProxy(true);
        when(httpServletRequest.getHeader(X_FORWARDED_FOR_HEADER)).thenReturn("headerAddress");

        String key = target.key(httpServletRequest, route, policy);
        assertThat(key).isEqualTo("key-prefix:id:headerAddress");
    }

    @Test
    public void testKeyUserNullPrincipal() {
        Policy policy = new Policy();
        policy.getType().add(new MatchType(Type.USER, null));

        String key = target.key(httpServletRequest, route, policy);
        assertThat(key).isEqualTo("key-prefix:id:anonymous");
    }

    @Test
    public void testKeyUser() {
        Policy policy = new Policy();
        policy.getType().add(new MatchType(Type.USER, null));
        when(httpServletRequest.getRemoteUser()).thenReturn("user");

        String key = target.key(httpServletRequest, route, policy);
        assertThat(key).isEqualTo("key-prefix:id:user");
    }
}