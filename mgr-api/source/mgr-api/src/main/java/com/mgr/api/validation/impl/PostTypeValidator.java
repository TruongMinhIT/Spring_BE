package com.mgr.api.validation.impl;

import com.mgr.api.validation.ValidPostType;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Arrays;

public class PostTypeValidator implements ConstraintValidator<ValidPostType, Integer> {

    private int[] validPostType;
    private boolean allowNull;
    @Override
    public void initialize(ValidPostType constraintAnnotation) {
        this.validPostType = constraintAnnotation.anyOf();
        this.allowNull = constraintAnnotation.allowNull();
    }

    @Override
    public boolean isValid(Integer value, ConstraintValidatorContext context) {
        if (value == null) {
            if (this.allowNull) {
                return true;
            }
            else {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("Post type is not null")
                        .addConstraintViolation();
                return false;
            }
        }
        return Arrays.stream((validPostType)).anyMatch(s -> s == value);
    }
}
