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

package com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.properties;

import static java.util.concurrent.TimeUnit.MINUTES;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.validation.annotation.Validated;

/**
 * @author Marcos Barbero
 * @author Liel Chayoun
 */
@Data
@Validated
@RefreshScope
@NoArgsConstructor
@ConfigurationProperties(RateLimitProperties.PREFIX)
public class RateLimitProperties {

    public static final String PREFIX = "zuul.ratelimit";

    @Valid
    private Policy defaultPolicy;
    @Valid
    @NotNull
    private List<Policy> defaultPolicyList = Lists.newArrayList();
    @Valid
    @NotNull
    private Map<String, Policy> policies = Maps.newHashMap();
    @Valid
    @NotNull
    private Map<String, List<Policy>> policyList = Maps.newHashMap();
    private boolean behindProxy;
    private boolean enabled;
    @NotNull
    @Value("${spring.application.name:rate-limit-application}")
    private String keyPrefix;
    @Valid
    @NotNull
    private Repository repository = Repository.IN_MEMORY;

    public enum Repository {
        /**
         * Uses Redis as data storage
         */
        REDIS,

        /**
         * Uses Consul as data storage
         */
        CONSUL,

        /**
         * Uses SQL database as data storage
         */
        JPA,

        /**
         * Uses Bucket4j JCache as data storage
         */
        BUCKET4J_JCACHE,

        /**
         * Uses Bucket4j Hazelcast as data storage
         */
        BUCKET4J_HAZELCAST,

        /**
         * Uses Bucket4j Ignite as data storage
         */
        BUCKET4J_IGNITE,

        /**
         * Uses Bucket4j Infinispan as data storage
         */
        BUCKET4J_INFINISPAN,

        /**
         * Uses a ConcurrentHashMap as data storage
         */
        IN_MEMORY
    }

    public List<Policy> getPolicies(String key) {
        return policyList.getOrDefault(key, defaultPolicyList);
    }

    @Data
    @NoArgsConstructor
    public static class Policy {

        @NotNull
        private Long refreshInterval = MINUTES.toSeconds(1L);

        private Long limit;

        private Long quota;

        @Valid
        @NotNull
        private List<MatchType> type = Lists.newArrayList();

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class MatchType {

            @Valid
            @NotNull
            private Type type;
            private String matcher;
        }

        public enum Type {
            /**
             * Rate limit policy considering the user's origin.
             */
            ORIGIN,

            /**
             * Rate limit policy considering the authenticated user.
             */
            USER,

            /**
             * Rate limit policy considering the request path to the downstream service.
             */
            URL
        }
    }
}