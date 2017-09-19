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

import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.properties.RateLimitProperties;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.properties.RateLimitProperties.Policy;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.netflix.zuul.filters.Route;
import org.springframework.cloud.netflix.zuul.filters.RouteLocator;
import org.springframework.web.util.UrlPathHelper;

/**
 * @author Marcos Barbero
 * @author Liel Chayoun
 */
@RequiredArgsConstructor
public abstract class AbstractRateLimitFilter extends ZuulFilter {

    public static final String QUOTA_HEADER = "X-RateLimit-Quota";
    public static final String REMAINING_QUOTA_HEADER = "X-RateLimit-Remaining-Quota";
    public static final String LIMIT_HEADER = "X-RateLimit-Limit";
    public static final String REMAINING_HEADER = "X-RateLimit-Remaining";
    public static final String RESET_HEADER = "X-RateLimit-Reset";
    public static final String REQUEST_START_TIME = "rateLimitRequestStartTime";

    private final RateLimitProperties properties;
    private final RouteLocator routeLocator;
    private final UrlPathHelper urlPathHelper;

    @Override
    public boolean shouldFilter() {
        return properties.isEnabled() && policy(route()).isPresent();
    }

    protected Route route() {
        String requestURI = urlPathHelper.getPathWithinApplication(RequestContext.getCurrentContext().getRequest());
        return routeLocator.getMatchingRoute(requestURI);
    }

    protected Optional<Policy> policy(final Route route) {
        if (route != null) {
            return properties.getPolicy(route.getId());
        }
        return Optional.ofNullable(properties.getDefaultPolicy());
    }
}
