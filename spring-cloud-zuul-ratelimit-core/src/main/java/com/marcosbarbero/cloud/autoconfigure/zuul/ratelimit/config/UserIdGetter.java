package com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config;

import com.netflix.zuul.context.RequestContext;

/**
 * UserIdGetter
 *
 * @author doob  fudali113@gmail.com
 * @date 2017/11/30
 */
public interface UserIdGetter {

    String ANONYMOUS_USER = "anonymous";

    /**
     * 根据context获取用户信息
     * @param context The {@link RequestContext}
     * @return user id
     */
    String getUserId(RequestContext context);
}
