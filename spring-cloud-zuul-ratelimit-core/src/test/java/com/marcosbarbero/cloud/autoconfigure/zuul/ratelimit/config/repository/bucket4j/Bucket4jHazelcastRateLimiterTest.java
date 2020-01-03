package com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.repository.bucket4j;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.repository.BaseRateLimiterTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.MockitoAnnotations;

public class Bucket4jHazelcastRateLimiterTest extends BaseRateLimiterTest {

    private static HazelcastInstance hazelcastInstance;

    @BeforeAll
    public static void setUpClass() {
        hazelcastInstance = Hazelcast.newHazelcastInstance();
    }

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        target = new Bucket4jHazelcastRateLimiter(hazelcastInstance.getMap("rateLimit"));
    }

    @AfterEach
    public void tearDown() {
        hazelcastInstance.getMap("rateLimit").destroy();
    }

    @AfterAll
    public static void tearDownClass() {
        Hazelcast.shutdownAll();
    }
}