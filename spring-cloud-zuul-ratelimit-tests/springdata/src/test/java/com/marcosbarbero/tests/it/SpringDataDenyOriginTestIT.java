package com.marcosbarbero.tests.it;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "zuul.ratelimit.deny-request.origins[0]=127.0.0.1",
                "zuul.ratelimit.deny-request.response-status-code=404"
        }
)
public class SpringDataDenyOriginTestIT {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void testDeniedOrigin() {
        ResponseEntity<String> response = this.restTemplate.getForEntity("/serviceC", String.class);
//        assertEquals(FORBIDDEN, response.getStatusCode());
        assertEquals(NOT_FOUND, response.getStatusCode());
    }

}
