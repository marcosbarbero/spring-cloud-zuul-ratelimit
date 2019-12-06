package com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.support;

import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.properties.RateLimitProperties.Policy;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.filters.RateLimitPreFilter;
import java.util.Objects;
import org.springframework.context.ApplicationEvent;

/**
 * Event raised when a rate limit exceeded.
 *
 * @author vasilaio
 */
public class RateLimitEvent extends ApplicationEvent {
    private static final long serialVersionUID = 5241485625003998587L;

    private final Policy policy;
    private final String remoteAddress;

    public RateLimitEvent(RateLimitPreFilter source, Policy policy, String remoteAddress) {
        super(source);
        this.policy = Objects.requireNonNull(policy, "Policy should not be null.");
        this.remoteAddress = Objects.requireNonNull(remoteAddress, "RemoteAddress should not be null.");
    }

    /**
     * Return the {@link Policy} which raised the event.
     *
     * @return the {@link Policy}
     */
    public Policy getPolicy() {
        return this.policy;
    }

    /**
     * Return the remote IP address.
     *
     * @return the remote IP address
     */
    public String getRemoteAddress() {
        return this.remoteAddress;
    }
}