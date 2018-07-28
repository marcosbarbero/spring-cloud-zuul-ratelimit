package com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.properties.validators;

import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.properties.RateLimitProperties.Policy;
import java.util.Collection;
import java.util.Map;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class PoliciesValidator implements ConstraintValidator<Policies, Object> {

    @Override
    public void initialize(Policies constraintAnnotation) {
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        } else if (value instanceof Policy) {
            return isValidObject(value);
        } else if (value instanceof Collection) {
            return isValidCollection((Collection<?>) value);
        } else if (value instanceof Map) {
            return ((Map<?, ?>) value).values().stream().allMatch(v -> isValid(v, context));
        } else {
            return false;
        }
    }

    private boolean isValidCollection(Collection<?> objects) {
        return objects.isEmpty()
            || objects.stream().allMatch(this::isValidObject);
    }

    private boolean isValidObject(Object o) {
        return (o instanceof Policy) && isValidPolicy((Policy) o);
    }

    private boolean isValidPolicy(Policy p) {
        return p.getLimit() != null || p.getQuota() != null;
    }
}
