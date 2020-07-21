package com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.support.DefaultRateLimitUtils;
import java.util.Collections;
import javax.servlet.http.HttpServletRequest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.cloud.netflix.zuul.filters.Route;

public class RateLimitTypeTest {

    @Mock
    private HttpServletRequest httpServletRequest;
    private Route route = new Route("servicea", "/test", "servicea", "/servicea", null, Collections.emptySet());
    private DefaultRateLimitUtils rateLimitUtils;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        RateLimitProperties properties = new RateLimitProperties();
        rateLimitUtils = new DefaultRateLimitUtils(properties);
    }

    @Test
    public void applyUser() {
        when(httpServletRequest.getRemoteUser()).thenReturn("testUser");

        boolean apply = RateLimitType.USER.apply(httpServletRequest, route, rateLimitUtils, "testUser");
        assertThat(apply).isTrue();
    }

    @Test
    public void applyUserNoMatch() {
        when(httpServletRequest.getRemoteUser()).thenReturn("testUser");

        boolean apply = RateLimitType.USER.apply(httpServletRequest, route, rateLimitUtils, "otherUser");
        assertThat(apply).isFalse();
    }

    @Test
    public void keyUser() {
        when(httpServletRequest.getRemoteUser()).thenReturn("testUser");

        String key = RateLimitType.USER.key(httpServletRequest, route, rateLimitUtils, null);
        assertThat(key).isEqualTo("testUser");
    }

    @Test
    public void applyOrigin() {
        when(httpServletRequest.getRemoteAddr()).thenReturn("testAddr");

        boolean apply = RateLimitType.ORIGIN.apply(httpServletRequest, route, rateLimitUtils, "testAddr");
        assertThat(apply).isTrue();
    }

    @Test
    public void applyOriginInRange() {
        when(httpServletRequest.getRemoteAddr()).thenReturn("127.0.0.10");

        boolean apply = RateLimitType.ORIGIN.apply(httpServletRequest, route, rateLimitUtils, "127.0.0.8/29");
        assertThat(apply).isTrue();
    }

    @Test
    public void applyOriginNotInRange(){
        when(httpServletRequest.getRemoteAddr()).thenReturn("127.0.0.8");

        boolean apply = RateLimitType.ORIGIN.apply(httpServletRequest, route, rateLimitUtils, "127.0.0.10/29");
        assertThat(apply).isFalse();
    }

    @Test
    public void applyOriginNoMatch() {
        when(httpServletRequest.getRemoteAddr()).thenReturn("testAddr");

        boolean apply = RateLimitType.ORIGIN.apply(httpServletRequest, route, rateLimitUtils, "otherAddr");
        assertThat(apply).isFalse();
    }

    @Test
    public void keyOrigin() {
        when(httpServletRequest.getRemoteAddr()).thenReturn("testAddr");

        String key = RateLimitType.ORIGIN.key(httpServletRequest, route, rateLimitUtils, null);
        assertThat(key).isEqualTo("testAddr");
    }

    @Test
    public void applyURL() {
        boolean apply = RateLimitType.URL.apply(httpServletRequest, route, rateLimitUtils, "/test");
        assertThat(apply).isTrue();
    }

    @Test
    public void applyURLNoMatch() {
        boolean apply = RateLimitType.URL.apply(httpServletRequest, route, rateLimitUtils, "/other");
        assertThat(apply).isFalse();
    }

    @Test
    public void keyURL() {
        String key = RateLimitType.URL.key(httpServletRequest, route, rateLimitUtils, null);
        assertThat(key).isEqualTo("/test");
    }

    @Test(expected = UnsupportedOperationException.class)
    public void doNotApplyRoleWithoutMatcher() {
        RateLimitType.ROLE.apply(httpServletRequest, route, rateLimitUtils, null);
    }

    @Test
    public void applyMethod() {
        when(httpServletRequest.getMethod()).thenReturn("GET");

        boolean apply;
        apply = RateLimitType.HTTPMETHOD.apply(httpServletRequest, route, rateLimitUtils, "get");
        assertThat(apply).isTrue();
        apply = RateLimitType.HTTPMETHOD.apply(httpServletRequest, route, rateLimitUtils, "GET");
        assertThat(apply).isTrue();
    }

    @Test
    public void applyMethodNoMatch() {
        when(httpServletRequest.getMethod()).thenReturn("GET");

        boolean apply = RateLimitType.HTTPMETHOD.apply(httpServletRequest, route, rateLimitUtils, "POST");
        assertThat(apply).isFalse();
    }

    @Test
    public void keyMethod() {
        when(httpServletRequest.getMethod()).thenReturn("GET");

        String key = RateLimitType.HTTPMETHOD.key(httpServletRequest, route, rateLimitUtils, null);
        assertThat(key).isEqualTo("GET");
    }

    @Test
    public void applyHeader() {
        when(httpServletRequest.getHeader("customHeader")).thenReturn("customValue");

        boolean apply = RateLimitType.HTTP_HEADER.apply(httpServletRequest, route, rateLimitUtils, "customHeader");
        assertThat(apply).isTrue();
    }

    @Test
    public void applyHeaderNoMatch() {
        when(httpServletRequest.getHeader("customHeader")).thenReturn("customValue");

        boolean apply = RateLimitType.HTTP_HEADER.apply(httpServletRequest, route, rateLimitUtils, "customHeader2");
        assertThat(apply).isFalse();
    }

    @Test
    public void keyHeader() {
        when(httpServletRequest.getHeader("customHeader")).thenReturn("customValue");

        String key = RateLimitType.HTTP_HEADER.key(httpServletRequest, route, rateLimitUtils, "customHeader");
        assertThat(key).isEqualTo("customValue");
    }
}