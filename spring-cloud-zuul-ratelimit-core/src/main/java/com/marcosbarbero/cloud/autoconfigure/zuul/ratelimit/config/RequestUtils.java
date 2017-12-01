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
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.X_FORWARDED_FOR_HEADER;

/**
 * RequestUtils
 *
 * @author doob[fudali133@gmail.com]
 * @since 17-12-1
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RequestUtils {

    /**
     * X-Forwarded-For: client, proxy1, proxy2
     * from: https://en.wikipedia.org/wiki/X-Forwarded-For
     *
     * @param request {@link HttpServletRequest}
     * @return {@link Optional<String>}
     */
    public static final String getRealIp(final HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader(X_FORWARDED_FOR_HEADER))
                .map(xForwardedForStr -> xForwardedForStr.split(",")[0])
                .orElseGet(request::getRemoteAddr);
    }

    /**
     *
     * @param request {@link HttpServletRequest}
     * @param isBehindProxy {@link RateLimitProperties.Policy#behindProxy}
     * @return
     */
    public static final String getRealIp(final HttpServletRequest request, boolean isBehindProxy) {
        if (isBehindProxy) {
            return RequestUtils.getRealIp(request);
        }
        return request.getRemoteAddr();
    }

}
