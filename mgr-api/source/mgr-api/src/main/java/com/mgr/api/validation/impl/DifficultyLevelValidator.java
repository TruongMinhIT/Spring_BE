package com.mgr.api.validation.impl;

import com.mgr.api.validation.ValidDifficultyLevel;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Arrays;

public class DifficultyLevelValidator implements ConstraintValidator<ValidDifficultyLevel, Integer> {

    private int[] validDifficultyLevel;
    private boolean allowNull;

    @Override
    public void initialize(ValidDifficultyLevel constraintAnnotation) {
        this.validDifficultyLevel = constraintAnnotation.anyOf();
        this.allowNull = constraintAnnotation.allowNull();
    }

    @Override
    public boolean isValid(Integer value, ConstraintValidatorContext context) {
        if (value == null) {
            if (this.allowNull) {
                return true;
            } else {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("Difficulty level is not null")
                        .addConstraintViolation();
                return false;
            }
        }
        return Arrays.stream(validDifficultyLevel).anyMatch(s -> s == value);
    }
}
