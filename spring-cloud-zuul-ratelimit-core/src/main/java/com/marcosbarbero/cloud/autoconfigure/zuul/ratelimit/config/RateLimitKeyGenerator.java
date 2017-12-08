package com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config;

import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.properties.RateLimitProperties;

import com.netflix.zuul.context.RequestContext;
import org.springframework.cloud.netflix.zuul.filters.Route;

import javax.servlet.http.HttpServletRequest;

/**
 * Key generator for rate limit control.
 */
public interface RateLimitKeyGenerator {

    /**
     * Returns a key based on {@link RequestContext}, {@link Route} and
     * {@link com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.properties.RateLimitProperties.Policy}
     *
     * @param context The {@link RequestContext}
     * @param route   The {@link Route}
     * @param policy  The
     * {@link com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.properties.RateLimitProperties.Policy}
     * @return Generated key
     */
    String key(RequestContext context, Route route, RateLimitProperties.Policy policy);
}
