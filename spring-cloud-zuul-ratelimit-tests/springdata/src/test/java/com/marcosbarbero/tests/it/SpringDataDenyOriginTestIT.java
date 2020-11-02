package com.marcosbarbero.tests.it;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@TestPropertySource(locations = "classpath:/deny-request.properties")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class SpringDataDenyOriginTestIT {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void testDeniedOrigin() {
        ResponseEntity<String> response = this.restTemplate.getForEntity("/serviceC", String.class);
        assertEquals(NOT_FOUND, response.getStatusCode());
    }

}
