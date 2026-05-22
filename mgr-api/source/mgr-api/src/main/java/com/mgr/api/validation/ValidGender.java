package com.mgr.api.validation;

import com.mgr.api.validation.impl.GenderValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = GenderValidator.class)
public @interface ValidGender {

    int [] anyOf() default {1, 2, 3};

    boolean allowNull() default false;

    String message() default "Gender is not valid! Only(1: male, 2:female, 3:other)";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
