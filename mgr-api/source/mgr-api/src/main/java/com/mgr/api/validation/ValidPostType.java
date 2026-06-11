package com.mgr.api.validation;

import com.mgr.api.constant.MgrConstant;
import com.mgr.api.validation.impl.PostTypeValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PostTypeValidator.class)
public @interface ValidPostType {
    int [] anyOf() default {MgrConstant.POST_TYPE_SALE, MgrConstant.POST_TYPE_BUY};

    boolean allowNull() default false;

    String message() default "Post type is not valid! Only(1: sale, 2:buy)";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
