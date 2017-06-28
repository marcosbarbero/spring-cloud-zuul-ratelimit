package com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.properties;

import org.junit.Test;

/**
 * @author Marcos Barbero
 * @since 2017-06-28
 */
public class PolicyTest {

    @Test(expected = IllegalArgumentException.class)
    public void testNoLimit() throws Exception {
        Policy policy = new Policy();
        policy.setRefreshInterval(15L);
        policy.afterPropertiesSet();
    }
}
