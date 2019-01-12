package com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.properties;

import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.RateLimitUtils;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.support.SecuredRateLimitUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.cloud.netflix.zuul.filters.Route;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithSecurityContextTestExecutionListener;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringRunner;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@TestExecutionListeners(WithSecurityContextTestExecutionListener.class)
public class SecureContextRateLimitTypeTest {

    @Mock
    private HttpServletRequest httpServletRequest;
    private Route route = new Route("servicea", "/test", "servicea", "/servicea", null, Collections.emptySet());
    private RateLimitUtils rateLimitUtils;

    @Before
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
}
