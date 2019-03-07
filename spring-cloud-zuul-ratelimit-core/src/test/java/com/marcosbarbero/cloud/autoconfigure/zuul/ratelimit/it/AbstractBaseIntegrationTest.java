package com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.it;

import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.RateLimitAutoConfiguration;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.support.controller.TestSupportController;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.support.RateLimitConstants.HEADER_LIMIT;
import static com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.support.RateLimitConstants.HEADER_QUOTA;
import static com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.support.RateLimitConstants.HEADER_REMAINING;
import static com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.support.RateLimitConstants.HEADER_REMAINING_QUOTA;
import static com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.support.RateLimitConstants.HEADER_RESET;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.TOO_MANY_REQUESTS;

/**
 * @author Marcos Barbero
 * @since 2019-03-03
 */
@RunWith(SpringRunner.class)
@ImportAutoConfiguration(RateLimitAutoConfiguration.class)
public abstract class AbstractBaseIntegrationTest {

    @Autowired
    protected TestRestTemplate restTemplate;

    @Autowired
    protected ApplicationContext context;

    public abstract void testRateLimiter() throws Exception;

    @Test
    public void testNotExceedingCapacityRequest() {
        ResponseEntity<String> response = this.restTemplate.getForEntity("/serviceA", String.class);
        HttpHeaders headers = response.getHeaders();
        assertHeaders(headers, "rate-limit-application_serviceA_127.0.0.1", false, false);
        assertEquals(OK, response.getStatusCode());
    }

    @Test
    public void testExceedingCapacity() throws InterruptedException {
        ResponseEntity<String> response = this.restTemplate.getForEntity("/serviceB", String.class);
        HttpHeaders headers = response.getHeaders();
        String key = "rate-limit-application_serviceB_127.0.0.1";
        assertHeaders(headers, key, false, false);
        assertEquals(OK, response.getStatusCode());

        for (int i = 0; i < 2; i++) {
            response = this.restTemplate.getForEntity("/serviceB", String.class);
        }

        assertEquals(TOO_MANY_REQUESTS, response.getStatusCode());
        assertNotEquals(TestSupportController.RESPONSE_BODY, response.getBody());

        TimeUnit.SECONDS.sleep(2);

        response = this.restTemplate.getForEntity("/serviceB", String.class);
        headers = response.getHeaders();
        assertHeaders(headers, key, false, false);
        assertEquals(OK, response.getStatusCode());
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

    private void assertHeaders(HttpHeaders headers, String key, boolean nullable, boolean quotaHeaders) {
        String quota = headers.getFirst(HEADER_QUOTA + key);
        String remainingQuota = headers.getFirst(HEADER_REMAINING_QUOTA + key);
        String limit = headers.getFirst(HEADER_LIMIT + key);
        String remaining = headers.getFirst(HEADER_REMAINING + key);
        String reset = headers.getFirst(HEADER_RESET + key);

        if (nullable) {
            if (quotaHeaders) {
                assertNull("Quota should be null", quota);
                assertNull("Remaining Quota should be null", remainingQuota);
            } else {
                assertNull("Limit should be null", limit);
                assertNull("Remaining Limit should be null", remaining);
            }
            assertNull(reset);
        } else {
            if (quotaHeaders) {
                assertNotNull("Quota should not be null", quota);
                assertNotNull("Remaining Quota should not be null", remainingQuota);
            } else {
                assertNotNull("Limit should not be null", limit);
                assertNotNull("Remaining Limit should not be null", remaining);
            }
            assertNotNull("Reset should not be null", reset);
        }
    }
}
