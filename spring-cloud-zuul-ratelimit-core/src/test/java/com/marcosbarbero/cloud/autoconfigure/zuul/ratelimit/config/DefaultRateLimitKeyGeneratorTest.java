package com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config;

import com.google.common.collect.Maps;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.properties.RateLimitProperties;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.properties.RateLimitProperties.Policy;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.properties.RateLimitProperties.Policy.Type;
import com.netflix.zuul.context.RequestContext;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.cloud.netflix.zuul.filters.Route;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.LinkedHashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.X_FORWARDED_FOR_HEADER;

public class DefaultRateLimitKeyGeneratorTest {

    private DefaultRateLimitKeyGenerator target;

    @Mock
    private HttpServletRequest httpServletRequest;

    private Route route = new Route("id", "/**", null, "/id", null, Collections.emptySet());
    private RateLimitProperties properties;
    private UserIDGenerator userIDGenerator;
    private RequestContext context = new RequestContext();

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        when(httpServletRequest.getRemoteAddr()).thenReturn("remote");
        properties = new RateLimitProperties();
        userIDGenerator = new DefaultUserIDGenerator();
        properties.setKeyPrefix("key-prefix");
        context.setRequest(httpServletRequest);
        target = new DefaultRateLimitKeyGenerator(userIDGenerator, properties);
    }

    @Test
    public void testKeyEmptyTypes() {
        Policy policy = new Policy();
        policy.setTypes(Maps.newLinkedHashMap());

        String key = target.key(context, null, policy);
        assertThat(key).isEqualTo("key-prefix");
    }

    @Test
    public void testKeyName() {
        Policy policy = new Policy();
        policy.setName("policy-name");
        LinkedHashMap<Type, String> map = Maps.newLinkedHashMap();
        policy.setTypes(map);

        String key = target.key(context, null, policy);
        assertThat(key).isEqualTo("key-prefix:policy-name");
    }

    @Test
    public void testKeyNameUrl() {
        Policy policy = new Policy();
        policy.setName("policy-name");
        LinkedHashMap<Type, String> map = Maps.newLinkedHashMap();
        map.put(Type.URL, "/**");
        policy.setTypes(map);

        String key = target.key(context, null, policy);
        assertThat(key).isEqualTo("key-prefix:policy-name");
    }

    @Test
    public void testKeyNotNameUrl() {
        Policy policy = new Policy();
        LinkedHashMap<Type, String> map = Maps.newLinkedHashMap();
        map.put(Type.URL, "/**");
        policy.setTypes(map);

        String key = target.key(context, null, policy);
        assertThat(key).isEqualTo("key-prefix:/**");
    }

    @Test
    public void testKeyUrlNullRoute() {
        Policy policy = new Policy();
        LinkedHashMap<Type, String> map = Maps.newLinkedHashMap();
        when(httpServletRequest.getRequestURI()).thenReturn("test");
        map.put(Type.URL, "");
        policy.setTypes(map);

        String key = target.key(context, null, policy);
        assertThat(key).isEqualTo("key-prefix:test");
    }

    @Test
    public void testRouteKeyUrl() {
        Policy policy = new Policy();
        when(httpServletRequest.getRequestURI()).thenReturn("/**");
        LinkedHashMap<Type, String> map = Maps.newLinkedHashMap();
        map.put(Type.ROUTE, "");
        map.put(Type.URL, "");
        policy.setTypes(map);

        String key = target.key(context, route, policy);
        assertThat(key).isEqualTo("key-prefix:id:/**");
    }

    @Test
    public void testKeyRouteOrigin() {
        Policy policy = new Policy();
        LinkedHashMap<Type, String> map = Maps.newLinkedHashMap();
        map.put(Type.ROUTE, "");
        map.put(Type.ORIGIN, "");
        policy.setTypes(map);

        String key = target.key(context, route, policy);
        assertThat(key).isEqualTo("key-prefix:id:remote");
    }

    @Test
    public void testRouteKeyOriginBehindProxyNullHeader() {
        Policy policy = new Policy();
        LinkedHashMap<Type, String> map = Maps.newLinkedHashMap();
        map.put(Type.ROUTE, "");
        map.put(Type.ORIGIN, "");
        policy.setTypes(map);
        properties.setBehindProxy(true);

        String key = target.key(context, route, policy);
        assertThat(key).isEqualTo("key-prefix:id:remote");
    }

    @Test
    public void testKeyOriginBehindProxyNullHeader() {
        Policy policy = new Policy();
        LinkedHashMap<Type, String> map = Maps.newLinkedHashMap();
        map.put(Type.ORIGIN, "");
        policy.setTypes(map);
        properties.setBehindProxy(true);

        String key = target.key(context, route, policy);
        assertThat(key).isEqualTo("key-prefix:remote");
    }

    @Test
    public void testRouteKeyOriginBehindProxy() {
        Policy policy = new Policy();
        LinkedHashMap<Type, String> map = Maps.newLinkedHashMap();
        map.put(Type.ROUTE, "");
        map.put(Type.ORIGIN, "");
        policy.setTypes(map);
        properties.setBehindProxy(true);
        when(httpServletRequest.getHeader(X_FORWARDED_FOR_HEADER)).thenReturn("headerAddress");

        String key = target.key(context, route, policy);
        assertThat(key).isEqualTo("key-prefix:id:headerAddress");
    }

    @Test
    public void testKeyOriginBehindProxy() {
        Policy policy = new Policy();
        LinkedHashMap<Type, String> map = Maps.newLinkedHashMap();
        map.put(Type.ORIGIN, "");
        policy.setTypes(map);
        properties.setBehindProxy(true);
        when(httpServletRequest.getHeader(X_FORWARDED_FOR_HEADER)).thenReturn("headerAddress");

        String key = target.key(context, route, policy);
        assertThat(key).isEqualTo("key-prefix:headerAddress");
    }

    @Test
    public void testRouteKeyUserNullPrincipal() {
        Policy policy = new Policy();
        LinkedHashMap<Type, String> map = Maps.newLinkedHashMap();
        map.put(Type.ROUTE, "");
        map.put(Type.USER, "");
        policy.setTypes(map);

        String key = target.key(context, route, policy);
        assertThat(key).isEqualTo("key-prefix:id:anonymous");
    }

    @Test
    public void testKeyUserNullPrincipal() {
        Policy policy = new Policy();
        LinkedHashMap<Type, String> map = Maps.newLinkedHashMap();
        map.put(Type.USER, "");
        policy.setTypes(map);

        String key = target.key(context, route, policy);
        assertThat(key).isEqualTo("key-prefix:anonymous");
    }

    @Test
    public void testKeyRouteUser() {
        Policy policy = new Policy();
        LinkedHashMap<Type, String> map = Maps.newLinkedHashMap();
        map.put(Type.ROUTE, "");
        map.put(Type.USER, "");
        policy.setTypes(map);
        when(httpServletRequest.getRemoteUser()).thenReturn("user");

        String key = target.key(context, route, policy);
        assertThat(key).isEqualTo("key-prefix:id:user");
    }

    @Test
    public void testKeyUser() {
        Policy policy = new Policy();
        LinkedHashMap<Type, String> map = Maps.newLinkedHashMap();
        map.put(Type.USER, "");
        policy.setTypes(map);
        when(httpServletRequest.getRemoteUser()).thenReturn("user");

        String key = target.key(context, route, policy);
        assertThat(key).isEqualTo("key-prefix:user");
    }
}