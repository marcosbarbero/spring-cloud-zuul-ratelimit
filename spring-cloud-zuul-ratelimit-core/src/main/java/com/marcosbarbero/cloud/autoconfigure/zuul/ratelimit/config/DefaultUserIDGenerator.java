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

import com.netflix.zuul.context.RequestContext;

import javax.servlet.http.HttpServletRequest;

/**
 * DefaultUserIDGenerator
 *
 * @author doob [fudali113@gmail.com]
 * @since 2017/11/30
 */
public class DefaultUserIDGenerator implements UserIDGenerator {
    @Override
    public String getUserId(RequestContext context) {
        HttpServletRequest request = context.getRequest();
        return request.getRemoteUser() != null ? request.getRemoteUser() : ANONYMOUS_USER;
    }
}