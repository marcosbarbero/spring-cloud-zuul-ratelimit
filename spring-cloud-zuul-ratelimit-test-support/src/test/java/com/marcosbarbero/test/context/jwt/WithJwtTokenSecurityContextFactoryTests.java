package com.marcosbarbero.test.context.jwt;


import static org.assertj.core.api.Assertions.*;

import java.text.ParseException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.marcosbarbero.test.context.JwtPrincipalAuthenticationToken;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.apache.commons.lang3.exception.ExceptionUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.lang.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;


import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.PlainJWT;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = WithJwtTokenSecurityContextFactoryTests.WithJwtTokenSecurityContextFactoryTestsConfig.class)
@ActiveProfiles("WithJwtTokenSecurityContextFactoryTests")
@WithJwtToken(tokenProducerMethod = "createToken")
public class WithJwtTokenSecurityContextFactoryTests {
    @Autowired
    private AuthenticationFetcher authenticationFetcher;

    @Test
    public void withMethodSetAtClassLevel() {
        assertAuthentication(JwtAuthenticationToken.class, "user", Collections.emptyList(), createToken());
    }

    @WithJwtToken(tokenProducerMethod = "createToken")
    @Test
    public void withMethodSet() {
        assertAuthentication(JwtAuthenticationToken.class, "user", Collections.emptyList(), createToken());
    }

    @WithJwtToken(tokenProducerMethod = "createToken", authenticationName = "user1")
    @Test
    public void withMethodAndUserSet() {
        assertAuthentication(JwtPrincipalAuthenticationToken.class, "user1", Collections.emptyList(), createToken());
    }

    @WithJwtToken(tokenProducerMethod = "createTokenWithScopes")
    @Test
    public void withScopesDefined() {
        List<String> scopes = Arrays.asList("one", "two");
        List<String> scopeAuthorities = scopes.stream()
                .map(scope -> "SCOPE_" + scope)
                .collect(Collectors.toList());

        assertAuthentication(JwtAuthenticationToken.class, "user", scopeAuthorities, createToken(scopes));
    }

    @WithJwtToken(tokenProducerMethod = "createTokenWithScopes", scopes = { "a", "b" })
    @Test
    public void withScopesAndExtraScopesDefined() {
        List<String> scopes = Arrays.asList("one", "two");
        List<String> scopeAuthorities = new ArrayList<>(Arrays.asList("SCOPE_a", "SCOPE_b"));
        scopeAuthorities.addAll(scopes.stream()
                .map(scope -> "SCOPE_" + scope)
                .collect(Collectors.toList())
        );

        assertAuthentication(JwtAuthenticationToken.class, "user", scopeAuthorities, createToken(scopes));
    }

    private void assertAuthentication(Class<? extends JwtAuthenticationToken> expectedAuthenticationClass, String expectedUser, Collection<String> expectedAuthorities, Jwt expectedToken) {
        assertThat(this.authenticationFetcher.getAuthentication())
                .isNotNull()
                .isExactlyInstanceOf(expectedAuthenticationClass)
                .extracting(
                        auth -> ((JwtAuthenticationToken) auth).getToken().getHeaders(),
                        auth -> ((JwtAuthenticationToken) auth).getToken().getSubject(),
                        auth -> ((JwtAuthenticationToken) auth).getTokenAttributes().get("user"),
                        auth -> ((JwtAuthenticationToken) auth).getToken().getClaimAsStringList("scp"),
                        Authentication::getName,
                        Authentication::getAuthorities
                )
                .containsExactly(
                        expectedToken.getHeaders(),
                        expectedToken.getSubject(),
                        expectedToken.getClaimAsString("user"),
                        expectedToken.getClaimAsStringList("scp"),
                        expectedUser,
                        expectedAuthorities.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList())
                );
    }

    private static Jwt createToken() {
        return createToken(null);
    }

    private static Jwt createTokenWithScopes() {
        return createToken(Arrays.asList("one", "two"));
    }

    private static Jwt createToken(@Nullable List<String> scopes) {
        try {
            JWTClaimsSet.Builder claimsBuilder = new JWTClaimsSet.Builder()
                    .issueTime(new Date())
                    .expirationTime(Date.from(Instant.now().plusSeconds(120L)))
                    .issuer("https://github.com/spring-projects/spring-security")
                    .claim("user", "user")
                    .subject("user");

            Optional.ofNullable(scopes)
                    .ifPresent(theScopes -> claimsBuilder.claim("scp", theScopes));

            PlainJWT jwt = new PlainJWT(claimsBuilder.build());

            return new Jwt(
                    jwt.serialize(),
                    jwt.getJWTClaimsSet().getIssueTime().toInstant(),
                    jwt.getJWTClaimsSet().getExpirationTime().toInstant(),
                    jwt.getHeader().toJSONObject(),
                    jwt.getJWTClaimsSet().getClaims()
            );
        }
        catch (ParseException ex) {
            return ExceptionUtils.rethrow(ex);
        }
    }

    private static class AuthenticationFetcher {
        public Authentication getAuthentication() {
            return SecurityContextHolder.getContext().getAuthentication();
        }
    }

    @Configuration
    @Profile("WithJwtTokenSecurityContextFactoryTests")
    static class WithJwtTokenSecurityContextFactoryTestsConfig {
        @Bean
        public AuthenticationFetcher authenticationFetcher() {
            return new AuthenticationFetcher();
        }
    }
}
