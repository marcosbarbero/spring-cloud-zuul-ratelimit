package com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.properties.validators;

import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.properties.RateLimitProperties;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.properties.RateLimitProperties.Policy;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.properties.RateLimitRepository;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.properties.RateLimitType;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import javax.validation.*;
import java.time.Duration;
import java.util.Collections;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class PoliciesValidatorTest {

    private Validator validator;
    private PoliciesValidator target;

    @Mock
    private ConstraintValidatorContext constraintValidatorContext;
    private RateLimitProperties properties;

    private Policy getPolicy(Long limit, Duration quota, Policy.MatchType matchType) {
        Policy policy = new Policy();
        policy.setLimit(limit);
        policy.setQuota(quota);

        if (matchType != null) {
            policy.getType().add(matchType);
        }

        return policy;
    }

    private void executeValidations(Policy policy, int numberOfViolations) {
        properties.setKeyPrefix("prefix");
        properties.getDefaultPolicyList().add(policy);
        properties.getPolicyList().put("key", Collections.singletonList(policy));
        Set<ConstraintViolation<RateLimitProperties>> violations = validator.validate(properties);
        assertThat(violations).hasSize(numberOfViolations);
    }

    @BeforeEach
    public void setUp() {
        target = new PoliciesValidator();
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        properties = new RateLimitProperties();
        properties.setRepository(RateLimitRepository.BUCKET4J_JCACHE);
    }

    @Test
    public void testInvalidWithNonMatchingObject() {
        boolean valid = target.isValid(new Object(), constraintValidatorContext);
        assertThat(valid).isFalse();
    }

    @Test
    public void testValidWithNoPolicies() {
        properties.setKeyPrefix("prefix");
        Set<ConstraintViolation<RateLimitProperties>> violations = validator.validate(properties);
        assertThat(violations).isEmpty();
    }

    @Test
    public void testInvalidOnPolicyWithNoLimitOrQuota() {
        Policy policy = getPolicy(null, null, null);
        executeValidations(policy, 2);
    }

    @Test
    public void testValidOnPolicyWithLimitNoQuota() {
        Policy policy = getPolicy(1L, null, null);
        executeValidations(policy, 0);
    }

    @Test
    public void testValidOnPolicyWithQuotaNoLimit() {
        Policy policy = getPolicy(null, Duration.ofSeconds(1), null);
        executeValidations(policy, 0);
    }

    @Test
    public void testValidOnPolicyWithLimitAndRole() {
        Policy policy = getPolicy(1L, null, new Policy.MatchType(RateLimitType.ROLE, "user"));
        executeValidations(policy, 0);
    }

    @Test
    public void testValidOnPolicyWithLimitAndRoleWithoutMatcher() {
        Policy policy = getPolicy(1L, null, new Policy.MatchType(RateLimitType.ROLE, null));
        executeValidations(policy, 2);
    }

    @Test
    public void testValidOnPolicyWithLimitAndClientId() {
        Policy policy = getPolicy(1L, null, new Policy.MatchType(RateLimitType.CLIENT_ID, "optimus_prime"));
        executeValidations(policy, 0);
    }

    @Test
    public void testValidOnPolicyWithLimitAndClientIdWithoutMatcher() {
        Policy policy = getPolicy(1L, null, new Policy.MatchType(RateLimitType.CLIENT_ID, null));
        executeValidations(policy, 0);
    }

    @Test
    public void testValidOnPolicyWithLimitAndURLPattern() {
        Policy policy = getPolicy(1L, null, new Policy.MatchType(RateLimitType.URL_PATTERN, "/user/[0-9]+"));
        executeValidations(policy, 0);
    }

    @Test
    public void testValidOnPolicyWithLimitAndMethod() {
        Policy policy = getPolicy(1L, null, new Policy.MatchType(RateLimitType.HTTP_METHOD, "GET"));
        executeValidations(policy, 0);
    }

    @Test
    public void testValidOnPolicyWithLimitAndMethodWithoutMatcher() {
        Policy policy = getPolicy(1L, null, new Policy.MatchType(RateLimitType.HTTP_METHOD, null));
        executeValidations(policy, 0);
    }


    @Test
    public void testValidOnPolicyWithLimitAndHeader() {
        properties.setKeyPrefix("prefix");
        Policy policy = getPolicy(1L, null, null);
        policy.getType().add(new Policy.MatchType(RateLimitType.HTTP_HEADER, "customHeader"));
        properties.getDefaultPolicyList().add(policy);
        properties.getPolicyList().put("key", Lists.newArrayList(policy));
        Set<ConstraintViolation<RateLimitProperties>> violations = validator.validate(properties);
        assertThat(violations).isEmpty();
    }

    @Test
    public void testValidOnPolicyWithLimitAndHeaderWithoutMatcher() {
        properties.setKeyPrefix("prefix");
        Policy policy = getPolicy(1L, null, null);
        policy.getType().add(new Policy.MatchType(RateLimitType.HTTP_HEADER, null));
        properties.getDefaultPolicyList().add(policy);
        properties.getPolicyList().put("key", Lists.newArrayList(policy));
        Set<ConstraintViolation<RateLimitProperties>> violations = validator.validate(properties);
        assertThat(violations).hasSize(2);
    }
}
