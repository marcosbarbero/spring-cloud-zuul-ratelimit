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

package com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.repository;

import com.ecwid.consul.v1.ConsulClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.Rate;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Consul rate limiter configuration.
 *
 * @author Liel Chayoun
 * @since 2017-08-16
 */
@Slf4j
@RequiredArgsConstructor
public class ConsulRateLimiter extends DataStoreRateLimiter {

    private final ConsulClient consulClient;
    private final ObjectMapper objectMapper;

    @Override
    protected void saveRate(String key, Rate rate) {
        String value = rate.toString();
        try {
            value = objectMapper.writeValueAsString(rate);
        } catch (JsonProcessingException e) {
            log.error("Failed serializing rate", e);
        }
        consulClient.setKVValue(key, value);
    }

    @Override
    protected Rate getRate(String key) {
        String value = consulClient.getKVValue(key).getValue().getValue();
        Rate rate = null;
        try {
            rate = objectMapper.readValue(value, Rate.class);
        } catch (IOException e) {
            log.error("Failed deserializing rate", e);
        }
        return rate;
    }
}
