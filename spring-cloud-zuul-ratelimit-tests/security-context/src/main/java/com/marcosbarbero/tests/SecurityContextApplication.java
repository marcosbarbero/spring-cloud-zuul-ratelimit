package com.marcosbarbero.tests;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import redis.embedded.RedisServer;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.net.Socket;

@EnableZuulProxy
@SpringBootApplication
public class SecurityContextApplication {

	public static void main(String... args) {
		SpringApplication.run(SecurityContextApplication.class, args);
	}

	@RestController
	public static class ServiceController {

		static final String RESPONSE_BODY = "ResponseBody";

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
	public static class RedisConfig {

		private static final int DEFAULT_PORT = 6380;

		private RedisServer redisServer;

		private static boolean available(int port) {
			try (Socket ignored = new Socket("localhost", port)) {
				return false;
			} catch (IOException ignored) {
				return true;
			}
		}

		@PostConstruct
		public void setUp() throws IOException {
			this.redisServer = new RedisServer(DEFAULT_PORT);
			if (available(DEFAULT_PORT)) {
				this.redisServer.start();
			}
		}

		@PreDestroy
		public void destroy() {
			this.redisServer.stop();
		}
	}

	@Configuration
	@EnableWebSecurity
	static class SecurityConfig extends WebSecurityConfigurerAdapter {

		@Bean
		@Override
		@SuppressWarnings("deprecation")
		public UserDetailsService userDetailsService() {
			UserDetails user =
					User.withDefaultPasswordEncoder()
							.username("user")
							.password("user")
							.roles("USER")
							.build();

			UserDetails admin = User.withDefaultPasswordEncoder()
					.username("admin")
					.password("admin")
					.roles("ADMIN")
					.build();

			return new InMemoryUserDetailsManager(user, admin);
		}
	}

}
