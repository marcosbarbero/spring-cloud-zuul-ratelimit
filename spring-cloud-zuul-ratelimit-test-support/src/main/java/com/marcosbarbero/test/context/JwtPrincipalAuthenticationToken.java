package com.marcosbarbero.test.context;

import org.springframework.lang.Nullable;
import org.springframework.security.core.SpringSecurityCoreVersion;
import org.springframework.security.core.Transient;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.Optional;

/**
 * {@link JwtAuthenticationToken} which take an override for the {@link #getName() principal}.
 *
 * @author Eric Deandrea November 2018
 */
@Transient
public class JwtPrincipalAuthenticationToken extends JwtAuthenticationToken {
    private static final long serialVersionUID = SpringSecurityCoreVersion.SERIAL_VERSION_UID;

    @Nullable
    private final String name;

    public JwtPrincipalAuthenticationToken(JwtAuthenticationToken underlyingToken) {
        this(underlyingToken, null);
    }

    public JwtPrincipalAuthenticationToken(JwtAuthenticationToken underlyingToken, @Nullable String name) {
        super(underlyingToken.getToken(), underlyingToken.getAuthorities());
        this.name = name;
    }

    @Override
    public String getName() {
        return Optional.ofNullable(this.name)
                .orElseGet(super::getName);
    }
}