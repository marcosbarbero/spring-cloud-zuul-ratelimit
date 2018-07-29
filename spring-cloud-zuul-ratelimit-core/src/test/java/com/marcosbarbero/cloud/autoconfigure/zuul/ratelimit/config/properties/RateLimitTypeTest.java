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

        String key = RateLimitType.USER.key(httpServletRequest, route, rateLimitUtils);
        assertThat(key).isEqualTo("testUser");
    }

    @Test
    public void applyOrigin() {
        when(httpServletRequest.getRemoteAddr()).thenReturn("testAddr");

        boolean apply = RateLimitType.ORIGIN.apply(httpServletRequest, route, rateLimitUtils, "testAddr");
        assertThat(apply).isTrue();
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

        String key = RateLimitType.ORIGIN.key(httpServletRequest, route, rateLimitUtils);
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
        String key = RateLimitType.URL.key(httpServletRequest, route, rateLimitUtils);
        assertThat(key).isEqualTo("/test");
    }
}