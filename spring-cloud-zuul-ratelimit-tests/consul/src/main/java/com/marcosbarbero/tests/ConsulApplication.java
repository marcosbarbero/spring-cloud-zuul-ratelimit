package com.marcosbarbero.tests;

import static org.springframework.boot.SpringApplication.run;

import com.pszymczyk.consul.ConsulProcess;
import com.pszymczyk.consul.ConsulStarterBuilder;
import javax.annotation.PreDestroy;
import org.springframework.cloud.client.SpringCloudApplication;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Marcos Barbero
 * @since 2017-08-28
 */
@EnableZuulProxy
@SpringCloudApplication
public class ConsulApplication {

    public static void main(String... args) {
        run(ConsulApplication.class, args);
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


    @Configuration
    static class ConsulConfig {

        private ConsulProcess consulProcess;

        @Bean
        public ConsulProcess consulProcess() {
            if (consulProcess == null) {
                consulProcess = ConsulStarterBuilder.consulStarter().withHttpPort(8500).build().start();
            }
            return consulProcess;
        }

        @PreDestroy
        public void cleanup() {
            consulProcess.close();
        }

    }
}
