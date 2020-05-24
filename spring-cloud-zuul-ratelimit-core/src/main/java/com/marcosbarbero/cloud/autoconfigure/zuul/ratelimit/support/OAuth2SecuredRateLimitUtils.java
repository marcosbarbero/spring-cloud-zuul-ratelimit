package com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.support;

import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.properties.RateLimitProperties;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

public class OAuth2SecuredRateLimitUtils extends SecuredRateLimitUtils {

    private static final String EMPTY_STRING = "";

    public OAuth2SecuredRateLimitUtils(final RateLimitProperties properties) {
        super(properties);
    }

    @Override
    public String getClientId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication instanceof OAuth2AuthenticationToken
                || authentication instanceof JwtAuthenticationToken) {
            return authentication.getName();
        }

        // Avoid NPE
        return EMPTY_STRING;
    }
}
