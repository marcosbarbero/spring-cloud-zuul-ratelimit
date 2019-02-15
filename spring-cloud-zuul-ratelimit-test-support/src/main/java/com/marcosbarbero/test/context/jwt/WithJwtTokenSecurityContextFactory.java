package com.marcosbarbero.test.context.jwt;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import com.marcosbarbero.test.context.JwtPrincipalAuthenticationToken;
import com.marcosbarbero.test.context.TestContextHolder;
import org.apache.commons.lang3.StringUtils;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.test.context.support.WithSecurityContextFactory;
import org.springframework.test.context.TestContext;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;

/**
 * A {@link WithSecurityContextFactory} that works with a {@link WithJwtToken}
 *
 * @author Eric Deandrea December 2018
 * @see WithJwtToken
 */
public class WithJwtTokenSecurityContextFactory implements WithSecurityContextFactory<WithJwtToken> {
    private static final String SCOPE_AUTHORITY_PREFIX = "SCOPE_";
    private static final Collection<String> WELL_KNOWN_SCOPE_ATTRIBUTE_NAMES = Arrays.asList("scope", "scp");

    @Override
    public SecurityContext createSecurityContext(WithJwtToken withJwtToken) {
        String tokenProducerMethodName = StringUtils.trimToNull(withJwtToken.tokenProducerMethod());
        Assert.hasText(tokenProducerMethodName, "tokenProducerMethod can not be null or empty");

        TestContext testContext = TestContextHolder.getContext();
        Assert.notNull(testContext, "testContext can not be null");

        Class<?> tokenProducerMethodClass = testContext.getTestClass();
        Assert.notNull(testContext.getTestClass(), "Can not figure out what class the test method is in");

        Object testInstance = testContext.getTestInstance();
        Assert.notNull(testInstance, () -> String.format("Instance of %s can not be resolved", tokenProducerMethodClass.getName()));

        Method method = Optional.ofNullable(ReflectionUtils.findMethod(tokenProducerMethodClass, tokenProducerMethodName))
                .orElseThrow(() -> new IllegalArgumentException(String.format("Unable to find method %s on class %s", tokenProducerMethodName, tokenProducerMethodClass.getName())));

        Assert.isTrue(
                Jwt.class.isAssignableFrom(method.getReturnType()),
                String.format(
                        "The return type of %s.%s is a %s. It must be a %s.",
                        tokenProducerMethodClass.getName(),
                        tokenProducerMethodName,
                        method.getReturnType().getName(),
                        Jwt.class.getName()
                )
        );

        ReflectionUtils.makeAccessible(method);
        Jwt token = (Jwt) ReflectionUtils.invokeMethod(method, testInstance);
        Assert.notNull(token, () -> String.format("Token returned by %s.%s is null and it can not be", tokenProducerMethodClass.getName(), tokenProducerMethodName));

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        String authenticationName = StringUtils.trimToNull(withJwtToken.authenticationName());
        Collection<GrantedAuthority> authorities = getAuthorities(withJwtToken, token);

        JwtAuthenticationToken jwtAuthenticationToken = new JwtAuthenticationToken(token, authorities);

        Authentication authentication = Optional.ofNullable(authenticationName)
                .map(authName -> new JwtPrincipalAuthenticationToken(jwtAuthenticationToken, authName))
                .map(JwtAuthenticationToken.class::cast)
                .orElse(jwtAuthenticationToken);

        context.setAuthentication(authentication);

        return context;
    }

    private static Collection<GrantedAuthority> getAuthorities(WithJwtToken withJwtToken, Jwt token) {
        Collection<String> scopes = Arrays.stream(withJwtToken.scopes())
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        scopes.addAll(getScopes(token));

        return scopes.stream()
                .filter(Objects::nonNull)
                .map(scope -> String.format("%s%s", SCOPE_AUTHORITY_PREFIX, scope))
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }

    private static Collection<String> getScopes(Jwt jwt) {
        // This method can be removed once upgrading to Spring Security 5.2 as there is now a standalone class
        // (org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter)
        // which can perform this logic
        for (String attributeName : WELL_KNOWN_SCOPE_ATTRIBUTE_NAMES) {
            Object scopes = jwt.getClaims().get(attributeName);

            if (scopes instanceof String) {
                if (org.springframework.util.StringUtils.hasText((String) scopes)) {
                    return Arrays.asList(((String) scopes).split(" "));
                }
                else {
                    return Collections.emptyList();
                }
            }
            else if (scopes instanceof Collection) {
                return (Collection<String>) scopes;
            }
        }

        return Collections.emptyList();
    }
}