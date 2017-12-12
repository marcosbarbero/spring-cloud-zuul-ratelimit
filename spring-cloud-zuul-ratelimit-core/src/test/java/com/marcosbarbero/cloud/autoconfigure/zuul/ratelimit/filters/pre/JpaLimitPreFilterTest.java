package com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.filters.pre;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.Rate;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.repository.IRateLimiterErrorHandler;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.repository.springdata.JpaRateLimiter;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.repository.springdata.RateLimiterRepository;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.junit.Before;

/**
 * @author Marcos Barbero
 * @since 2017-06-30
 */
public class JpaLimitPreFilterTest extends BaseRateLimitPreFilterTest {

    @Before
    @Override
    public void setUp() {
        Map<String, Rate> repository = new ConcurrentHashMap<>();
        RateLimiterRepository rateLimiterRepository = mock(RateLimiterRepository.class);
        when(rateLimiterRepository.save(any(Rate.class))).thenAnswer(invocationOnMock -> {
            Rate rate = invocationOnMock.getArgument(0);
            repository.put(rate.getKey(), rate);
            return rate;
        });
        when(rateLimiterRepository.findOne(any())).thenAnswer(invocationOnMock -> {
            String key = invocationOnMock.getArgument(0);
            return repository.get(key);
        });
        IRateLimiterErrorHandler rateLimiterErrorHandler = mock(IRateLimiterErrorHandler.class);
        this.setRateLimiter(new JpaRateLimiter(rateLimiterErrorHandler, rateLimiterRepository));
        super.setUp();
    }
}
