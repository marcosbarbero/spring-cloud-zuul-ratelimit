package com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.repository.springdata;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.google.common.collect.Maps;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.Rate;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.repository.BaseRateLimiterTest;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.repository.RateLimiterErrorHandler;
import java.util.Map;
import org.junit.Before;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class JpaRateLimiterTest extends BaseRateLimiterTest {

    @Mock
    private RateLimiterErrorHandler rateLimiterErrorHandler;
    @Mock
    private RateLimiterRepository rateLimiterRepository;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        Map<String, Rate> repository = Maps.newHashMap();
        when(rateLimiterRepository.save(any(Rate.class))).thenAnswer(invocationOnMock -> {
            Rate rate = invocationOnMock.getArgument(0);
            repository.put(rate.getKey(), rate);
            return rate;
        });
        when(rateLimiterRepository.findOne(any())).thenAnswer(invocationOnMock -> {
            String key = invocationOnMock.getArgument(0);
            return repository.get(key);
        });

        target = new JpaRateLimiter(rateLimiterErrorHandler, rateLimiterRepository);
    }
}