package com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.repository;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.Response;
import com.ecwid.consul.v1.kv.model.GetValue;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;
import java.util.Base64;
import java.util.Map;
import org.junit.Before;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class ConsulRateLimiterTest extends BaseRateLimiterTest {

    @Mock
    private ConsulClient consulClient;

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
        target = new ConsulRateLimiter(consulClient, objectMapper);
    }
}