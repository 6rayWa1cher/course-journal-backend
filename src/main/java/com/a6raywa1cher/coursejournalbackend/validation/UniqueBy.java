package com.a6raywa1cher.coursejournalbackend.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;

@Constraint(validatedBy = UniqueByValidator.class)
@Retention(RetentionPolicy.RUNTIME)
@Target(FIELD)
@Repeatable(RepeatableUniqueBy.class)
public @interface UniqueBy {
    String[] fields();

    Class<?> clazz();

    String message() default "List contains intersected values";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
