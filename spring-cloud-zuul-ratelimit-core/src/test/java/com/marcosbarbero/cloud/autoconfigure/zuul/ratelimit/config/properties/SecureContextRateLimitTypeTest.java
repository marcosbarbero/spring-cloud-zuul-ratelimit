package com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.properties;

import static org.assertj.core.api.Assertions.assertThat;

import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.RateLimitAutoConfiguration;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.RateLimitUtils;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.support.SecuredRateLimitUtils;
import java.util.Collections;
import javax.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.netflix.zuul.filters.Route;
import org.springframework.security.test.context.support.WithMockUser;

@SpringBootTest(classes = RateLimitAutoConfiguration.class)
public class SecureContextRateLimitTypeTest {

    @Mock
    private HttpServletRequest httpServletRequest;
    private Route route = new Route("servicea", "/test", "servicea", "/servicea", null, Collections.emptySet());
    private RateLimitUtils rateLimitUtils;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        RateLimitProperties properties = new RateLimitProperties();
        rateLimitUtils = new SecuredRateLimitUtils(properties);
    }

    @Test
    @WithMockUser(username = "commonuser", authorities = {"USER"})
    public void applyRole() {
        boolean apply = RateLimitType.ROLE.apply(httpServletRequest, route, rateLimitUtils, "user");
        assertThat(apply).isTrue();
    }

    @Test
    @WithMockUser(username = "commonuser", authorities = {"ADMIN"})
    public void doNotApplyRole() {
        boolean apply = RateLimitType.ROLE.apply(httpServletRequest, route, rateLimitUtils, "user");
        assertThat(apply).isFalse();
    }

    @Test
    public void withEmptyAuthentication() {
        boolean apply = RateLimitType.ROLE.apply(httpServletRequest, route, rateLimitUtils, "user");
        assertThat(apply).isFalse();
    }
}
