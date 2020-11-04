package com.marcosbarbero.tests.it;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.stream.Stream;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestPropertySource(locations = "classpath:/application-test-request-location.properties")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
public class SpringDataBypassDenyLocationTestIT {

    @Autowired
    MockMvc mockMvc;

    @ParameterizedTest
    @MethodSource("locationAndResponseCode")
    void testDenyOrBypassLocationRequest(String location, HttpStatus httpStatus) throws Exception {
        mockMvc.perform(get("/serviceB")
                .with(location(location))
        ).andDo(print())
                .andExpect(status().is(httpStatus.value()));
    }

    private static Stream<Arguments> locationAndResponseCode() {
        return Stream.of(
                Arguments.arguments("127.0.0.1", HttpStatus.OK),
                Arguments.arguments("126.0.0.1", HttpStatus.NOT_FOUND),
                Arguments.arguments("125.0.0.1", HttpStatus.NOT_FOUND)
        );
    }

    private static RequestPostProcessor location(String location) {
        return request -> {
            request.setRemoteAddr(location);
            return request;
        };
    }

}
