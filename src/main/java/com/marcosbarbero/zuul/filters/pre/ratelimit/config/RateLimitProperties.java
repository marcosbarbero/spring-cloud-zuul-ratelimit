package com.marcosbarbero.zuul.filters.pre.ratelimit.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.LinkedHashMap;
import java.util.Map;

import lombok.Data;

import static com.marcosbarbero.zuul.filters.pre.ratelimit.config.RateLimitProperties.PREFIX;

/**
 * @author Marcos Barbero
 */
@Data
@ConfigurationProperties(PREFIX)
public class RateLimitProperties {

    public static final String PREFIX = "zuul.ratelimit";

    private Map<String, Policy> policies = new LinkedHashMap<>();
    private boolean enabled;
    private boolean behindProxy;
}