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

package com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.properties;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import static java.util.concurrent.TimeUnit.MINUTES;

/**
 * A policy is used to define rate limit constraints within RateLimiter implementations
 *
 * @author Marcos Barbero
 * @author Michal Šváb
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Policy implements InitializingBean {
    private Long refreshInterval = MINUTES.toSeconds(1L);
    private Long limit;
    private List<Type> type = new ArrayList<>();

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(this.limit, "Policy's limit cannot be null");
    }

    public enum Type {
        ORIGIN, USER, URL
    }

}
