package com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.properties;

import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.RateLimitUtils;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cloud.netflix.zuul.filters.Route;

public enum RateLimitType {
    /**
     * Rate limit policy considering the user's origin.
     */
    ORIGIN {
        @Override
        public boolean apply(HttpServletRequest request, Route route, RateLimitUtils rateLimitUtils, String matcher) {
            return matcher.equals(rateLimitUtils.getRemoteAddress(request));
        }

        @Override
        public String key(HttpServletRequest request, Route route, RateLimitUtils rateLimitUtils) {
            return rateLimitUtils.getRemoteAddress(request);
        }
    },

    /**
     * Rate limit policy considering the authenticated user.
     */
    USER {
        @Override
        public boolean apply(HttpServletRequest request, Route route, RateLimitUtils rateLimitUtils, String matcher) {
            return matcher.equals(rateLimitUtils.getUser(request));
        }

        @Override
        public String key(HttpServletRequest request, Route route, RateLimitUtils rateLimitUtils) {
            return rateLimitUtils.getUser(request);
        }
    },

    /**
     * Rate limit policy considering the request path to the downstream service.
     */
    URL {
        @Override
        public boolean apply(HttpServletRequest request, Route route, RateLimitUtils rateLimitUtils, String matcher) {
            return route == null || route.getPath().startsWith(matcher);
        }

        @Override
        public String key(HttpServletRequest request, Route route, RateLimitUtils rateLimitUtils) {
            return Optional.ofNullable(route).map(Route::getPath).orElse(StringUtils.EMPTY);
        }
    },
    ;

    public abstract boolean apply(HttpServletRequest request, Route route, RateLimitUtils rateLimitUtils,
        String matcher);

    public abstract String key(HttpServletRequest request, Route route, RateLimitUtils rateLimitUtils);
}
