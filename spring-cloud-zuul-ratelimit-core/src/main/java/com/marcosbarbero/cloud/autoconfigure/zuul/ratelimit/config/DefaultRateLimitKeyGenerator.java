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

import java.util.Map;
import java.util.Optional;
import java.util.StringJoiner;

/**
 * Default KeyGenerator implementation.
 *
 * @author roxspring (github user)
 * @author Marcos Barbero
 * @author Liel Chayoun
 */
@RequiredArgsConstructor
public class DefaultRateLimitKeyGenerator implements RateLimitKeyGenerator {

    private final UserIDGenerator userIDGenerator;
    private final RateLimitProperties properties;

    @Override
    public String key(final RequestContext context, final Route route, final Policy policy) {
        final Map<Type, String> types = policy.getTypes();
        final StringJoiner joiner = new StringJoiner(":");
        joiner.add(properties.getKeyPrefix());
        boolean hasPolicyName = StringUtils.isNotEmpty(policy.getName());
        if (hasPolicyName) {
            joiner.add(policy.getName());
        }
        types.forEach((type, value) -> {
            /**
             * if value is not empty, we have a const value or more const value
             * we want use policy name replace it to mark key
             */
            if (StringUtils.isNotEmpty(value)) {
                if (!hasPolicyName) {
                    joiner.add(value);
                }
                return;
            }
            switch (type) {
                case ORIGIN:
                    joiner.add(RequestUtils.getRealIp(context.getRequest(), properties.isBehindProxy()));
                    break;
                case USER:
                    joiner.add(userIDGenerator.getUserId(context));
                    break;
                case ROUTE:
                    Optional.ofNullable(route).ifPresent(r -> joiner.add(r.getId()));
                    break;
                case URL:
                    joiner.add(context.getRequest().getRequestURI());
            }
        });
        return joiner.toString();
    }
}
