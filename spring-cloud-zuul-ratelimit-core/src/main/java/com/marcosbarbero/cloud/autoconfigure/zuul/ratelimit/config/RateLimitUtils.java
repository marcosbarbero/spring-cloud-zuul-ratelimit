package com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Liel Chayoun
 */
public interface RateLimitUtils {

    String getUser(HttpServletRequest request);

    String getRemoteAddress(HttpServletRequest request);
}
