package com.mgr.api.validation;

import com.mgr.api.constant.MgrConstant;
import com.mgr.api.validation.impl.DifficultyLevelValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = DifficultyLevelValidator.class)
public @interface ValidDifficultyLevel {
    int[] anyOf() default {MgrConstant.DIFFICULTY_LEVEL_BEGINNER, MgrConstant.DIFFICULTY_LEVEL_INTERMEDIATE, MgrConstant.DIFFICULTY_LEVEL_ADVANCED};

    boolean allowNull() default false;

    String message() default "Difficulty level is not valid! Only(1: beginner, 2: intermediate, 3: advanced)";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
