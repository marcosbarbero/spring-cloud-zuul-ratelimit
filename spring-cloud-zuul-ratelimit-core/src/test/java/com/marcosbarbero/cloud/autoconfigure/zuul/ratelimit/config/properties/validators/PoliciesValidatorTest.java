package com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.properties.validators;

import static org.assertj.core.api.Assertions.assertThat;

import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.properties.RateLimitProperties;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.properties.RateLimitProperties.Policy;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.properties.RateLimitRepository;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.properties.RateLimitType;
import java.time.Duration;
import java.util.Set;
import javax.validation.ConstraintValidatorContext;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

public class PoliciesValidatorTest {

    private Validator validator;
    private PoliciesValidator target;

    @Mock
    private ConstraintValidatorContext constraintValidatorContext;
    private RateLimitProperties properties;

    private Policy getPolicy(Long limit, Duration quota) {
        Policy policy = new Policy();
        policy.setLimit(limit);
        policy.setQuota(quota);
        return policy;
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
        properties.setKeyPrefix("prefix");
        Policy policy = getPolicy(null, null);
        properties.getDefaultPolicyList().add(policy);
        properties.getPolicyList().put("key", Lists.newArrayList(policy));
        Set<ConstraintViolation<RateLimitProperties>> violations = validator.validate(properties);
        assertThat(violations).hasSize(2);
    }

    @Test
    public void testValidOnPolicyWithLimitNoQuota() {
        properties.setKeyPrefix("prefix");
        Policy policy = getPolicy(1L, null);
        properties.getDefaultPolicyList().add(policy);
        properties.getPolicyList().put("key", Lists.newArrayList(policy));
        Set<ConstraintViolation<RateLimitProperties>> violations = validator.validate(properties);
        assertThat(violations).isEmpty();
    }

    @Test
    public void testValidOnPolicyWithQuotaNoLimit() {
        properties.setKeyPrefix("prefix");
        Policy policy = getPolicy(null, Duration.ofSeconds(1));
        properties.getDefaultPolicyList().add(policy);
        properties.getPolicyList().put("key", Lists.newArrayList(policy));
        Set<ConstraintViolation<RateLimitProperties>> violations = validator.validate(properties);
        assertThat(violations).isEmpty();
    }

    @Test
    public void testValidOnPolicyWithLimitAndRole() {
        properties.setKeyPrefix("prefix");
        Policy policy = getPolicy(1L, null);
        policy.getType().add(new Policy.MatchType(RateLimitType.ROLE, "user"));
        properties.getDefaultPolicyList().add(policy);
        properties.getPolicyList().put("key", Lists.newArrayList(policy));
        Set<ConstraintViolation<RateLimitProperties>> violations = validator.validate(properties);
        assertThat(violations).isEmpty();
    }

    @Test
    public void testValidOnPolicyWithLimitAndURLPattern() {
        properties.setKeyPrefix("prefix");
        Policy policy = getPolicy(1L, null);
        policy.getType().add(new Policy.MatchType(RateLimitType.URL_PATTERN, "/user/[0-9]+"));
        properties.getDefaultPolicyList().add(policy);
        properties.getPolicyList().put("key", Lists.newArrayList(policy));
        Set<ConstraintViolation<RateLimitProperties>> violations = validator.validate(properties);
        assertThat(violations).isEmpty();
    }

    @Test
    public void testValidOnPolicyWithLimitAndRoleWithoutMatcher() {
        properties.setKeyPrefix("prefix");
        Policy policy = getPolicy(1L, null);
        policy.getType().add(new Policy.MatchType(RateLimitType.ROLE, null));
        properties.getDefaultPolicyList().add(policy);
        properties.getPolicyList().put("key", Lists.newArrayList(policy));
        Set<ConstraintViolation<RateLimitProperties>> violations = validator.validate(properties);
        assertThat(violations).hasSize(2);
    }

    @Test
    public void testValidOnPolicyWithLimitAndMethod() {
        properties.setKeyPrefix("prefix");
        Policy policy = getPolicy(1L, null);
        policy.getType().add(new Policy.MatchType(RateLimitType.HTTP_METHOD, "GET"));
        properties.getDefaultPolicyList().add(policy);
        properties.getPolicyList().put("key", Lists.newArrayList(policy));
        Set<ConstraintViolation<RateLimitProperties>> violations = validator.validate(properties);
        assertThat(violations).isEmpty();
    }

    @Test
    public void testValidOnPolicyWithLimitAndHeader() {
        properties.setKeyPrefix("prefix");
        Policy policy = getPolicy(1L, null);
        policy.getType().add(new Policy.MatchType(RateLimitType.HTTP_HEADER, "customHeader"));
        properties.getDefaultPolicyList().add(policy);
        properties.getPolicyList().put("key", Lists.newArrayList(policy));
        Set<ConstraintViolation<RateLimitProperties>> violations = validator.validate(properties);
        assertThat(violations).isEmpty();
    }

    @Test
    public void testValidOnPolicyWithLimitAndMethodWithoutMatcher() {
        properties.setKeyPrefix("prefix");
        Policy policy = getPolicy(1L, null);
        policy.getType().add(new Policy.MatchType(RateLimitType.ROLE, null));
        properties.getDefaultPolicyList().add(policy);
        properties.getPolicyList().put("key", Lists.newArrayList(policy));
        Set<ConstraintViolation<RateLimitProperties>> violations = validator.validate(properties);
        assertThat(violations).hasSize(2);
    }
}