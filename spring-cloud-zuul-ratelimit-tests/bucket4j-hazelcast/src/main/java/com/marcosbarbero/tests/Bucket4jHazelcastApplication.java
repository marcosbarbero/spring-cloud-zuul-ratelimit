package com.marcosbarbero.tests;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.map.IMap;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.cloud.client.SpringCloudApplication;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.context.annotation.Bean;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Liel Chayoun
 * @since 2018-04-07
 */
@EnableZuulProxy
@SpringCloudApplication
public class Bucket4jHazelcastApplication {

    public static void main(String... args) {
        SpringApplication.run(Bucket4jHazelcastApplication.class, args);
    }

    @Bean
    @Qualifier("RateLimit")
    public IMap<String, byte[]> map() {
        return Hazelcast.newHazelcastInstance().getMap("rateLimit");
    }

    @RestController
    public class ServiceController {

        public static final String RESPONSE_BODY = "ResponseBody";

        @GetMapping("/serviceA")
        public ResponseEntity<String> serviceA() {
            return ResponseEntity.ok(RESPONSE_BODY);
        }

        @GetMapping("/serviceB")
        public ResponseEntity<String> serviceB() {
            return ResponseEntity.ok(RESPONSE_BODY);
        }

        @GetMapping("/serviceC")
        public ResponseEntity<String> serviceC() {
            return ResponseEntity.ok(RESPONSE_BODY);
        }

        @GetMapping("/serviceD/{paramName}")
        public ResponseEntity<String> serviceD(@PathVariable String paramName) {
            return ResponseEntity.ok(RESPONSE_BODY + " " + paramName);
        }

        @GetMapping("/serviceE")
        public ResponseEntity<String> serviceE() throws InterruptedException {
            Thread.sleep(1100);
            return ResponseEntity.ok(RESPONSE_BODY);
        }
    }
}
