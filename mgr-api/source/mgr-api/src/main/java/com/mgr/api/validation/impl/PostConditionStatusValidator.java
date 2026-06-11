package com.mgr.api.validation.impl;

import com.mgr.api.validation.ValidPostConditionStatus;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Arrays;

public class PostConditionStatusValidator implements ConstraintValidator<ValidPostConditionStatus, Integer> {

    private int[] validPostConditionStatus;
    private boolean allowNull;
    @Override
    public void initialize(ValidPostConditionStatus constraintAnnotation) {
        this.validPostConditionStatus = constraintAnnotation.anyOf();
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
                context.buildConstraintViolationWithTemplate("Post condition status is not null")
                        .addConstraintViolation();
                return false;
            }
        }
        return Arrays.stream((validPostConditionStatus)).anyMatch(s -> s == value);
    }
}
