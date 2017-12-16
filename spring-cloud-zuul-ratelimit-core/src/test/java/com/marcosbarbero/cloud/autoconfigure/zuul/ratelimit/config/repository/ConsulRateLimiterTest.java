package com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.Response;
import com.ecwid.consul.v1.kv.model.GetValue;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.Rate;
import java.io.IOException;
import java.util.Base64;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class ConsulRateLimiterTest extends BaseRateLimiterTest {

    @Mock
    private RateLimiterErrorHandler rateLimiterErrorHandler;
    @Mock
    private ConsulClient consulClient;
    @Mock
    private ObjectMapper objectMapper;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        Map<String, String> repository = Maps.newHashMap();
        when(consulClient.setKVValue(any(), any())).thenAnswer(invocation -> {
            String key = invocation.getArgument(0);
            String value = invocation.getArgument(1);
            repository.put(key, value);
            return null;
        });
        when(consulClient.getKVValue(any())).thenAnswer(invocation -> {
            String key = invocation.getArgument(0);
            GetValue getValue = new GetValue();
            String value = repository.get(key);
            getValue.setValue(value != null ? Base64.getEncoder().encodeToString(value.getBytes()) : null);
            return new Response<>(getValue, 1L, true, 1L);
        });
        ObjectMapper objectMapper = new ObjectMapper();
        target = new ConsulRateLimiter(rateLimiterErrorHandler, consulClient, objectMapper);
    }

    @Test
    public void testGetRateException() throws IOException {
        GetValue getValue = new GetValue();
        getValue.setValue("");
        when(consulClient.getKVValue(any())).thenReturn(new Response<>(getValue, 1L, true, 1L));
        when(objectMapper.readValue(anyString(), eq(Rate.class))).thenThrow(new IOException());
        ConsulRateLimiter consulRateLimiter = new ConsulRateLimiter(rateLimiterErrorHandler, consulClient, objectMapper);

        Rate rate = consulRateLimiter.getRate("");
        assertThat(rate).isNull();
    }

    @Test
    public void testSaveRateException() throws IOException {
        JsonProcessingException jsonProcessingException = Mockito.mock(JsonProcessingException.class);
        when(objectMapper.writeValueAsString(any())).thenThrow(jsonProcessingException);
        ConsulRateLimiter consulRateLimiter = new ConsulRateLimiter(rateLimiterErrorHandler, consulClient, objectMapper);

        consulRateLimiter.saveRate(null);
        verifyZeroInteractions(consulClient);
    }
}