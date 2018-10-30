package com.marcosbarbero.tests;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;

@EnableZuulProxy
@SpringBootApplication
public class SecurityContextApplication {

    public static void main(String... args) {
        SpringApplication.run(SecurityContextApplication.class, args);
    }
}
