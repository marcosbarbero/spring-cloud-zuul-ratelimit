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

import com.fasterxml.jackson.annotation.JsonFormat;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Date;

/**
 * Represents a view of rate limit in a giving time for a user. <p> limit - How many requests can be executed by the
 * user. Maps to X-RateLimit-Limit header remaining - How many requests are still left on the current window. Maps to
 * X-RateLimit-Remaining header reset - Epoch when the rate is replenished by limit. Maps to X-RateLimit-Reset header
 *
 * @author Marcos Barbero
 * @author Liel Chayoun
 */
@Entity
public class Rate {

    @Id
    @Column(name = "rate_key")
    private String key;
    private Long remaining;
    private Long remainingQuota;
    private Long reset;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy HH:mm:ss")
    private Date expiration;

    public Rate() {
    }

    public Rate(String key, Long remaining, Long remainingQuota, Long reset, Date expiration) {
        this.key = key;
        this.remaining = remaining;
        this.remainingQuota = remainingQuota;
        this.reset = reset;
        this.expiration = expiration;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Long getRemaining() {
        return remaining;
    }

    public void setRemaining(Long remaining) {
        this.remaining = remaining;
    }

    public Long getRemainingQuota() {
        return remainingQuota;
    }

    public void setRemainingQuota(Long remainingQuota) {
        this.remainingQuota = remainingQuota;
    }

    public Long getReset() {
        return reset;
    }

    public void setReset(Long reset) {
        this.reset = reset;
    }

    public Date getExpiration() {
        return expiration;
    }

    public void setExpiration(Date expiration) {
        this.expiration = expiration;
    }
}
