package com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.it;

import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.RateLimiter;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.repository.springdata.JpaRateLimiter;
import org.junit.Test;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Marcos Barbero
 * @since 2019-03-03
 */
@TestPropertySource(properties = {"zuul.ratelimit.repository=jpa"})
public class SpringDataIntegrationTest extends AbstractBaseUnsecuredIntegrationTests {

    @Test
    @Override
    public void testRateLimiter() {
        assertThat(context.getBean(RateLimiter.class)).isExactlyInstanceOf(JpaRateLimiter.class);
    }
}
