package com.marindulja.mentalhealthbackend.validations.annotations;

import com.marindulja.mentalhealthbackend.validations.validators.BetweenValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = BetweenValidator.class)
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Between {
    String message() default "must be between {min} and {max}";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};

    int min();
    int max();
}
