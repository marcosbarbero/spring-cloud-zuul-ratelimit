package com.marcosbarbero.tests.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import redis.embedded.RedisServer;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.net.Socket;

/**
 * Embedded redis configuration.
 *
 * @author Marcos Barbero
 * @since 2017-06-27
 */
@Configuration
public class RedisConfig {

    private static final int DEFAULT_PORT = 6379;

    private RedisServer redisServer;

    private static boolean available(int port) {
        try (Socket ignored = new Socket("localhost", port)) {
            return false;
        } catch (IOException ignored) {
            return true;
        }
    }

    @Bean
    public JedisConnectionFactory connectionFactory() throws IOException {
        this.redisServer = new RedisServer(DEFAULT_PORT);
        if (available(DEFAULT_PORT)) {
            this.redisServer.start();
        }
        return new JedisConnectionFactory();
    }

    @PreDestroy
    public void destroy() {
        this.redisServer.stop();
    }
}
