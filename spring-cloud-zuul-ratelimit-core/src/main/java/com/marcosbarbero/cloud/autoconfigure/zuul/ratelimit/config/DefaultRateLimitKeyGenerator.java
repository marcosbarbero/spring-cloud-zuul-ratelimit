package com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config;

import com.google.common.net.HttpHeaders;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.properties.RateLimitProperties;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.properties.RateLimitProperties.Policy.Type;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.netflix.zuul.filters.Route;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.StringJoiner;

@RequiredArgsConstructor
public class DefaultRateLimitKeyGenerator implements RateLimitKeyGenerator {

    private static final String ANONYMOUS_USER = "anonymous";

    private final RateLimitProperties properties;

    public String key(final HttpServletRequest request, final Route route, final RateLimitProperties.Policy policy) {
        final List<Type> types = policy.getType();
        final StringJoiner joiner = new StringJoiner(":");
        joiner.add(properties.getKeyPrefix());
        joiner.add(route.getId());
        if (!types.isEmpty()) {
            if (types.contains(Type.URL)) {
                joiner.add(route.getPath());
            }
            if (types.contains(Type.ORIGIN)) {
                joiner.add(getRemoteAddr(request));
            }
            if (types.contains(Type.USER)) {
                joiner.add(request.getUserPrincipal() != null ? request.getUserPrincipal().getName() : ANONYMOUS_USER);
            }
        }
        return joiner.toString();
    }

    private String getRemoteAddr(final HttpServletRequest request) {
        if (properties.isBehindProxy() && request.getHeader(HttpHeaders.X_FORWARDED_FOR) != null) {
            return request.getHeader(HttpHeaders.X_FORWARDED_FOR);
        }
        return request.getRemoteAddr();
    }
}
