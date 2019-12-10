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

package com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config;

import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.properties.RateLimitProperties;
import org.springframework.cloud.netflix.zuul.filters.Route;

import javax.servlet.http.HttpServletRequest;

/**
 * Key generator for rate limit control.
 *
 * @author Liel Chayoun
 */
public interface RateLimitKeyGenerator {

    /**
     * Returns a key based on {@link HttpServletRequest}, {@link Route} and
     * {@link RateLimitProperties.Policy}
     *
     * @param request The {@link HttpServletRequest}
     * @param route   The {@link Route}
     * @param policy  The {@link RateLimitProperties.Policy}
     * @return Generated key
     */
    String key(HttpServletRequest request, Route route, RateLimitProperties.Policy policy);
}
