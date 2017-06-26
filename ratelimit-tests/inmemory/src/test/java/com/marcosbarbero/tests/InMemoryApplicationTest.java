package com.marcosbarbero.tests;

import com.marcosbarbero.zuul.filters.pre.ratelimit.config.RateLimiter;
import com.marcosbarbero.zuul.filters.pre.ratelimit.config.repository.InMemoryRateLimiter;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertTrue;

/**
 * @author Marcos Barbero
 * @since 2017-06-26
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class InMemoryApplicationTest {

    @Autowired
    private ApplicationContext context;

    @Test
    public void testInMemoryRateLimiter() {
        RateLimiter rateLimiter = context.getBean(RateLimiter.class);
        assertTrue("InMemoryRateLimiter", rateLimiter instanceof InMemoryRateLimiter);
    }
}
