package com.marcosbarbero.tests.it;

import static com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.support.RateLimitConstants.HEADER_LIMIT;
import static com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.support.RateLimitConstants.HEADER_QUOTA;
import static com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.support.RateLimitConstants.HEADER_REMAINING;
import static com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.support.RateLimitConstants.HEADER_REMAINING_QUOTA;
import static com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.support.RateLimitConstants.HEADER_RESET;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.TOO_MANY_REQUESTS;

import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.RateLimiter;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.repository.RedisRateLimiter;
import com.marcosbarbero.tests.RedisApplication;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.ApplicationContext;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

/**
 * @author Marcos Barbero
 * @since 2017-06-27
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class RedisApplicationTestIT {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ApplicationContext context;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @BeforeEach
    void resetStorage() {
      redisTemplate.delete(redisTemplate.keys("*"));
    }

    @Test
    public void testRedisRateLimiter() {
        RateLimiter rateLimiter = context.getBean(RateLimiter.class);
        assertTrue(rateLimiter instanceof RedisRateLimiter, "RedisRateLimiter");
    }

    @Test
    public void testNotExceedingCapacityRequest() {
        ResponseEntity<String> response = this.restTemplate.getForEntity("/serviceA", String.class);
        HttpHeaders headers = response.getHeaders();
        assertHeaders(headers, "rate-limit-application_serviceA_127.0.0.1", false, false);
        assertEquals(OK, response.getStatusCode());
    }

    @Test
    public void testExceedingCapacity() {
        ResponseEntity<String> response = this.restTemplate.getForEntity("/serviceB", String.class);
        HttpHeaders headers = response.getHeaders();
        String key = "rate-limit-application_serviceB_127.0.0.1";
        assertHeaders(headers, key, false, false);
        assertEquals(OK, response.getStatusCode());

        for (int i = 0; i < 2; i++) {
            response = this.restTemplate.getForEntity("/serviceB", String.class);
        }

        assertEquals(TOO_MANY_REQUESTS, response.getStatusCode());
        assertNotEquals(RedisApplication.ServiceController.RESPONSE_BODY, response.getBody());

        await().pollDelay(2, TimeUnit.SECONDS).untilAsserted(() -> {
            final ResponseEntity<String> responseAfterReset = this.restTemplate
                .getForEntity("/serviceB", String.class);
            final HttpHeaders headersAfterReset = responseAfterReset.getHeaders();
            assertHeaders(headersAfterReset, key, false, false);
            assertEquals(OK, responseAfterReset.getStatusCode());
        });
    }

    @Test
    public void testNoRateLimit() {
        ResponseEntity<String> response = this.restTemplate.getForEntity("/serviceC", String.class);
        HttpHeaders headers = response.getHeaders();
        assertHeaders(headers, "rate-limit-application_serviceC", true, false);
        assertEquals(OK, response.getStatusCode());
    }

    @Test
    public void testMultipleUrls() {
        String randomPath = UUID.randomUUID().toString();

        for (int i = 0; i < 10; i++) {
            if (i % 2 == 0) {
                randomPath = UUID.randomUUID().toString();
            }

            ResponseEntity<String> response = this.restTemplate.getForEntity("/serviceD/" + randomPath, String.class);
            HttpHeaders headers = response.getHeaders();
            assertHeaders(headers, "rate-limit-application_serviceD_serviceD_" + randomPath, false, false);
            assertEquals(OK, response.getStatusCode());
        }
    }

    @Test
    public void testExceedingQuotaCapacityRequest() {
        ResponseEntity<String> response = this.restTemplate.getForEntity("/serviceE", String.class);
        HttpHeaders headers = response.getHeaders();
        String key = "rate-limit-application_serviceE_127.0.0.1";
        assertHeaders(headers, key, false, true);
        assertEquals(OK, response.getStatusCode());

        response = this.restTemplate.getForEntity("/serviceE", String.class);
        headers = response.getHeaders();
        assertHeaders(headers, key, false, true);
        assertEquals(TOO_MANY_REQUESTS, response.getStatusCode());
    }

    @Test
    public void testShouldReturnCorrectRateRemainingValue() {
        String key = "rate-limit-application_serviceA_127.0.0.1";

        Long rateLimit = 10L;
        Long requestCounter = rateLimit;
        do {
            ResponseEntity<String> response = this.restTemplate.getForEntity("/serviceA", String.class);
            assertEquals(OK, response.getStatusCode());
            HttpHeaders headers = response.getHeaders();
            assertHeaders(headers, key, false, false);
            Long limit = Long.valueOf(headers.getFirst(HEADER_LIMIT + key));
            assertEquals(rateLimit, limit);
            Long remaining = Long.valueOf(headers.getFirst(HEADER_REMAINING + key));
            assertEquals(--requestCounter, remaining);
        } while (requestCounter > 0);

        ResponseEntity<String> response = this.restTemplate.getForEntity("/serviceA", String.class);
        assertEquals(TOO_MANY_REQUESTS, response.getStatusCode());
        HttpHeaders headers = response.getHeaders();
        assertHeaders(headers, key, false, false);
        String limit = headers.getFirst(HEADER_LIMIT + key);
        assertEquals("10", limit);
        String remaining = headers.getFirst(HEADER_REMAINING + key);
        assertEquals("0", remaining);
    }

    private void assertHeaders(HttpHeaders headers, String key, boolean nullable, boolean quotaHeaders) {
        String quota = headers.getFirst(HEADER_QUOTA + key);
        String remainingQuota = headers.getFirst(HEADER_REMAINING_QUOTA + key);
        String limit = headers.getFirst(HEADER_LIMIT + key);
        String remaining = headers.getFirst(HEADER_REMAINING + key);
        String reset = headers.getFirst(HEADER_RESET + key);

        if (nullable) {
            if (quotaHeaders) {
                assertNull(quota);
                assertNull(remainingQuota);
            } else {
                assertNull(limit);
                assertNull(remaining);
            }
            assertNull(reset);
        } else {
            if (quotaHeaders) {
                assertNotNull(quota);
                assertNotNull(remainingQuota);
            } else {
                assertNotNull(limit);
                assertNotNull(remaining);
            }
            assertNotNull(reset);
        }
    }
}
