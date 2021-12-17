package com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.repository.bucket4j;

import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.repository.BaseRateLimiterTest;
import org.infinispan.AdvancedCache;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.functional.FunctionalMap;
import org.infinispan.functional.impl.FunctionalMapImpl;
import org.infinispan.functional.impl.ReadWriteMapImpl;
import org.infinispan.manager.DefaultCacheManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.MockitoAnnotations;

public class Bucket4jInfinispanRateLimiterTest extends BaseRateLimiterTest {

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        DefaultCacheManager cacheManager = new DefaultCacheManager();
        cacheManager.defineConfiguration("rateLimit", new ConfigurationBuilder().build());
        AdvancedCache<String, byte[]> cache = cacheManager.<String, byte[]>getCache("rateLimit").getAdvancedCache();
        FunctionalMapImpl<String, byte[]> functionalMap = FunctionalMapImpl.create(cache);
        FunctionalMap.ReadWriteMap<String, byte[]> readWriteMap = ReadWriteMapImpl.create(functionalMap);
        target = new Bucket4jInfinispanRateLimiter(readWriteMap);
    }

    @AfterEach
    public void tearDown() {
        DefaultCacheManager cacheManager = new DefaultCacheManager();
        cacheManager.removeCache("rateLimit");
    }
}