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
import com.netflix.zuul.util.HTTPRequestUtils;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import javax.servlet.http.HttpServletRequest;

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
     * @return origin client ip
     */
    public static final String getRealIp(final HttpServletRequest request) {
        return HTTPRequestUtils.getInstance().getClientIP(request);
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
