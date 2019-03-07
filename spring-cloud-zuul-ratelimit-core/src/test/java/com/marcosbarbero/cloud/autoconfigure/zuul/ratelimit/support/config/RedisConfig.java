package com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.support.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.test.context.TestConfiguration;
import redis.embedded.RedisServer;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.net.Socket;

/**
 * Embedded redis configuration.
 *
 * @author Marcos Barbero
 * @since 2019-03-03
 */
@TestConfiguration
@ConditionalOnProperty(name = "zuul.ratelimit.repository", havingValue = "redis")
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
