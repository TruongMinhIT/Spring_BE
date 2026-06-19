package com.mgr.api.validation.impl;

import com.mgr.api.validation.ValidNationType;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Arrays;

public class NationTypeValidator implements ConstraintValidator<ValidNationType, Integer> {
    private int[] validNationType;
    private boolean allowNull;

    @Override
    public void initialize(ValidNationType constraintAnnotation) {
        this.validNationType = constraintAnnotation.anyOf();
        this.allowNull = constraintAnnotation.allowNull();
    }

    @Override
    public boolean isValid(Integer value, ConstraintValidatorContext context) {
        if (value == null) {
            if (this.allowNull) {
                return true;
            } else {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("Nation type can not null")
                        .addConstraintViolation();
                return false;
            }
        }
        return Arrays.stream((validNationType)).anyMatch(s -> s == value);
    }
}
