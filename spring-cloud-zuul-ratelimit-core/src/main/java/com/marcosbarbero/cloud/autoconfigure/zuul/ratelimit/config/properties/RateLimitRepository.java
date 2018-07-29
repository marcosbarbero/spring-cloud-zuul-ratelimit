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

public enum RateLimitRepository {
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
    IN_MEMORY,
}
