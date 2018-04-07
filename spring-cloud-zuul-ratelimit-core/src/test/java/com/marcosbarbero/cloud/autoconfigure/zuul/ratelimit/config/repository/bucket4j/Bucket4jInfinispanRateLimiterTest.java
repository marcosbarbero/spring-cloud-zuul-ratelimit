package com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.repository.bucket4j;

import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.repository.BaseRateLimiterTest;
import io.github.bucket4j.grid.GridBucketState;
import org.infinispan.AdvancedCache;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.functional.FunctionalMap;
import org.infinispan.functional.impl.FunctionalMapImpl;
import org.infinispan.functional.impl.ReadWriteMapImpl;
import org.infinispan.manager.DefaultCacheManager;
import org.junit.After;
import org.junit.Before;
import org.mockito.MockitoAnnotations;

public class Bucket4jInfinispanRateLimiterTest extends BaseRateLimiterTest {

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        DefaultCacheManager cacheManager = new DefaultCacheManager();
        cacheManager.defineConfiguration("rateLimit", new ConfigurationBuilder().build());
        AdvancedCache<String, GridBucketState> cache = cacheManager.<String, GridBucketState>getCache("rateLimit").getAdvancedCache();
        FunctionalMapImpl<String, GridBucketState> functionalMap = FunctionalMapImpl.create(cache);
        FunctionalMap.ReadWriteMap<String, GridBucketState> readWriteMap = ReadWriteMapImpl.create(functionalMap);
        target = new Bucket4jInfinispanRateLimiter(readWriteMap);
    }

    @After
    public void tearDown() {
        DefaultCacheManager cacheManager = new DefaultCacheManager();
        cacheManager.removeCache("rateLimit");
    }
}