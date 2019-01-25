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

import javax.servlet.http.HttpServletRequest;
import java.util.Set;

/**
 * @author Liel Chayoun
 */
public interface RateLimitUtils {

    /**
     * Returns the authenticated user from {@link HttpServletRequest}.
     *
     * @param request The {@link HttpServletRequest}
     * @return The authenticated user or annonymous
     */
    String getUser(HttpServletRequest request);

    /**
     * Returns the remote IP address from {@link HttpServletRequest}.
     *
     * @param request The {@link HttpServletRequest}
     * @return The remote IP address
     */
    String getRemoteAddress(HttpServletRequest request);

    /**
     * Returns the authenticated user's roles.
     *
     * @return The authenticated user's roles or empty
     */
    Set<String> getUserRoles();

    /**
     * Returns the OAuth2 clientId.
     *
     * @return The clientId
     */
    String getClientId();

}
