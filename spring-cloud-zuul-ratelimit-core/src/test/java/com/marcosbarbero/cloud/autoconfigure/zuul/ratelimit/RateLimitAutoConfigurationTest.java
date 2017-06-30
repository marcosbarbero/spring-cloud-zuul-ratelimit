package com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit;

import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.filters.RateLimitFilter;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.mock.web.MockServletContext;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

import static com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.properties.RateLimitProperties.PREFIX;

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
        this.context.register(RateLimitAutoConfiguration.class);
        this.context.refresh();
    }

    @After
    public void close() {
        if (this.context != null) {
            this.context.close();
        }
    }

    @Test
    @Ignore
    public void testDefaultConfiguration() {
        Assert.assertNotNull(this.context.getBean(RateLimitFilter.class));
    }

}
