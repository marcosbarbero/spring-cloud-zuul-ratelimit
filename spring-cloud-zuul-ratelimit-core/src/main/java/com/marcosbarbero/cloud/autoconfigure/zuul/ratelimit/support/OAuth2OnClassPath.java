package com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.support;

import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.util.ClassUtils;

@Order(Ordered.HIGHEST_PRECEDENCE)
public final class OAuth2OnClassPath implements Condition {

    private static final String OAUTH2_AUTHENTICATION = "org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken";
    private static final String JWT_AUTHENTICATION = "org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken";

    @Override
    public boolean matches(@NotNull ConditionContext context, @NotNull AnnotatedTypeMetadata metadata) {
        return isPresent(OAUTH2_AUTHENTICATION) || isPresent(JWT_AUTHENTICATION);
    }

    private boolean isPresent(String className) {
        ClassLoader classLoader = ClassUtils.getDefaultClassLoader();
        try {
            resolve(className, classLoader);
            return true;
        } catch (Throwable ex) {
            return false;
        }
    }

    private Class<?> resolve(String className, ClassLoader classLoader) throws ClassNotFoundException {
        if (classLoader != null) {
            return classLoader.loadClass(className);
        }
        return Class.forName(className);
    }
}
