package com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config;

import com.netflix.zuul.context.RequestContext;

import javax.servlet.http.HttpServletRequest;

/**
 * DefaultUserIdGetter
 *
 * @author doob  fudali113@gmail.com
 * @date 2017/11/30
 */
public class DefaultUserIdGetter implements UserIdGetter {
    @Override
    public String getUserId(RequestContext context) {
        HttpServletRequest request = context.getRequest();
        return request.getRemoteUser() != null ? request.getRemoteUser() : ANONYMOUS_USER;
    }
}
