package com.marcosbarbero.tests.it;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.http.HttpStatus.OK;

@TestPropertySource(locations = "classpath:/bypass-request-location.properties")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class SpringDataBypassLocationTestIT {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void testDeniedOrigin() {
        for (int i = 0; i < 5; i++) {
            final ResponseEntity<String> responseAfterReset = this.restTemplate
                    .getForEntity("/serviceB", String.class);
            assertEquals(OK, responseAfterReset.getStatusCode());
        }
    }

}
