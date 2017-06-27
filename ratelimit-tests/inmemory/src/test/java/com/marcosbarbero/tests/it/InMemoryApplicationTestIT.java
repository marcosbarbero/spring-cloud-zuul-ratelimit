package com.marcosbarbero.tests.it;

import com.marcosbarbero.tests.InMemoryApplication;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.TOO_MANY_REQUESTS;

/**
 * @author Marcos Barbero
 * @since 2017-06-27
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class InMemoryApplicationTestIT {

    private static final String LIMIT = "X-RateLimit-Limit";
    private static final String REMAINING = "X-RateLimit-Remaining";
    private static final String RESET = "X-RateLimit-Reset";

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void testNotExceedingCapacityRequest() {
        ResponseEntity<String> response = this.restTemplate.exchange("/serviceA", GET, null, String.class);
        HttpHeaders headers = response.getHeaders();
        assertHeaders(headers, false);
        assertEquals(OK, response.getStatusCode());
    }

    @Test
    public void testExceedingCapacity() throws InterruptedException {
        ResponseEntity<String> response = this.restTemplate.exchange("/serviceB", GET, null, String.class);
        HttpHeaders headers = response.getHeaders();
        assertHeaders(headers, false);
        assertEquals(OK, response.getStatusCode());

        for (int i = 0; i < 2; i++) {
            response = this.restTemplate.exchange("/serviceB", GET, null, String.class);
        }

        assertEquals(TOO_MANY_REQUESTS, response.getStatusCode());
        assertNotEquals(InMemoryApplication.ServiceController.RESPONSE_BODY, response.getBody());

        TimeUnit.SECONDS.sleep(5);

        response = this.restTemplate.exchange("/serviceB", GET, null, String.class);
        headers = response.getHeaders();
        assertHeaders(headers, false);
        assertEquals(OK, response.getStatusCode());
    }

    @Test
    public void testNoRateLimit() {
        ResponseEntity<String> response = this.restTemplate.exchange("/serviceC", GET, null, String.class);
        HttpHeaders headers = response.getHeaders();
        assertHeaders(headers, true);
        assertEquals(OK, response.getStatusCode());
    }

    private void assertHeaders(HttpHeaders headers, boolean nullable) {
        String limit = headers.getFirst(LIMIT);
        String remaining = headers.getFirst(REMAINING);
        String reset = headers.getFirst(RESET);

        if (!nullable) {
            assertNotNull(limit);
            assertNotNull(remaining);
            assertNotNull(reset);
        } else {
            assertNull(limit);
            assertNull(remaining);
            assertNull(reset);
        }
    }

}
