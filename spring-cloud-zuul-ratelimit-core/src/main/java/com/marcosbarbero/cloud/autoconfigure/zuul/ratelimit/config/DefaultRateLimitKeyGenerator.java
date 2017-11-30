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

package com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config;

import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.properties.RateLimitProperties;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.properties.RateLimitProperties.Policy;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.properties.RateLimitProperties.Policy.Type;
import com.netflix.zuul.context.RequestContext;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cloud.netflix.zuul.filters.Route;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.StringJoiner;

import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.X_FORWARDED_FOR_HEADER;

/**
 * Default KeyGenerator implementation.
 *
 * @author roxspring (github user)
 * @author Marcos Barbero
 * @author Liel Chayoun
 */
@RequiredArgsConstructor
public class DefaultRateLimitKeyGenerator implements RateLimitKeyGenerator {

    private final UserIdGetter userIdGetter;
    private final RateLimitProperties properties;

    @Override
    public String key(final RequestContext context, final Route route, final Policy policy) {
        final Map<Type, String> types = policy.getTypes();
        final StringJoiner joiner = new StringJoiner(":");
        joiner.add(properties.getKeyPrefix());
        types.forEach((type, value) -> {
            if (StringUtils.isNotEmpty(value)) {
                joiner.add(value);
                return;
            }
            switch (type) {
                case ORIGIN:
                    joiner.add(getRemoteAddress(context.getRequest()));
                    break;
                case USER:
                    joiner.add(userIdGetter.getUserId(context));
                    break;
                case ROUTE:
                    if (route != null) {
                        joiner.add(route.getId());
                    }
                    break;
                case URL:
                    joiner.add(context.getRequest().getRequestURI());
            }
        });
        return joiner.toString();
    }

    private String getRemoteAddress(final HttpServletRequest request) {
        String xForwardedFor = request.getHeader(X_FORWARDED_FOR_HEADER);
        if (properties.isBehindProxy() && xForwardedFor != null) {
            return xForwardedFor;
        }
        return request.getRemoteAddr();
    }
}
