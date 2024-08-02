package com.marindulja.mentalhealthbackend.validations.validators;

import com.marindulja.mentalhealthbackend.validations.annotations.Between;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class BetweenValidator implements ConstraintValidator<Between, Integer> {
    private int min;
    private int max;

    @Override
    public void initialize(Between constraintAnnotation) {
        this.min = constraintAnnotation.min();
        this.max = constraintAnnotation.max();
    }

    @Override
    public boolean isValid(Integer value, ConstraintValidatorContext context) {
        if (value == null) {
            return true; // null values are valid, use @NotNull for null check
        }
        return value >= min && value <= max;
    }
}
