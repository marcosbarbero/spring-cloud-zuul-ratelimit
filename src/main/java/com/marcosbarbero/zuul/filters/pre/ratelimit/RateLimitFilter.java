package com.marcosbarbero.zuul.filters.pre.ratelimit;

import com.marcosbarbero.zuul.filters.pre.ratelimit.config.Policy;
import com.marcosbarbero.zuul.filters.pre.ratelimit.config.RateLimitProperties;
import com.marcosbarbero.zuul.filters.pre.ratelimit.config.RateLimiter;
import com.marcosbarbero.zuul.filters.pre.ratelimit.config.Rate;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import org.springframework.cloud.netflix.zuul.filters.Route;
import org.springframework.cloud.netflix.zuul.filters.RouteLocator;
import org.springframework.http.HttpStatus;
import org.springframework.web.util.UrlPathHelper;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Marcos Barbero
 */
public class RateLimitFilter extends ZuulFilter {

    private final RateLimiter limiter;
    private RateLimitProperties properties;
    private RouteLocator routeLocator;
    private static final UrlPathHelper URL_PATH_HELPER = new UrlPathHelper();

    public RateLimitFilter(RateLimiter limiter, RateLimitProperties properties, RouteLocator routeLocator) {
        this.limiter = limiter;
        this.properties = properties;
        this.routeLocator = routeLocator;
    }

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
        return properties.isEnabled() && policy() != null;
    }

    public Object run() {
        RequestContext ctx = RequestContext.getCurrentContext();
        HttpServletResponse response = ctx.getResponse();
        HttpServletRequest request = ctx.getRequest();
        Policy policy = policy();
        String key = key(request);
        Rate rate = limiter.consume(policy, key);
        response.setHeader(Headers.LIMIT, rate.getLimit().toString());
        response.setHeader(Headers.REMAINING, String.valueOf(Math.max(rate.getRemaining(), 0)));
        response.setHeader(Headers.RESET, rate.getReset().toString());
        if (rate.getRemaining() < 0) {
            ctx.setResponseStatusCode(HttpStatus.TOO_MANY_REQUESTS.value());
            ctx.put("rateLimitExceeded", "true");
            throw new RuntimeException(HttpStatus.TOO_MANY_REQUESTS.toString());
        }
        return null;
    }

    /**
     * Get the requestURI from request.
     *
     * @return The request URI
     */
    private String requestURI() {
        return URL_PATH_HELPER.getPathWithinApplication(RequestContext.getCurrentContext().getRequest());
    }

    /**
     * Return the requestedContext from request.
     *
     * @return The requestedContext
     */
    private Route route() {
        return this.routeLocator.getMatchingRoute(this.requestURI());
    }

    private Policy policy() {
        return (route() != null) ? properties.getPolicies().get(route().getId()) : null;
    }

    private String key(HttpServletRequest request) {
        final Policy policy = this.policy();
        final Route route = route();
        StringBuilder builder = new StringBuilder(route.getId());
        if (policy.getType().contains(Policy.Type.ORIGIN)) {
            builder.append(":").append(getRemoteAddr(request));
        }
        if (policy.getType().contains(Policy.Type.USER)) {
            builder.append(":").append((request.getUserPrincipal() != null) ? request.getUserPrincipal().getName() : "anonymous");
        }
        return builder.toString();
    }

    private String getRemoteAddr(final HttpServletRequest request) {
        final String remoteAddr;
        if (this.properties.isBehindProxy() && request.getHeader("X-FORWARDED-FOR") != null) {
            remoteAddr = request.getHeader("X-FORWARDED-FOR");
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
