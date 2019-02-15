package com.marcosbarbero.tests.it;

import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.RateLimitUtils;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.support.OAuth2SecuredRateLimitUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit4.SpringRunner;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class SecurityContextApplicationTestIT {

    @Autowired
    private ApplicationContext context;

    @Test
    public void securedRateLimitUtils() {
        RateLimitUtils rateLimitUtils = context.getBean(RateLimitUtils.class);
        assertThat(rateLimitUtils, instanceOf(OAuth2SecuredRateLimitUtils.class));
    }
}
