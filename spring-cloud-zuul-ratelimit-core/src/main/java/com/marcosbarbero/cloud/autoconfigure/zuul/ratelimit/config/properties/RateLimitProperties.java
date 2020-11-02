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
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.DeprecatedConfigurationProperty;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.boot.convert.DurationUnit;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.netflix.zuul.filters.Route;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.properties.ResponseHeadersVerbosity.NONE;
import static com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.properties.ResponseHeadersVerbosity.VERBOSE;
import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.FORM_BODY_WRAPPER_FILTER_ORDER;
import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.SEND_RESPONSE_FILTER_ORDER;

/**
 * @author Marcos Barbero
 * @author Liel Chayoun
 */
@Validated
@RefreshScope
@ConfigurationProperties(RateLimitProperties.PREFIX)
public class RateLimitProperties {

    public static final String PREFIX = "zuul.ratelimit";

    @Valid
    @NotNull
    @Policies
    @NestedConfigurationProperty
    private List<Policy> defaultPolicyList = Lists.newArrayList();

    @Valid
    @NotNull
    @Policies
    @NestedConfigurationProperty
    private Map<String, List<Policy>> policyList = Maps.newHashMap();

    private boolean behindProxy;

    private boolean enabled;

    @NotNull
    private ResponseHeadersVerbosity responseHeaders = VERBOSE;

    @NotNull
    @Value("${spring.application.name:rate-limit-application}")
    private String keyPrefix;

    @NotNull
    private RateLimitRepository repository;

    private int postFilterOrder = SEND_RESPONSE_FILTER_ORDER - 10;

    private int preFilterOrder = FORM_BODY_WRAPPER_FILTER_ORDER;

    @NestedConfigurationProperty
    private Location location = new Location();

    public List<Policy> getPolicies(String key) {
        return policyList.getOrDefault(key, defaultPolicyList);
    }

    public List<Policy> getDefaultPolicyList() {
        return defaultPolicyList;
    }

    public void setDefaultPolicyList(List<Policy> defaultPolicyList) {
        this.defaultPolicyList = defaultPolicyList;
    }

    public Map<String, List<Policy>> getPolicyList() {
        return policyList;
    }

    public void setPolicyList(Map<String, List<Policy>> policyList) {
        this.policyList = policyList;
    }

    public boolean isBehindProxy() {
        return behindProxy;
    }

