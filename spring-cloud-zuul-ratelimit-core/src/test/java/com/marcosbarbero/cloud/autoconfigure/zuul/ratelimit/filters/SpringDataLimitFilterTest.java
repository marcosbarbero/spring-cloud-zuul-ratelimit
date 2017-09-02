package com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.filters;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.Rate;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.repository.springdata.IRateLimiterRepository;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.repository.springdata.SpringDataRateLimiter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.junit.Before;

/**
 * @author Marcos Barbero
 * @since 2017-06-30
 */
public class SpringDataLimitFilterTest extends BaseRateLimitFilterTest {

    @Before
    @Override
    public void setUp() {
        Map<String, Rate> repository = new ConcurrentHashMap<>();
        IRateLimiterRepository rateLimiterRepository = mock(IRateLimiterRepository.class);
        when(rateLimiterRepository.save(any(Rate.class))).thenAnswer(invocationOnMock -> {
            Rate rate = invocationOnMock.getArgument(0);
            repository.put(rate.getKey(), rate);
            return rate;
        });
        when(rateLimiterRepository.findOne(any())).thenAnswer(invocationOnMock -> {
            String key = invocationOnMock.getArgument(0);
            return repository.get(key);
        });
        this.setRateLimiter(new SpringDataRateLimiter(rateLimiterRepository));
        super.setUp();
    }
}
