package com.a6raywa1cher.coursejournalbackend.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;

@Constraint(validatedBy = UniqueIdsReorderRequestValidator.class)
@Retention(RetentionPolicy.RUNTIME)
@Target(FIELD)
public @interface UniqueIds {
    String message() default "Reorder map cannot contain same task id twice";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
