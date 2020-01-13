package com.marcosbarbero.tests.it;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.RateLimitUtils;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.support.SecuredRateLimitUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class SecurityContextApplicationTestIT {

    @Autowired
    private ApplicationContext context;

    @Test
    public void securedRateLimitUtils() {
        RateLimitUtils rateLimitUtils = context.getBean(RateLimitUtils.class);
        assertTrue(rateLimitUtils instanceof SecuredRateLimitUtils);
    }
}
