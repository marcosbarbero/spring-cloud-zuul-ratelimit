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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.DefaultRateLimitKeyGenerator;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.concurrent.TimeUnit.MINUTES;

/**
 * @author Marcos Barbero
 */
@Data
@Validated
@NoArgsConstructor
@ConfigurationProperties(RateLimitProperties.PREFIX)
public class RateLimitProperties {

    public static final String PREFIX = "zuul.ratelimit";

    public AtomicInteger runOnce = new AtomicInteger(0);

    @NotNull
    private List<Policy> policyList = Lists.newArrayList();

    /**
     * compatibility
     */
    @Deprecated
    private Map<String, Policy> policies = Maps.newHashMap();
    /**
     * compatibility
     */
    @Deprecated
    private Policy defaultPolicy;

    private boolean behindProxy;

    private boolean enabled;

    @NotNull
    @Value("${spring.application.name:rate-limit-application}")
    private String keyPrefix;

    @NotNull
    private Repository repository = Repository.IN_MEMORY;

    /**
     * compatibility policies & defaultPolicy in config
     *
     * @return all policies
     */
    public List<Policy> getNewPolicies() {
        if (runOnce.compareAndSet(0, 1)) {
            policies.forEach((k, v) -> {
                if (!v.getTypes().containsKey(Policy.Type.ROUTE)) {
                    v.getTypes().put(Policy.Type.ROUTE, k);
                }
                policyList.add(v);
            });
            if (defaultPolicy != null) {
                policyList.add(defaultPolicy.mergeOldType());
            }
        }
        return policyList;
    }

    public enum Repository {
        REDIS, CONSUL, JPA, IN_MEMORY
    }

    @Data
    @NoArgsConstructor
    public static class Policy {

        /**
         * if name not empty, will add to key in {@link DefaultRateLimitKeyGenerator#key}
         * why?
         * Because we may have the same types of types, but they have different requirements for time
         * such as :
         * ```
         * - limit: 10
         *   refresh-interval: 60
         *   types:
         *     user:
         * - limit: 1000
         *   refresh-interval: 6000000
         *   types: #optional
         *     user:
         * ```
         * These two policy generated ids are the same, This will result in a failure of our policy
         * so we need a name field
         */
        private String name;

        @NotNull
        private Long refreshInterval = MINUTES.toSeconds(1L);

        private Long limit;

        private Long quota;

        /**
         * if this request in limited , show this alert message
         */
        private String alertMessage;

        /**
         * compatibility
         */
        @Deprecated
        private List<Type> type = Lists.newArrayList();

        private Map<Type, String> types = Maps.newLinkedHashMap();

        /**
         * compatibility type field in config, merge old type to types
         */
        public Policy mergeOldType() {
            type.forEach(t -> {
                if (!types.containsKey(t)) {
                    types.put(t, "");
                }
            });
            return this;
        }

        public enum Type {
            ORIGIN, USER, URL, ROUTE
        }

    }
}