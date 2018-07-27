package com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit;

import static com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.properties.RateLimitProperties.PREFIX;
import static org.assertj.core.api.Assertions.assertThat;

import com.ecwid.consul.v1.ConsulClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hazelcast.core.IMap;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.RateLimitKeyGenerator;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.RateLimiter;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.properties.RateLimitProperties;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.properties.RateLimitProperties.Policy;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.repository.ConsulRateLimiter;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.repository.InMemoryRateLimiter;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.repository.RedisRateLimiter;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.repository.bucket4j.Bucket4jHazelcastRateLimiter;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.repository.bucket4j.Bucket4jIgniteRateLimiter;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.repository.bucket4j.Bucket4jInfinispanRateLimiter;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.repository.bucket4j.Bucket4jJCacheRateLimiter;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.support.DefaultRateLimitKeyGenerator;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.support.StringToMatchTypeConverter;
import com.netflix.zuul.ZuulFilter;
import io.github.bucket4j.grid.GridBucketState;
import java.util.List;
import java.util.Map;
import org.apache.ignite.IgniteCache;
import org.infinispan.functional.FunctionalMap.ReadWriteMap;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.netflix.zuul.filters.RouteLocator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.mock.web.MockServletContext;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

/**
 * @author Marcos Barbero
 * @since 2017-06-28
 */
public class RateLimitAutoConfigurationTest {

    private AnnotationConfigWebApplicationContext context;

    @Before
    public void setUp() {
        System.setProperty(PREFIX + ".enabled", "true");
        this.context = new AnnotationConfigWebApplicationContext();
        this.context.setServletContext(new MockServletContext());
        this.context.register(Conf.class);
        this.context.register(RateLimitAutoConfiguration.class);
    }

    @After
    public void tearDown() {
        System.clearProperty(PREFIX + ".repository");
    }

    @After
    public void close() {
        if (this.context != null) {
            this.context.close();
        }
    }

    @Test
    public void testStringToMatchTypeConverter() {
        this.context.refresh();

        Assert.assertNotNull(this.context.getBean(StringToMatchTypeConverter.class));
    }

    @Test
    public void testZuulFilters() {
        this.context.refresh();

        Map<String, ZuulFilter> zuulFilterMap = context.getBeansOfType(ZuulFilter.class);
        assertThat(zuulFilterMap.size()).isEqualTo(2);
        assertThat(zuulFilterMap.keySet()).containsExactly("rateLimiterPreFilter", "rateLimiterPostFilter");
    }

    @Test
    public void testInMemoryRateLimiterByDefault() {
        this.context.refresh();

        Assert.assertTrue(this.context.getBean(RateLimiter.class) instanceof InMemoryRateLimiter);
    }

    @Test
    public void testConsulRateLimiterByProperty() {
        System.setProperty(PREFIX + ".repository", "CONSUL");
        System.setProperty("spring.cloud.consul.enabled", "true");
        this.context.refresh();

        Assert.assertTrue(this.context.getBean(RateLimiter.class) instanceof ConsulRateLimiter);
    }

    @Test
    public void testRedisRateLimiterByProperty() {
        System.setProperty(PREFIX + ".repository", "REDIS");
        this.context.refresh();

        Assert.assertTrue(this.context.getBean(RateLimiter.class) instanceof RedisRateLimiter);
    }

    @Test
    public void testBucket4jJCacheRateLimiterByProperty() {
        System.setProperty(PREFIX + ".repository", "BUCKET4J_JCACHE");
        this.context.refresh();

        Assert.assertTrue(this.context.getBean(RateLimiter.class) instanceof Bucket4jJCacheRateLimiter);
    }

    @Test
    public void testBucket4jHazelcastRateLimiterByProperty() {
        System.setProperty(PREFIX + ".repository", "BUCKET4J_HAZELCAST");
        this.context.refresh();

        Assert.assertTrue(this.context.getBean(RateLimiter.class) instanceof Bucket4jHazelcastRateLimiter);
    }

    @Test
    public void testBucket4jIgniteRateLimiterByProperty() {
        System.setProperty(PREFIX + ".repository", "BUCKET4J_IGNITE");
        this.context.refresh();

        Assert.assertTrue(this.context.getBean(RateLimiter.class) instanceof Bucket4jIgniteRateLimiter);
    }

    @Test
    public void testBucket4jInfinispanRateLimiterByProperty() {
        System.setProperty(PREFIX + ".repository", "BUCKET4J_INFINISPAN");
        this.context.refresh();

        Assert.assertTrue(this.context.getBean(RateLimiter.class) instanceof Bucket4jInfinispanRateLimiter);
    }

    @Test
    public void testInMemoryRateLimiterByProperty() {
        System.setProperty(PREFIX + ".repository", "IN_MEMORY");
        this.context.refresh();

        Assert.assertTrue(this.context.getBean(RateLimiter.class) instanceof InMemoryRateLimiter);
    }

    @Test
    public void testDefaultRateLimitKeyGenerator() {
        this.context.refresh();

        Assert.assertTrue(this.context.getBean(RateLimiter.class) instanceof InMemoryRateLimiter);
        Assert.assertTrue(this.context.getBean(RateLimitKeyGenerator.class) instanceof DefaultRateLimitKeyGenerator);
    }

    @Test
    public void testPolicyAdjuster() {
        System.setProperty(PREFIX + ".defaultPolicy.limit", "3");
        System.setProperty(PREFIX + ".defaultPolicyList[0].limit", "4");
        System.setProperty(PREFIX + ".policies.a.limit", "5");
        System.setProperty(PREFIX + ".policyList.a[0].limit", "6");
        this.context.refresh();

        RateLimitProperties rateLimitProperties = this.context.getBean(RateLimitProperties.class);
        List<Policy> defaultPolicyList = rateLimitProperties.getDefaultPolicyList();
        assertThat(defaultPolicyList).hasSize(2);
        assertThat(defaultPolicyList.get(0).getLimit()).isEqualTo(3);
        assertThat(defaultPolicyList.get(1).getLimit()).isEqualTo(4);
        Map<String, List<Policy>> policyList = rateLimitProperties.getPolicyList();
        assertThat(policyList).hasSize(1);
        List<Policy> policyA = policyList.get("a");
        assertThat(policyA).hasSize(2);
        assertThat(policyA.get(0).getLimit()).isEqualTo(5);
        assertThat(policyA.get(1).getLimit()).isEqualTo(6);
    }

    @Configuration
    public static class Conf {

        @Bean
        public RouteLocator routeLocator() {
            return Mockito.mock(RouteLocator.class);
        }

        @Bean
        public ConsulClient consulClient() {
            return Mockito.mock(ConsulClient.class);
        }

        @Bean
        public ObjectMapper objectMapper() {
            return Mockito.mock(ObjectMapper.class);
        }

        @Bean
        public RedisConnectionFactory redisConnectionFactory() {
            return Mockito.mock(RedisConnectionFactory.class);
        }

        @Bean
        @Qualifier("RateLimit")
        public IMap<String, GridBucketState> hazelcastMap() {
            return Mockito.mock(IMap.class);
        }

        @Bean
        @Qualifier("RateLimit")
        public IgniteCache<String, GridBucketState> igniteCache() {
            return Mockito.mock(IgniteCache.class);
        }

        @Bean
        @Qualifier("RateLimit")
        public ReadWriteMap<String, GridBucketState> infinispanMap() {
            return Mockito.mock(ReadWriteMap.class);
        }
    }
}
