package com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.properties;

import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.support.DefaultRateLimitUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.cloud.netflix.zuul.filters.Route;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.concurrent.ThreadLocalRandom;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

public class RateLimitTypeTest {

    @Mock
    private HttpServletRequest httpServletRequest;
    private Route route = new Route("servicea", "/test", "servicea", "/servicea", null, Collections.emptySet());
    private DefaultRateLimitUtils rateLimitUtils;

    @BeforeEach
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
    public void applyPatternURL() {
        int id = ThreadLocalRandom.current().nextInt(0, 5000);

        when(httpServletRequest.getRequestURI()).thenReturn("/resource/" + id + "/specific");

        boolean apply = RateLimitType.URL_PATTERN.apply(httpServletRequest, route, rateLimitUtils, "/resource/*/specific");
        assertThat(apply).isTrue();
    }

    @Test
    public void keyPatternURL() {
        String pattern = "/resource/*/specific";
        String key = RateLimitType.URL_PATTERN.key(httpServletRequest, route, rateLimitUtils, pattern);
        assertThat(key).isEqualTo(pattern);
    }

    @Test
    public void applyPatternURL_withInvalidPattern_shouldNotApply() {
        when(httpServletRequest.getRequestURI()).thenReturn("/resource/abcd/specific");

        boolean apply = RateLimitType.URL_PATTERN.apply(httpServletRequest, route, rateLimitUtils, "/resource/??/specific");
        assertThat(apply).isFalse();
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

    @Test
    public void doNotApplyRoleWithoutMatcher() {
        assertThrows(UnsupportedOperationException.class, () -> RateLimitType.ROLE.apply(httpServletRequest, route, rateLimitUtils, null));
    }

    @Test
    public void applyMethod() {
        when(httpServletRequest.getMethod()).thenReturn("GET");

        boolean apply;
        apply = RateLimitType.HTTP_METHOD.apply(httpServletRequest, route, rateLimitUtils, "get");
        assertThat(apply).isTrue();
        apply = RateLimitType.HTTP_METHOD.apply(httpServletRequest, route, rateLimitUtils, "GET");
        assertThat(apply).isTrue();
    }

    @Test
    public void applyMethodNoMatch() {
        when(httpServletRequest.getMethod()).thenReturn("GET");

        boolean apply = RateLimitType.HTTP_METHOD.apply(httpServletRequest, route, rateLimitUtils, "POST");
        assertThat(apply).isFalse();
    }

    @Test
    public void keyMethod() {
        when(httpServletRequest.getMethod()).thenReturn("GET");

        String key = RateLimitType.HTTP_METHOD.key(httpServletRequest, route, rateLimitUtils, null);
        assertThat(key).isEqualTo("GET");
    }
}