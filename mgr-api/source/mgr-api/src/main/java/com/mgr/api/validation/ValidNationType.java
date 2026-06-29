package com.mgr.api.validation;

import com.mgr.api.constant.MgrConstant;
import com.mgr.api.validation.impl.NationTypeValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = NationTypeValidator.class)
public @interface ValidNationType {
    int [] anyOf() default {MgrConstant.NATION_TYPE_PROVINCE, MgrConstant.NATION_TYPE_DISTRICT, MgrConstant.NATION_TYPE_COMMUNE};

    boolean allowNull() default false;

    String message() default "Nation type is not valid! Only(1: Province, 2:District, 3:Commune)";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
