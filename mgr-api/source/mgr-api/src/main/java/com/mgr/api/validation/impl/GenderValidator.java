package com.mgr.api.validation.impl;

import com.mgr.api.validation.ValidGender;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Arrays;

public class GenderValidator implements ConstraintValidator<ValidGender, Integer> {
    private int[] validGenders;
    private boolean allowNull;

    @Override
    public void initialize(ValidGender constrainAnnotation){
        this.validGenders = constrainAnnotation.anyOf();
        this.allowNull = constrainAnnotation.allowNull();
    }

    @Override
    public boolean isValid(Integer value, ConstraintValidatorContext context){
        if (value ==null){
            if (this.allowNull){
                return true;
            }
            else
            {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("Gender is not null")
                        .addConstraintViolation();
                return false;
            }
        }
        return Arrays.stream((validGenders)).anyMatch(g -> g == value);
    }
}
