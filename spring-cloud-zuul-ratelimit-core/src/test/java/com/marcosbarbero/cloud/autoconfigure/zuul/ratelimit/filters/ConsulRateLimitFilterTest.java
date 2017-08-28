package com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.filters;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.Response;
import com.ecwid.consul.v1.kv.model.GetValue;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.Rate;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.repository.ConsulRateLimiter;

import org.junit.Before;
import org.junit.Test;

import java.util.Date;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Marcos Barbero
 * @since 2017-08-28
 */
public class ConsulRateLimitFilterTest extends BaseRateLimitFilterTest {

    private ConsulClient consulClient = mock(ConsulClient.class);
    private ObjectMapper objectMapper = new ObjectMapper();

    private Rate rate() {
        return new Rate(30L, -1L, 100L, new Date(System.currentTimeMillis() + SECONDS.toMillis(10)));
    }

    @Before
    @Override
    public void setUp() {
        this.setRateLimiter(new ConsulRateLimiter(this.consulClient, this.objectMapper));
        super.setUp();
    }

    @Test
    @Override
    @SuppressWarnings("unchecked")
    public void testRateLimitExceedCapacity() throws Exception {
        Response<GetValue> response = mock(Response.class);
        GetValue getValue = mock(GetValue.class);
        when(this.consulClient.getKVValue(anyString())).thenReturn(response);
        when(response.getValue()).thenReturn(getValue);
        when(getValue.getValue()).thenReturn(this.objectMapper.writeValueAsString(this.rate()));
        super.testRateLimitExceedCapacity();
    }

    @Test
    @Override
    @SuppressWarnings("unchecked")
    public void testRateLimit() throws Exception {
//        BoundValueOperations ops = mock(BoundValueOperations.class);
//        when(this.redisTemplate.boundValueOps(anyString())).thenReturn(ops);
//        when(ops.increment(anyLong())).thenReturn(2L);
//
//
//        this.request.setRequestURI("/serviceA");
//        this.request.setRemoteAddr("10.0.0.100");
//
//        assertTrue(this.filter.shouldFilter());
//
//        for (int i = 0; i < 2; i++) {
//            this.filter.run();
//        }
//
//        String remaining = this.response.getHeader(RateLimitFilter.Headers.REMAINING);
//        assertEquals("0", remaining);
//
//        TimeUnit.SECONDS.sleep(2);
//
//        when(ops.increment(anyLong())).thenReturn(1L);
//        this.filter.run();
//        remaining = this.response.getHeader(RateLimitFilter.Headers.REMAINING);
//        assertEquals(remaining, "1");
    }
}
