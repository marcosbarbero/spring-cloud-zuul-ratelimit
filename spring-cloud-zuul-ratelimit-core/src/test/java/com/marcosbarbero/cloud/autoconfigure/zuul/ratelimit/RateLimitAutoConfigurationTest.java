package com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit;

import static com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.properties.RateLimitProperties.PREFIX;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.ecwid.consul.v1.ConsulClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hazelcast.core.IMap;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.RateLimitKeyGenerator;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.RateLimitUtils;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.RateLimiter;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.properties.RateLimitProperties;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.properties.RateLimitProperties.Policy;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.repository.ConsulRateLimiter;
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
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.context.annotation.UserConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.cloud.netflix.zuul.filters.RouteLocator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;

/**
 * @author Marcos Barbero
 */
public class RateLimitAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withPropertyValues(PREFIX + ".enabled=true")
            .withConfiguration(UserConfigurations.of(Conf.class))
            .withConfiguration(AutoConfigurations.of(RateLimitAutoConfiguration.class));

    @Test
    public void testStringToMatchTypeConverter() {
        contextRunner.withPropertyValues(PREFIX + ".repository=BUCKET4J_JCACHE")
                .run((context) -> assertThat(context).hasSingleBean(StringToMatchTypeConverter.class));
    }

    @Test
    public void testRateLimitUtils() {
        contextRunner.withPropertyValues(PREFIX + ".repository=BUCKET4J_JCACHE")
                .run((context) -> assertThat(context).hasSingleBean(RateLimitUtils.class));
    }

    @Test
    public void testZuulFilters() {
        contextRunner.withPropertyValues(PREFIX + ".repository=BUCKET4J_JCACHE")
                .run(context -> {
                    assertThat(context).getBeanNames(ZuulFilter.class).hasSize(2);
                    assertThat(context).getBeanNames(ZuulFilter.class)
                            .containsExactly("rateLimiterPreFilter", "rateLimiterPostFilter");
                });
    }

    @Test
    public void testConsulRateLimiterByProperty() {
        contextRunner.withPropertyValues(PREFIX + ".repository=CONSUL", "spring.cloud.consul.enabled=true")
                .run(context ->
                        assertThat(context).getBean(RateLimiter.class).isExactlyInstanceOf(ConsulRateLimiter.class));
    }

    @Test
    public void testRedisRateLimiterByProperty() {
        contextRunner.withPropertyValues(PREFIX + ".repository=REDIS")
                .run(context -> assertThat(context).getBean(RateLimiter.class).isExactlyInstanceOf(RedisRateLimiter.class));
    }

    @Test
    public void testBucket4jJCacheRateLimiterByProperty() {
        contextRunner.withPropertyValues(PREFIX + ".repository=BUCKET4J_JCACHE")
                .run(context -> assertThat(context).getBean(RateLimiter.class).isExactlyInstanceOf(Bucket4jJCacheRateLimiter.class));
    }

    @Test
    public void testBucket4jHazelcastRateLimiterByProperty() {
        contextRunner.withPropertyValues(PREFIX + ".repository=BUCKET4J_HAZELCAST")
                .run(context -> assertThat(context).getBean(RateLimiter.class).isExactlyInstanceOf(Bucket4jHazelcastRateLimiter.class));
    }

    @Test
    public void testBucket4jIgniteRateLimiterByProperty() {
        contextRunner.withPropertyValues(PREFIX + ".repository=BUCKET4J_IGNITE")
                .run(context -> assertThat(context).getBean(RateLimiter.class).isExactlyInstanceOf(Bucket4jIgniteRateLimiter.class));
    }

    @Test
    public void testBucket4jInfinispanRateLimiterByProperty() {
        contextRunner.withPropertyValues(PREFIX + ".repository=BUCKET4J_INFINISPAN")
                .run(context -> assertThat(context).getBean(RateLimiter.class).isExactlyInstanceOf(Bucket4jInfinispanRateLimiter.class));
    }

    @Test
    public void testDefaultRateLimitKeyGenerator() {
        contextRunner.withPropertyValues(PREFIX + ".repository=BUCKET4J_JCACHE")
                .run(context -> assertThat(context).getBean(RateLimitKeyGenerator.class).isExactlyInstanceOf(DefaultRateLimitKeyGenerator.class));
    }

    @Test
    public void testPolicyAdjuster() {
        contextRunner.withPropertyValues(PREFIX + ".repository=BUCKET4J_JCACHE",
                PREFIX + ".defaultPolicyList[0].limit=3",
                PREFIX + ".defaultPolicyList[1].limit=4",
                PREFIX + ".policyList.a[0].limit=5",
                PREFIX + ".policyList.a[1].limit=6")
                .run(context -> {
                    RateLimitProperties rateLimitProperties = context.getBean(RateLimitProperties.class);
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
                });
    }

    @Configuration
    public static class Conf {

        @Bean
        public RouteLocator routeLocator() {
            return mock(RouteLocator.class);
        }

        @Bean
        public ConsulClient consulClient() {
            return mock(ConsulClient.class);
        }

        @Bean
        public ObjectMapper objectMapper() {
            return mock(ObjectMapper.class);
        }

        @Bean
        public RedisConnectionFactory redisConnectionFactory() {
            return mock(RedisConnectionFactory.class);
        }

        @Bean
        @Qualifier("RateLimit")
        @SuppressWarnings("unchecked")
        public IMap<String, GridBucketState> hazelcastMap() {
            return mock(IMap.class);
        }

        @Bean
        @Qualifier("RateLimit")
        @SuppressWarnings("unchecked")
        public IgniteCache<String, GridBucketState> igniteCache() {
            return mock(IgniteCache.class);
        }

        @Bean
        @Qualifier("RateLimit")
        @SuppressWarnings("unchecked")
        public ReadWriteMap<String, GridBucketState> infinispanMap() {
            return mock(ReadWriteMap.class);
        }

    }
}
