package com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.properties.validators;

import static org.assertj.core.api.Assertions.assertThat;

import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.properties.RateLimitProperties;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.properties.RateLimitProperties.Policy;
import java.util.Set;
import javax.validation.ConstraintValidatorContext;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

public class PoliciesValidatorTest {

    private Validator validator;
    private PoliciesValidator target;

    @Mock
    private ConstraintValidatorContext constraintValidatorContext;

    private static Policy getPolicy(Long limit, Long quota) {
        Policy policy = new Policy();
        policy.setLimit(limit);
        policy.setQuota(quota);
        return policy;
    }

    @Before
    public void setUp() {
        target = new PoliciesValidator();
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    public void testInvalidWithNonMatchingObject() {
        boolean valid = target.isValid(new Object(), constraintValidatorContext);
        assertThat(valid).isFalse();
    }

    @Test
    public void testValidWithNoPolicies() {
        RateLimitProperties properties = new RateLimitProperties();
        properties.setKeyPrefix("prefix");
        Set<ConstraintViolation<RateLimitProperties>> violations = validator.validate(properties);
        assertThat(violations).isEmpty();
    }

    @Test
    public void testInvalidOnPolicyWithNoLimitOrQuota() {
        RateLimitProperties properties = new RateLimitProperties();
        properties.setKeyPrefix("prefix");
        Policy policy = getPolicy(null, null);
        properties.setDefaultPolicy(policy);
        properties.getDefaultPolicyList().add(policy);
        properties.getPolicyList().put("key", Lists.newArrayList(policy));
        properties.getPolicies().put("key", policy);
        Set<ConstraintViolation<RateLimitProperties>> violations = validator.validate(properties);
        assertThat(violations).hasSize(4);
    }

    @Test
    public void testValidOnPolicyWithLimitNoQuota() {
        RateLimitProperties properties = new RateLimitProperties();
        properties.setKeyPrefix("prefix");
        Policy policy = getPolicy(1L, null);
        properties.setDefaultPolicy(policy);
        properties.getDefaultPolicyList().add(policy);
        properties.getPolicyList().put("key", Lists.newArrayList(policy));
        properties.getPolicies().put("key", policy);
        Set<ConstraintViolation<RateLimitProperties>> violations = validator.validate(properties);
        assertThat(violations).isEmpty();
    }

    @Test
    public void testValidOnPolicyWithQuotaNoLimit() {
        RateLimitProperties properties = new RateLimitProperties();
        properties.setKeyPrefix("prefix");
        Policy policy = getPolicy(null, 1L);
        properties.setDefaultPolicy(policy);
        properties.getDefaultPolicyList().add(policy);
        properties.getPolicyList().put("key", Lists.newArrayList(policy));
        properties.getPolicies().put("key", policy);
        Set<ConstraintViolation<RateLimitProperties>> violations = validator.validate(properties);
        assertThat(violations).isEmpty();
    }
}