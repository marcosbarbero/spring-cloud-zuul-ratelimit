package com.barbero.zuul.filters.pre.ratelimit.config.redis;

import com.barbero.zuul.filters.pre.ratelimit.config.Policy;
import com.barbero.zuul.filters.pre.ratelimit.config.RateLimiter;
import com.barbero.zuul.filters.pre.ratelimit.config.Rate;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author Marcos Barbero
 */
public class RedisRateLimiter implements RateLimiter {
    private RedisTemplate template;

    public RedisRateLimiter(RedisTemplate template) {
        this.template = template;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Rate consume(final Policy policy, final String key) {
        Long now = System.currentTimeMillis();
        Long time = (now / (1000 * policy.getRefreshInterval()));
        final String tempKey = key + ":" + time;
        List results = (List) template.execute(new SessionCallback() {
            @Override
            public List<Object> execute(RedisOperations ops) throws DataAccessException {
                ops.multi();
                ops.boundValueOps(tempKey).increment(1L);
                if (ops.getExpire(tempKey) == null) {
                    ops.expire(tempKey, policy.getRefreshInterval(), TimeUnit.SECONDS);
                }
                return ops.exec();
            }
        });
        Long current = (Long) results.get(0);
        return new Rate(policy.getLimit(), Math.max(-1, policy.getLimit() - current), time * (policy.getRefreshInterval() * 1000));
    }
}
