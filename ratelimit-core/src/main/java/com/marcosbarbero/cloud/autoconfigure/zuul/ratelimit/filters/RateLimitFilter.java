package com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.filters;

import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.Policy;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.Rate;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.RateLimitProperties;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.RateLimiter;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;

import org.springframework.cloud.netflix.zuul.filters.Route;
import org.springframework.cloud.netflix.zuul.filters.RouteLocator;
import org.springframework.cloud.netflix.zuul.util.ZuulRuntimeException;
import org.springframework.http.HttpStatus;
import org.springframework.web.util.UrlPathHelper;

import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;

import static com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.Policy.Type;
import static com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.Policy.Type.ORIGIN;
import static com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.Policy.Type.URL;
import static com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.Policy.Type.USER;

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
            response.setHeader(Headers.LIMIT, rate.getLimit().toString());
            response.setHeader(Headers.REMAINING, String.valueOf(Math.max(rate.getRemaining(), 0)));
            response.setHeader(Headers.RESET, rate.getReset().toString());
            if (rate.getRemaining() < 0) {
                ctx.setResponseStatusCode(HttpStatus.TOO_MANY_REQUESTS.value());
                ctx.put("rateLimitExceeded", "true");
                throw new ZuulRuntimeException(new ZuulException(HttpStatus.TOO_MANY_REQUESTS.toString(), HttpStatus
                        .TOO_MANY_REQUESTS.value(), null));
            }
        });
        return null;
    }

    private Route route() {
        String requestURI = URL_PATH_HELPER.getPathWithinApplication(RequestContext.getCurrentContext().getRequest());
        return this.routeLocator.getMatchingRoute(requestURI);
    }

    private Optional<Policy> policy() {
        return (route() != null) ? Optional.ofNullable(this.properties.getPolicies().get(route().getId())) :
                Optional.empty();
    }

    private String key(final HttpServletRequest request, final List<Type> types) {
        final Route route = route();
        final StringJoiner joiner = new StringJoiner(":");
        joiner.add(route.getId());
        if (types.contains(URL)) {
            joiner.add(route.getPath());
        }
        if (types.contains(ORIGIN)) {
            joiner.add(getRemoteAddr(request));
        }
        if (types.contains(USER)) {
            joiner.add(request.getUserPrincipal() != null ? request.getUserPrincipal().getName() : ANONYMOUS);
        }
        return joiner.toString();
    }

    private String getRemoteAddr(final HttpServletRequest request) {
        final String remoteAddr;
        if (this.properties.isBehindProxy() && request.getHeader(X_FORWARDED_FOR) != null) {
            remoteAddr = request.getHeader(X_FORWARDED_FOR);
        } else {
            remoteAddr = request.getRemoteAddr();
        }
        return remoteAddr;
    }

    interface Headers {
        String LIMIT = "X-RateLimit-Limit";
        String REMAINING = "X-RateLimit-Remaining";
        String RESET = "X-RateLimit-Reset";
    }
}
