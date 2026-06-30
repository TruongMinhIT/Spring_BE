package com.mgr.api.validation;

import com.mgr.api.constant.MgrConstant;
import com.mgr.api.validation.impl.FileUploadValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = FileUploadValidator.class)
public @interface ValidFileUploadKind {
    int[] anyOf() default {MgrConstant.UPLOAD_FILE_AVATAR, MgrConstant.UPLOAD_FILE_LOGO, MgrConstant.UPLOAD_FILE_IMAGE_THUMBNAIL};

    boolean allowNull() default false;

    String message() default "File upload is not valid! Only (1: Avatar, 2: Logo)";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
