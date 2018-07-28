package com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.properties.validators;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PoliciesValidator.class)
public @interface Policies {

    String message() default "Policy must contain limit, quota or both";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
