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
