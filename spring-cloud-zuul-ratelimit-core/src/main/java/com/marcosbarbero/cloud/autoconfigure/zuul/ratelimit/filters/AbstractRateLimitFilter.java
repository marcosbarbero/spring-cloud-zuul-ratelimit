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
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.properties.RateLimitProperties;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.properties.RateLimitProperties.Policy;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.properties.RateLimitProperties.Policy.MatchType;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.properties.RateLimitProperties.Policy.Type;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.support.RateLimitUtils;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
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

    private final RateLimitProperties properties;
    private final RouteLocator routeLocator;
    private final UrlPathHelper urlPathHelper;
    private final RateLimitKeyGenerator rateLimitKeyGenerator;
    private final RateLimitUtils rateLimitUtils;

    @Override
    public boolean shouldFilter() {
        HttpServletRequest request = RequestContext.getCurrentContext().getRequest();
        return properties.isEnabled() && !policy(route(request), request).isEmpty();
    }

    Route route(HttpServletRequest request) {
        String requestURI = urlPathHelper.getPathWithinApplication(request);
        return routeLocator.getMatchingRoute(requestURI);
    }

    protected List<Policy> policy(Route route, HttpServletRequest request) {
        Map<String, Policy> policyMap = properties.getDefaultPolicyList().stream()
            .collect(Collectors.toMap(policy -> rateLimitKeyGenerator.key(request, route, policy), Function.identity()));
        if (route != null) {
            properties.getPolicies(route.getId()).forEach(policy ->
                policyMap.put(rateLimitKeyGenerator.key(request, route, policy), policy));
        }
        return policyMap.values().stream()
            .filter(policy -> applyPolicy(request, route, policy))
            .collect(Collectors.toList());
    }

    private boolean applyPolicy(HttpServletRequest request, Route route, Policy policy) {
        List<MatchType> types = policy.getType();
        return types.isEmpty() || (urlApply(types, route) && originApply(types, request) && userApply(types, request));
    }

    private boolean userApply(List<MatchType> types, HttpServletRequest request) {
        List<String> users = getConfiguredType(types, Type.USER);

        return users.isEmpty()
            || users.contains(rateLimitUtils.getUser(request));
    }

    private boolean originApply(List<MatchType> types, HttpServletRequest request) {
        List<String> origins = getConfiguredType(types, Type.ORIGIN);

        return origins.isEmpty()
            || origins.contains(rateLimitUtils.getRemoteAddress(request));
    }

    private boolean urlApply(List<MatchType> types, Route route) {
        List<String> urls = getConfiguredType(types, Type.URL);

        return urls.isEmpty()
            || route == null
            || urls.stream().anyMatch(url -> route.getPath().startsWith(url));
    }

    private List<String> getConfiguredType(List<MatchType> types, Type user) {
        return types.stream()
            .filter(matchType -> matchType.getType().equals(user))
            .map(MatchType::getMatcher)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }
}
