package com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.repository.bucket4j;

import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.repository.BaseRateLimiterTest;
import org.apache.ignite.Ignite;
import org.apache.ignite.Ignition;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.mockito.MockitoAnnotations;

public class Bucket4jJCacheRateLimiterTest extends BaseRateLimiterTest {

    private static Ignite ignite;

    @BeforeClass
    public static void setUpClass() {
        ignite = Ignition.start();
    }

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        target = new Bucket4jJCacheRateLimiter(ignite.createCache("rateLimit"));
    }

    @After
    public void tearDown() {
        ignite.destroyCache("rateLimit");
    }

    @AfterClass
    public static void tearDownClass() {
        Ignition.stop(true);
    }
}