package com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.X_FORWARDED_FOR_HEADER;

import com.google.common.collect.Lists;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.properties.RateLimitProperties;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.properties.RateLimitProperties.Policy;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.properties.RateLimitProperties.Policy.Type;
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
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        when(httpServletRequest.getRemoteAddr()).thenReturn("remote");
        properties = new RateLimitProperties();
        properties.setKeyPrefix("key-prefix");
        target = new DefaultRateLimitKeyGenerator(properties);
    }

    @Test
    public void testKeyEmptyTypes() {
        Policy policy = new Policy();
        policy.setType(Lists.newArrayList());

        String key = target.key(httpServletRequest, null, policy);
        assertThat(key).isEqualTo("key-prefix");
    }

    @Test
    public void testKeyUrlNullRoute() {
        Policy policy = new Policy();
        policy.setType(Lists.newArrayList(Type.URL));

        String key = target.key(httpServletRequest, null, policy);
        assertThat(key).isEqualTo("key-prefix");
    }

    @Test
    public void testKeyUrl() {
        Policy policy = new Policy();
        policy.setType(Lists.newArrayList(Type.URL));

        String key = target.key(httpServletRequest, route, policy);
        assertThat(key).isEqualTo("key-prefix:id:/**");
    }

    @Test
    public void testKeyOrigin() {
        Policy policy = new Policy();
        policy.setType(Lists.newArrayList(Type.ORIGIN));

        String key = target.key(httpServletRequest, route, policy);
        assertThat(key).isEqualTo("key-prefix:id:remote");
    }

    @Test
    public void testKeyOriginBehindProxyNullHeader() {
        Policy policy = new Policy();
        policy.setType(Lists.newArrayList(Type.ORIGIN));
        properties.setBehindProxy(true);

        String key = target.key(httpServletRequest, route, policy);
        assertThat(key).isEqualTo("key-prefix:id:remote");
    }

    @Test
    public void testKeyOriginBehindProxy() {
        Policy policy = new Policy();
        policy.setType(Lists.newArrayList(Type.ORIGIN));
        properties.setBehindProxy(true);
        when(httpServletRequest.getHeader(X_FORWARDED_FOR_HEADER)).thenReturn("headerAddress");

        String key = target.key(httpServletRequest, route, policy);
        assertThat(key).isEqualTo("key-prefix:id:headerAddress");
    }

    @Test
    public void testKeyUserNullPrincipal() {
        Policy policy = new Policy();
        policy.setType(Lists.newArrayList(Type.USER));

        String key = target.key(httpServletRequest, route, policy);
        assertThat(key).isEqualTo("key-prefix:id:anonymous");
    }

    @Test
    public void testKeyUser() {
        Policy policy = new Policy();
        policy.setType(Lists.newArrayList(Type.USER));
        when(httpServletRequest.getRemoteUser()).thenReturn("user");

        String key = target.key(httpServletRequest, route, policy);
        assertThat(key).isEqualTo("key-prefix:id:user");
    }
}