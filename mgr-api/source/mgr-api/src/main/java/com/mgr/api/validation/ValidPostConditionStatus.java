package com.mgr.api.validation;

import com.mgr.api.constant.MgrConstant;
import com.mgr.api.validation.impl.PostConditionStatusValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PostConditionStatusValidator.class)
public @interface ValidPostConditionStatus {
    int[] anyOf() default {MgrConstant.POST_CONDITION_STATUS_BRAND_NEW, MgrConstant.POST_CONDITION_STATUS_USED};

    boolean allowNull() default false;

    String message() default "Post condition status is not valid! Only(1: Brand new, 2: Used)";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
