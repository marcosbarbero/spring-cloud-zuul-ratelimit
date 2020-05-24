package com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.properties;

import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.RateLimitAutoConfiguration;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.RateLimitUtils;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.support.OAuth2SecuredRateLimitUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.netflix.zuul.filters.Route;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = RateLimitAutoConfiguration.class)
public class OAuth2SecureContextRateLimitTypeTest {

    @Mock
    private HttpServletRequest httpServletRequest;
    private Route route = new Route("servicea", "/test", "servicea", "/servicea", null, Collections.emptySet());
    private RateLimitUtils rateLimitUtils;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        RateLimitProperties properties = new RateLimitProperties();
        rateLimitUtils = new OAuth2SecuredRateLimitUtils(properties);

        SecurityContextHolder.getContext().setAuthentication(null);
    }

    @Test
    public void applyClientId() {
        SecurityContextHolder.getContext().setAuthentication(mockAuthentication());

        boolean apply = RateLimitType.CLIENT_ID.apply(httpServletRequest, route, rateLimitUtils, "optimus_prime");
        assertThat(apply).isTrue();
    }

    @Test
    public void doNotApplyClientId() {
        SecurityContextHolder.getContext().setAuthentication(mockAuthentication());

        boolean apply = RateLimitType.CLIENT_ID.apply(httpServletRequest, route, rateLimitUtils, "bumblebee");
        assertThat(apply).isFalse();
    }

    @Test
    public void withEmptyAuthentication() {
        boolean apply = RateLimitType.CLIENT_ID.apply(httpServletRequest, route, rateLimitUtils, null);
        assertThat(apply).isFalse();
    }

    @Test
    public void withJwtAuthentication() {
        JwtAuthenticationToken jwt = mock(JwtAuthenticationToken.class);
        when(jwt.getName()).thenReturn("optimus_prime");

        SecurityContextHolder.getContext().setAuthentication(jwt);
        boolean apply = RateLimitType.CLIENT_ID.apply(httpServletRequest, route, rateLimitUtils, "optimus_prime");
        assertThat(apply).isTrue();
    }

    private OAuth2AuthenticationToken mockAuthentication() {
        OAuth2AuthenticationToken auth = mock(OAuth2AuthenticationToken.class);
        when(auth.getName()).thenReturn("optimus_prime");
        return auth;
    }

}
