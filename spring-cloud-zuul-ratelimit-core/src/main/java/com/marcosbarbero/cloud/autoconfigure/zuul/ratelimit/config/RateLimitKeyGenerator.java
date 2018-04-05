package com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config;

import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.properties.RateLimitProperties;

import org.springframework.cloud.netflix.zuul.filters.Route;

import javax.servlet.http.HttpServletRequest;

/**
 * Key generator for rate limit control.
 */
public interface RateLimitKeyGenerator {

    /**
     * Returns a key based on {@link HttpServletRequest}, {@link Route} and
     * {@link RateLimitProperties.Policy}
     *
     * @param request The {@link HttpServletRequest}
     * @param route   The {@link Route}
     * @param policy  The {@link RateLimitProperties.Policy}
     * @return Generated key
     */
    String key(HttpServletRequest request, Route route, RateLimitProperties.Policy policy);
}
