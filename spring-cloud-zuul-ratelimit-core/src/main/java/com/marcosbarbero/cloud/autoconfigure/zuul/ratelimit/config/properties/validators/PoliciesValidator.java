/*
 * Copyright 2012-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.properties.validators;

import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.properties.RateLimitProperties.Policy;
import java.util.Collection;
import java.util.Map;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Validates the rate limit policies.
 *
 * @author Liel Chayoun
 */
public class PoliciesValidator implements ConstraintValidator<Policies, Object> {

    @Override
    public void initialize(Policies constraintAnnotation) {
        //Nothing to do here
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
