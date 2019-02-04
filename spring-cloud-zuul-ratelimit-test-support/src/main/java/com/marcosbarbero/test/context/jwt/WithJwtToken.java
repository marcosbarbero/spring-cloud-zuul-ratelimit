package com.marcosbarbero.test.context.jwt;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.core.annotation.AliasFor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithSecurityContext;
import org.springframework.test.context.TestContext;

/**
 * When used with {@link org.springframework.security.test.context.annotation.SecurityTestExecutionListeners} this annotation can be
 * added to a test class or method to emulate running with a mocked {@link org.springframework.security.oauth2.jwt.Jwt} token.
 * <p>
 *   In order to work with {@link org.springframework.test.web.servlet.MockMvc} / {@link org.springframework.test.web.reactive.server.WebTestClient}
 *   the {@link SecurityContext} that is used will have the following properties:
 *
 *   <ul>
 *     <li>The {@link SecurityContext} created will be that of {@link SecurityContextHolder#createEmptyContext()}</li>
 *     <li>The type of {@link org.springframework.security.core.Authentication} object will be that of a {@link org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken}</li>
 *   </ul>
 * </p>
 *
 * @author Eric Deandrea December 2018
 */
@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
@WithSecurityContext(factory = WithJwtTokenSecurityContextFactory.class)
public @interface WithJwtToken {
    /**
     * A reference to a method which will produce a {@link org.springframework.security.oauth2.jwt.Jwt} token to be set as the principal
     * in the {@link org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken}.
     * <p>
     *   This method must be defined in the same class (or superclass) as the test method, return
     *   a {@link org.springframework.security.oauth2.jwt.Jwt}, and take in no arguments (effectively the method should be a
     *   {@link java.util.function.Supplier Supplier&lt;Jwt&gt;}). The method can be <strong>public</strong>, <strong>private</strong>,
     *   <strong>protected</strong>, or <strong>package-private</strong>.
     * </p>
     *
     * @return A reference to a method to produce a {@link org.springframework.security.oauth2.jwt.Jwt} token
     */
    String tokenProducerMethod();

    /**
     * Defines the {@link Authentication#getName()} that should be set on the {@link org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken}.
     * If not set will default to {@link org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken#getName()}.
     *
     * @return The overridden name to set on the {@link Authentication}
     */
    String authenticationName() default "";

    /**
     * The scopes to use. A {@link org.springframework.security.core.GrantedAuthority} will be created for each value. Each value will be
     * automatically prefixed with <strong>SCOPE_</strong>. These are added in addition to anything that is set in either the <strong>scp</strong>
     * or <strong>scope</strong> claims within the {@link org.springframework.security.oauth2.jwt.Jwt} returned by {@link #tokenProducerMethod()}.
     *
     * @return The list of scopes
     */
    String[] scopes() default {};

    /**
     * Determines when the {@link SecurityContext} is setup. The default is before {@link TestExecutionEvent#TEST_METHOD} which occurs during
     * {@link org.springframework.test.context.TestExecutionListener#beforeTestMethod(TestContext)}
     * @return the {@link TestExecutionEvent} to initialize before
     */
    @AliasFor(annotation = WithSecurityContext.class)
    TestExecutionEvent setupBefore() default TestExecutionEvent.TEST_METHOD;
}
