/*
 * Copyright 2012-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.filters;

import static com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.properties.Policy.Type;
import static com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.properties.Policy.Type.ORIGIN;
import static com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.properties.Policy.Type.URL;
import static com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.properties.Policy.Type.USER;
import static org.springframework.http.HttpStatus.TOO_MANY_REQUESTS;

import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.Rate;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.RateLimiter;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.properties.Policy;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.properties.RateLimitProperties;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.netflix.zuul.filters.Route;
import org.springframework.cloud.netflix.zuul.filters.RouteLocator;
import org.springframework.cloud.netflix.zuul.util.ZuulRuntimeException;
import org.springframework.web.util.UrlPathHelper;

/**
 * @author Marcos Barbero
 * @author Michal Šváb
 */
@RequiredArgsConstructor
public class RateLimitFilter extends ZuulFilter {

    private static final UrlPathHelper URL_PATH_HELPER = new UrlPathHelper();
    private static final String X_FORWARDED_FOR = "X-FORWARDED-FOR";
    private static final String ANONYMOUS = "anonymous";

    private final RateLimiter rateLimiter;
    private final RateLimitProperties properties;
    private final RouteLocator routeLocator;

    @Override
    public String filterType() {
        return "pre";
    }

    @Override
    public int filterOrder() {
        return -1;
    }

    @Override
    public boolean shouldFilter() {
        return this.properties.isEnabled() && policy().isPresent();
    }

    public Object run() {
        final RequestContext ctx = RequestContext.getCurrentContext();
        final HttpServletResponse response = ctx.getResponse();
        final HttpServletRequest request = ctx.getRequest();

        policy().ifPresent(policy -> {
            final Rate rate = this.rateLimiter.consume(policy, key(request, policy.getType()));
            response.setHeader(Headers.LIMIT, policy.getLimit().toString());
            response.setHeader(Headers.REMAINING, String.valueOf(Math.max(rate.getRemaining(), 0)));
            response.setHeader(Headers.RESET, rate.getReset().toString());
            if (rate.getRemaining() < 0) {
                ctx.setResponseStatusCode(TOO_MANY_REQUESTS.value());
                ctx.put("rateLimitExceeded", "true");
                throw new ZuulRuntimeException(new ZuulException(TOO_MANY_REQUESTS.toString(),
                        TOO_MANY_REQUESTS.value(), null));
            }
        });
        return null;
    }

    private Route route() {
        String requestURI = URL_PATH_HELPER.getPathWithinApplication(RequestContext.getCurrentContext().getRequest());
        return this.routeLocator.getMatchingRoute(requestURI);
    }

    private Optional<Policy> policy() {
        Route route = route();
        return (route != null) ? Optional.ofNullable(this.properties.getPolicies().get(route.getId())) :
                Optional.empty();
    }

    private String key(final HttpServletRequest request, final List<Type> types) {
        final Route route = route();
        final StringJoiner joiner = new StringJoiner(":");
        joiner.add(this.properties.getKeyPrefix());
        joiner.add(route.getId());
        if (!types.isEmpty()) {
            if (types.contains(URL)) {
                joiner.add(route.getPath());
            }
            if (types.contains(ORIGIN)) {
                joiner.add(getRemoteAddr(request));
            }
            if (types.contains(USER)) {
                joiner.add(request.getUserPrincipal() != null ? request.getUserPrincipal().getName() : ANONYMOUS);
            }
        }
        return joiner.toString();
    }

    private String getRemoteAddr(final HttpServletRequest request) {
        if (this.properties.isBehindProxy() && request.getHeader(X_FORWARDED_FOR) != null) {
            return request.getHeader(X_FORWARDED_FOR);
        }
        return request.getRemoteAddr();
    }

    interface Headers {
        String LIMIT = "X-RateLimit-Limit";
        String REMAINING = "X-RateLimit-Remaining";
        String RESET = "X-RateLimit-Reset";
    }
}
