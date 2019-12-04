/*
 * Copyright 2012-2018 the original author or authors.
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

import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.RateLimitKeyGenerator;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.RateLimitUtils;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.RateLimiter;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.properties.RateLimitProperties;
import com.netflix.zuul.context.RequestContext;
import org.springframework.cloud.netflix.zuul.filters.Route;
import org.springframework.cloud.netflix.zuul.filters.RouteLocator;
import org.springframework.web.util.UrlPathHelper;

import javax.servlet.http.HttpServletRequest;

import static com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.support.RateLimitConstants.REQUEST_START_TIME;
import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.POST_TYPE;

/**
 * @author Marcos Barbero
 * @author Liel Chayoun
 */
public class RateLimitPostFilter extends AbstractRateLimitFilter {

    private final RateLimiter rateLimiter;
    private final RateLimitKeyGenerator rateLimitKeyGenerator;

    public RateLimitPostFilter(final RateLimitProperties properties, final RouteLocator routeLocator,
                               final UrlPathHelper urlPathHelper, final RateLimiter rateLimiter,
                               final RateLimitKeyGenerator rateLimitKeyGenerator, final RateLimitUtils rateLimitUtils) {
        super(properties, routeLocator, urlPathHelper, rateLimitUtils);
        this.rateLimiter = rateLimiter;
        this.rateLimitKeyGenerator = rateLimitKeyGenerator;
    }

    @Override
    public String filterType() {
        return POST_TYPE;
    }

    @Override
    public int filterOrder() {
        return this.properties.getPostFilterOrder();
    }

    @Override
    public boolean shouldFilter() {
        return super.shouldFilter() && getRequestStartTime() != null;
    }

    private Long getRequestStartTime() {
        final RequestContext ctx = RequestContext.getCurrentContext();
        final HttpServletRequest request = ctx.getRequest();
        return (Long) request.getAttribute(REQUEST_START_TIME);
    }

    @Override
    public Object run() {
        RequestContext ctx = RequestContext.getCurrentContext();
        HttpServletRequest request = ctx.getRequest();
        Route route = route(request);

        policy(route, request).forEach(policy -> {
            long requestTime = System.currentTimeMillis() - getRequestStartTime();
            String key = this.rateLimitKeyGenerator.key(request, route, policy);
            this.rateLimiter.consume(policy, key, requestTime > 0 ? requestTime : 1);
        });

        return null;
    }
}
