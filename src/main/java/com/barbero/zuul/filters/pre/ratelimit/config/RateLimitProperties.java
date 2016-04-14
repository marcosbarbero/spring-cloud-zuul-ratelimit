package com.barbero.zuul.filters.pre.ratelimit.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Marcos Barbero
 */
@Data
@ConfigurationProperties("zuul.ratelimit")
public class RateLimitProperties {

    private Map<String, Policy> policies = new LinkedHashMap<>();
    private boolean enabled;

}