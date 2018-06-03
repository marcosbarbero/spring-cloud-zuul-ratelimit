package com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.properties;

import org.junit.Test;
import org.springframework.validation.Errors;
import org.springframework.validation.MapBindingResult;
import org.springframework.validation.ObjectError;

import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;

public class RateLimitPropertiesTest {

    private static final String OBJECT_NAME = "properties";
    private static final ObjectError FILTER_ORDER_ERROR = new ObjectError(OBJECT_NAME,
            new String[]{"filters.order.properties", "filters.order"},
            new String[]{"postFilterOrder", "preFilterOrder"},
            "Value of postFilterOrder must be greater than preFilterOrder.");
    private Errors errors = new MapBindingResult(new HashMap<>(), OBJECT_NAME);

    @Test
    public void shouldNotThrowExceptionIfPostFilterHasGreaterOrderThanPreFilter() {
        RateLimitProperties properties = new RateLimitProperties();
        properties.setPreFilterOrder(10);
        properties.setPostFilterOrder(20);

        properties.validate(properties, errors);

        assertThat(errors.getAllErrors()).isEmpty();
    }

    @Test
    public void shouldThrowExceptionIfPostFilterHasLessOrderThanPreFilter() {
        RateLimitProperties properties = new RateLimitProperties();
        properties.setPreFilterOrder(20);
        properties.setPostFilterOrder(10);

        properties.validate(properties, errors);

        assertThat(errors.getAllErrors()).containsOnly(FILTER_ORDER_ERROR);
    }

    @Test
    public void shouldThrowExceptionIfPostFilterHasEqualOrderAsPreFilter() {
        RateLimitProperties properties = new RateLimitProperties();
        properties.setPreFilterOrder(20);
        properties.setPostFilterOrder(20);

        properties.validate(properties, errors);

        assertThat(errors.getAllErrors()).containsOnly(FILTER_ORDER_ERROR);
    }
}