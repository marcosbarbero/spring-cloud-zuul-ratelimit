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

package com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.support;

import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.RateLimitKeyGenerator;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.RateLimitUtils;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.properties.RateLimitProperties;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.properties.RateLimitProperties.Policy;
import java.util.StringJoiner;
import javax.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cloud.netflix.zuul.filters.Route;

/**
 * Default KeyGenerator implementation.
 *
 * @author roxspring (github user)
 * @author Marcos Barbero
 * @author Liel Chayoun
 */
@RequiredArgsConstructor
public class DefaultRateLimitKeyGenerator implements RateLimitKeyGenerator {

    private final RateLimitProperties properties;
    private final RateLimitUtils rateLimitUtils;

    @Override
    public String key(final HttpServletRequest request, final Route route, final Policy policy) {
        final StringJoiner joiner = new StringJoiner(":");
        joiner.add(properties.getKeyPrefix());
        if (route != null) {
            joiner.add(route.getId());
        }
        policy.getType().forEach(matchType -> {
            String key = matchType.key(request, route, rateLimitUtils);
            if (StringUtils.isNotEmpty(key)) {
                joiner.add(key);
            }
        });
        return joiner.toString();
    }
}
