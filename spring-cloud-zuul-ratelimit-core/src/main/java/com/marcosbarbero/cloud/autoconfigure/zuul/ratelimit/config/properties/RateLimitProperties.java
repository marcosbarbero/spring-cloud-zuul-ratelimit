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
import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.FORM_BODY_WRAPPER_FILTER_ORDER;
import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.SEND_RESPONSE_FILTER_ORDER;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.RateLimitUtils;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.properties.validators.Policies;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.netflix.zuul.filters.Route;
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
    @Policies
    private Policy defaultPolicy;
    @Valid
    @NotNull
    @Policies
    private List<Policy> defaultPolicyList = Lists.newArrayList();
    @Valid
    @NotNull
    @Policies
    private Map<String, Policy> policies = Maps.newHashMap();
    @Valid
    @NotNull
    @Policies
    private Map<String, List<Policy>> policyList = Maps.newHashMap();
    private boolean behindProxy;
    private boolean enabled;
    @NotNull
    @Value("${spring.application.name:rate-limit-application}")
    private String keyPrefix;
    @Valid
    @NotNull
    private RateLimitRepository repository = RateLimitRepository.IN_MEMORY;
    private int postFilterOrder = SEND_RESPONSE_FILTER_ORDER - 10;
    private int preFilterOrder = FORM_BODY_WRAPPER_FILTER_ORDER;

    public List<Policy> getPolicies(String key) {
        if (StringUtils.isEmpty(key)) {
            return defaultPolicyList;
        }
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
            private RateLimitType type;
            private String matcher;

            public boolean apply(HttpServletRequest request, Route route, RateLimitUtils rateLimitUtils) {
                return StringUtils.isEmpty(matcher) || type.apply(request, route, rateLimitUtils, matcher);
            }

            public String key(HttpServletRequest request, Route route, RateLimitUtils rateLimitUtils) {
                return type.key(request, route, rateLimitUtils) +
                    (StringUtils.isEmpty(matcher) ? StringUtils.EMPTY : (":" + matcher));
            }
        }
    }
}