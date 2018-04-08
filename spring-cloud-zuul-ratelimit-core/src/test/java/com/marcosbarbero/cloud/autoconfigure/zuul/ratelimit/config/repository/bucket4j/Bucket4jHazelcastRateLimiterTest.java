package com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.repository.bucket4j;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.repository.BaseRateLimiterTest;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.mockito.MockitoAnnotations;

public class Bucket4jHazelcastRateLimiterTest extends BaseRateLimiterTest {

    private static HazelcastInstance hazelcastInstance;

    @BeforeClass
    public static void setUpClass() {
        hazelcastInstance = Hazelcast.newHazelcastInstance();
    }

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        target = new Bucket4jHazelcastRateLimiter(hazelcastInstance.getMap("rateLimit"));
    }

    @After
    public void tearDown() {
        hazelcastInstance.getMap("rateLimit").destroy();
    }

    @AfterClass
    public static void tearDownClass() {
        Hazelcast.shutdownAll();
    }
}