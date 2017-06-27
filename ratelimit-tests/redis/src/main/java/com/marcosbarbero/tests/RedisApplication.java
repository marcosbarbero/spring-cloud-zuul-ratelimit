package com.marcosbarbero.tests;

import org.springframework.boot.SpringApplication;
import org.springframework.cloud.client.SpringCloudApplication;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;

/**
 * @author Marcos Barbero
 * @since 2017-06-26
 */
@EnableZuulProxy
@SpringCloudApplication
public class RedisApplication {

    public static void main(String... args) {
        SpringApplication.run(RedisApplication.class, args);
    }
}
