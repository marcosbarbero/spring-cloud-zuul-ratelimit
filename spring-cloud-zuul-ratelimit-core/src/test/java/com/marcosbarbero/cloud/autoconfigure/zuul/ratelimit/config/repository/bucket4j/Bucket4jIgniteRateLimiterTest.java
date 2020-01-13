package com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.repository.bucket4j;

import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.repository.BaseRateLimiterTest;
import org.apache.ignite.Ignite;
import org.apache.ignite.Ignition;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.MockitoAnnotations;

public class Bucket4jIgniteRateLimiterTest extends BaseRateLimiterTest {

    private static Ignite ignite;

    @BeforeAll
    public static void setUpClass() {
        ignite = Ignition.start();
    }

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        target = new Bucket4jIgniteRateLimiter(ignite.createCache("rateLimit"));
    }

    @AfterEach
    public void tearDown() {
        ignite.destroyCache("rateLimit");
    }

    @AfterAll
    public static void tearDownClass() {
        Ignition.stop(true);
    }
}