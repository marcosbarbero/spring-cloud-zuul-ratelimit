package com.marcosbarbero.tests.it;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestPropertySource(locations = "classpath:/application-test-request-location.properties")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
public class SpringDataBypassDenyLocationTestIT {

    @Autowired
    MockMvc mockMvc;

    @Test
    void testBypassRequestOrigin() throws Exception {
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(get("/serviceB")
                    .with(location("127.0.0.1"))
            ).andDo(print())
                    .andExpect(status().isOk());
        }
    }

    @Test
    void testDeniedOrigin() throws Exception {
        mockMvc.perform(get("/serviceB")
                .with(location("126.0.0.1"))
        ).andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    void testDeniedOriginWithDeprecatedConfiguration() throws Exception {
        mockMvc.perform(get("/serviceB")
                .with(location("125.0.0.1"))
        ).andDo(print())
                .andExpect(status().isNotFound());
    }

    private static RequestPostProcessor location(String location) {
        return request -> {
            request.setRemoteAddr(location);
            return request;
        };
    }

}
