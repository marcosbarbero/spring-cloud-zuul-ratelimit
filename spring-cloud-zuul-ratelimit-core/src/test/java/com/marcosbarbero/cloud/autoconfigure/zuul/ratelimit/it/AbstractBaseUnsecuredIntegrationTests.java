package com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.it;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

/**
 * @author Marcos Barbero
 * @since 2019-03-03
 */
@SpringBootTest(classes = AbstractBaseUnsecuredIntegrationTests.Application.class, webEnvironment = RANDOM_PORT)
public abstract class AbstractBaseUnsecuredIntegrationTests extends AbstractBaseIntegrationTest {

    /**
     * Spring Boot entry point without security auto config.
     */
    @EnableZuulProxy
    @SpringBootApplication(
            scanBasePackages = "com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit",
            exclude = SecurityAutoConfiguration.class
    )
    static class Application {

        public static void main(String... args) {
            SpringApplication.run(Application.class, args);
        }

    }

}
