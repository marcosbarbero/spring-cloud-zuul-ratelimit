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

import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.RequestUtils;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.UserIdGetter;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.properties.RateLimitProperties;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.properties.RateLimitProperties.Policy;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cloud.netflix.zuul.filters.Route;
import org.springframework.cloud.netflix.zuul.filters.RouteLocator;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.util.UrlPathHelper;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    public static final String LIMIT_POLICIES = "limit-policies";

    private final RateLimitProperties properties;
    private final RouteLocator routeLocator;
    private final UrlPathHelper urlPathHelper;
    private final UserIdGetter userIdGetter;
    private final AntPathMatcher antPathMatcher = new AntPathMatcher();

    @Override
    public boolean shouldFilter() {
        return properties.isEnabled() && policy(RequestContext.getCurrentContext()).size() > 0;
    }

    Route route() {
        String requestURI = urlPathHelper.getPathWithinApplication(RequestContext.getCurrentContext().getRequest());
        return routeLocator.getMatchingRoute(requestURI);
    }

    protected List<Policy> policy(RequestContext context) {
        if (context.containsKey(LIMIT_POLICIES)) {
            return (List<Policy>) context.get(LIMIT_POLICIES);
        }
        HashMap<Policy.Type, String> map = new HashMap<>(8);
        map.put(Policy.Type.URL, context.getRequest().getRequestURI());
        map.put(Policy.Type.USER, userIdGetter.getUserId(context));
        map.put(Policy.Type.ORIGIN, RequestUtils.getRealIp(context.getRequest(), properties.isBehindProxy()));
        Route route = route();
        map.put(Policy.Type.ROUTE, route == null ? null : route.getId());
        List<Policy> policies = properties.getPolicies().stream()
                .filter(policy -> match(policy, map))
                .collect(Collectors.toList());
        context.set(LIMIT_POLICIES, policies);
        return policies;
    }

    protected boolean match(Policy policy, Map<Policy.Type, String> requestInfo) {
        Map<Policy.Type, String> types = policy.getTypes();
        return !types.entrySet().stream()
                .filter(entry -> StringUtils.isNotEmpty(entry.getValue()) && !isMatch(entry, requestInfo))
                .findFirst()
                .isPresent();
    }

    /**
     * url type support multiple & antPath
     *
     * @param entry  {@link Map.Entry<Policy.Type, String>}
     * @param requestInfo {@link Map.Entry<Policy.Type, String>}
     * @return isMatch
     */
    private boolean isMatch(Map.Entry<Policy.Type, String> entry, Map<Policy.Type, String> requestInfo) {
        if (entry.getKey() == Policy.Type.URL) {
            return Arrays.stream(entry.getValue().split(","))
                    .filter(url -> antPathMatcher.match(url, requestInfo.get(entry.getKey())))
                    .findFirst()
                    .isPresent();
        }
        return entry.getValue().equals(requestInfo.get(entry.getKey()));
    }

}
