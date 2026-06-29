package com.mgr.api.validation.impl;

import com.mgr.api.validation.ValidFileUploadKind;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Arrays;

public class FileUploadValidator implements ConstraintValidator<ValidFileUploadKind, Integer> {
    private int[] validFileUploadType;
    private boolean allowNull;

    @Override
    public void initialize(ValidFileUploadKind constraintAnnotation) {
        this.validFileUploadType = constraintAnnotation.anyOf();
        this.allowNull = constraintAnnotation.allowNull();
    }

    @Override
    public boolean isValid(Integer value, ConstraintValidatorContext context) {
        if (value == null) {
            if (this.allowNull) {
                return true;
            } else {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("File upload type cannot null")
                        .addConstraintViolation();
                return false;
            }
        }
        return Arrays.stream((validFileUploadType)).anyMatch(s -> s == value);
    }
}
