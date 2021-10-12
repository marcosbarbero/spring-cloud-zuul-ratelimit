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

package com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.repository;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.kv.model.GetValue;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.Rate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.springframework.util.StringUtils.hasText;

/**
 * Consul rate limiter configuration.
 *
 * @author Liel Chayoun
 * @author Marcos Barbero
 * @since 2017-08-15
 */
public class ConsulRateLimiter extends AbstractRateLimiter {

    private static Logger log = LoggerFactory.getLogger(ConsulRateLimiter.class);

    private final ConsulClient consulClient;
    private final ObjectMapper objectMapper;

    public ConsulRateLimiter(RateLimiterErrorHandler rateLimiterErrorHandler,
        ConsulClient consulClient, ObjectMapper objectMapper) {
        super(rateLimiterErrorHandler);
        this.consulClient = consulClient;
        this.objectMapper = objectMapper;
    }

    @Override
    protected Rate getRate(final String key) {
        Rate rate = null;
        GetValue value = this.consulClient.getKVValue(buildValidConsulKey(key)).getValue();
        if (value != null && value.getDecodedValue() != null) {
            try {
                rate = this.objectMapper.readValue(value.getDecodedValue(), Rate.class);
            } catch (IOException e) {
                log.error("Failed to deserialize Rate", e);
            }
        }
        return rate;
    }

    @Override
    protected void saveRate(Rate rate) {
        String value = "";
        try {
            value = this.objectMapper.writeValueAsString(rate);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize Rate", e);
        }

        if (hasText(value)) {
            this.consulClient.setKVValue(buildValidConsulKey(rate.getKey()), value);
        }
    }

    // Slash will corrupt Consul Call , to be changed to _
    private String buildValidConsulKey(String key){
        if(isNotBlank(key)){
            return key.replaceAll("/", "_");
        }
        return key;
    }

}
