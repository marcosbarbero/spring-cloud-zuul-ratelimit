package com.marcosbarbero.zuul.filters.pre.ratelimit.config;

/**
 * Represents a view of rate limit in a giving time for a user.
 * <p>
 * limit - How many requests can be executed by the user. Maps to X-RateLimit-Limit header
 * remaining - How many requests are still left on the current window. Maps to X-RateLimit-Remaining header
 * reset - Epoch when the rate is replenished by limit. Maps to X-RateLimit-Reset header
 *
 * @author Marcos Barbero
 */
public class Rate {
    private Long limit;

    private Long remaining;

    private Long reset;

    public Rate(Long limit, Long remaining, Long reset) {
        this.limit = limit;
        this.remaining = remaining;
        this.reset = reset;
    }

    public Long getLimit() {
        return limit;
    }

    public void setLimit(Long limit) {
        this.limit = limit;
    }

    public Long getRemaining() {
        return remaining;
    }

    public void setRemaining(Long remaining) {
        this.remaining = remaining;
    }

    public Long getReset() {
        return reset;
    }

    public void setReset(Long reset) {
        this.reset = reset;
    }
}
