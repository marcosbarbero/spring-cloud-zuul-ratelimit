package com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.it;

import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.RateLimiter;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.repository.bucket4j.Bucket4jJCacheRateLimiter;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Marcos Barbero
 * @since 2019-03-03
 */
@Ignore
@TestPropertySource(properties = {"zuul.ratelimit.repository=bucket4j_jcache"})
public class Bucket4jJCacheIntegrationTest extends AbstractBaseUnsecuredIntegrationTests {

    @Test
    @Override
    public void testRateLimiter() {
        assertThat(context.getBean(RateLimiter.class)).isExactlyInstanceOf(Bucket4jJCacheRateLimiter.class);
    }
}
