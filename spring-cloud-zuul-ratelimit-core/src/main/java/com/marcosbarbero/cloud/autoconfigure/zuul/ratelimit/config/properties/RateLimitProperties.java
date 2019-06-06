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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.RateLimitUtils;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.properties.validators.Policies;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.netflix.zuul.filters.Route;
import org.springframework.validation.annotation.Validated;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

import static java.util.concurrent.TimeUnit.MINUTES;
import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.FORM_BODY_WRAPPER_FILTER_ORDER;
import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.SEND_RESPONSE_FILTER_ORDER;

/**
 * @author Marcos Barbero
 * @author Liel Chayoun
 */
@Data
@Validated
@RefreshScope
@ConfigurationProperties(RateLimitProperties.PREFIX)
public class RateLimitProperties {

    public static final String PREFIX = "zuul.ratelimit";

    @Valid
    @NotNull
    @Policies
    private List<Policy> defaultPolicyList = Lists.newArrayList();
    @Valid
    @NotNull
    @Policies
    private Map<String, List<Policy>> policyList = Maps.newHashMap();
    private boolean behindProxy;
    private boolean enabled;
    private boolean addResponseHeaders = true;
    @NotNull
    @Value("${spring.application.name:rate-limit-application}")
    private String keyPrefix;
    @NotNull
    private RateLimitRepository repository;
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

        @NotNull
        private boolean breakOnMatch;

        @Valid
        @NotNull
        private List<MatchType> type = Lists.newArrayList();

        @Data
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
                return type.key(request, route, rateLimitUtils, matcher) +
                        (StringUtils.isEmpty(matcher) ? StringUtils.EMPTY : (":" + matcher));
            }
        }
    }
}