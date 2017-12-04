package com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config;

/**
 * RateLimitException
 *
 * @author doob [fudali113@gmail.com]
 * @since 17-12-4
 */
public class RateLimitException extends Exception {
    public RateLimitException(String message) {
        super(message);
    }
}