    public void setBehindProxy(boolean behindProxy) {
        this.behindProxy = behindProxy;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Tells if rate limit response headers should be added to response.
     *
     * @return Whether the response headers should be added
     * @deprecated use {{@link #responseHeaders}
     */
    @Deprecated
    @DeprecatedConfigurationProperty(replacement = "zuul.ratelimit.response-headers")
    public boolean isAddResponseHeaders() {
        return !NONE.equals(responseHeaders);
    }

    @Deprecated
    public void setAddResponseHeaders(boolean addResponseHeaders) {
        setResponseHeaders(addResponseHeaders ? VERBOSE : NONE);
    }

    public ResponseHeadersVerbosity getResponseHeaders() {
        return this.responseHeaders;
    }

    public void setResponseHeaders(ResponseHeadersVerbosity responseHeaders) {
        this.responseHeaders = responseHeaders;
    }

    public String getKeyPrefix() {
        return keyPrefix;
    }

    public void setKeyPrefix(String keyPrefix) {
        this.keyPrefix = keyPrefix;
    }

    public RateLimitRepository getRepository() {
        return repository;
    }

    public void setRepository(RateLimitRepository repository) {
        this.repository = repository;
    }

    public int getPostFilterOrder() {
        return postFilterOrder;
    }

    public void setPostFilterOrder(int postFilterOrder) {
        this.postFilterOrder = postFilterOrder;
    }

    public int getPreFilterOrder() {
        return preFilterOrder;
    }

    public void setPreFilterOrder(int preFilterOrder) {
        this.preFilterOrder = preFilterOrder;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    /**
     * Tells if rate limit response headers should be added to response.
     *
     * @return Whether a {@link DenyRequest}
     * @deprecated use {{@link #location}
     */
    @Deprecated
    @DeprecatedConfigurationProperty(replacement = "zuul.ratelimit.location.deny")
    public DenyRequest getDenyRequest() {
        return new DenyRequest(this.location.getDeny());
    }

    @Deprecated
    public void setDenyRequest(DenyRequest denyRequest) {
        getLocation().setDeny(denyRequest.origins);
    }

    public static class Policy {
        /**
         * Refresh interval window (in seconds).
         */
        @NotNull
        @DurationUnit(ChronoUnit.SECONDS)
        private Duration refreshInterval = Duration.ofSeconds(60);

        /**
         * Request number limit per refresh interval window.
         */
        private Long limit;

        /**
         * Request time limit per refresh interval window (in seconds).
         */
        @DurationUnit(ChronoUnit.SECONDS)
        private Duration quota;

        private boolean breakOnMatch;

        @Valid
        @NotNull
        @NestedConfigurationProperty
        private List<MatchType> type = Lists.newArrayList();

        public Duration getRefreshInterval() {
            return refreshInterval;
        }

        public void setRefreshInterval(Duration refreshInterval) {
            this.refreshInterval = refreshInterval;
        }

        public Long getLimit() {
            return limit;
        }

        public void setLimit(Long limit) {
            this.limit = limit;
        }

        public Duration getQuota() {
            return quota;
        }

        public void setQuota(Duration quota) {
            this.quota = quota;
        }

        public boolean isBreakOnMatch() {
            return breakOnMatch;
        }

        public void setBreakOnMatch(boolean breakOnMatch) {
            this.breakOnMatch = breakOnMatch;
        }

        public List<MatchType> getType() {
            return type;
        }

        public void setType(List<MatchType> type) {
            this.type = type;
        }

        public static class MatchType {

            @Valid
            @NotNull
            private RateLimitType type;

            private String matcher;

            public MatchType(@Valid @NotNull RateLimitType type, String matcher) {
                this.type = type;
                this.matcher = matcher;
            }

            public boolean apply(HttpServletRequest request, Route route, RateLimitUtils rateLimitUtils) {
                return StringUtils.isEmpty(matcher) || type.apply(request, route, rateLimitUtils, matcher);
            }

            public String key(HttpServletRequest request, Route route, RateLimitUtils rateLimitUtils) {
                return type.key(request, route, rateLimitUtils, matcher) +
                        (StringUtils.isEmpty(matcher) ? StringUtils.EMPTY : (":" + matcher));
            }

            public RateLimitType getType() {
                return type;
            }

            public void setType(RateLimitType type) {
                this.type = type;
            }

            public String getMatcher() {
                return matcher;
            }

            public void setMatcher(String matcher) {
                this.matcher = matcher;
            }
        }
    }

    /**
     * @see Location
     */
    @Deprecated
    public static class DenyRequest {

        /**
         * List of origins that will have the request denied.
         */
        @NotNull
        private List<String> origins = new ArrayList<>();

        /**
         * Status code returned when a blocked origin tries to reach the server.
         */
        private int responseStatusCode = HttpStatus.FORBIDDEN.value();

        public DenyRequest() {
        }

        public DenyRequest(@NotNull List<String> origins) {
            this.origins = origins;
        }

        public List<String> getOrigins() {
            return origins;
        }

        public void setOrigins(List<String> origins) {
            this.origins = origins;
        }

        public int getResponseStatusCode() {
            return responseStatusCode;
        }

        public void setResponseStatusCode(int responseStatusCode) {
            this.responseStatusCode = responseStatusCode;
        }
    }

    public static class Location {

        /**
         * List of origins that will have the request denied.
         */
        private List<String> deny = new ArrayList<>();

        /**
         * List of origins that will have the request by-passed.
         */
        private List<String> bypass = new ArrayList<>();

        public Location() {
        }

        public Location(List<String> deny, List<String> bypass) {
            this.deny = deny;
            this.bypass = bypass;
        }

        public List<String> getDeny() {
            return deny;
        }

        public void setDeny(List<String> deny) {
            this.deny = deny;
        }

        public List<String> getBypass() {
            return bypass;
        }

        public void setBypass(List<String> bypass) {
            this.bypass = bypass;
        }
    }

}
