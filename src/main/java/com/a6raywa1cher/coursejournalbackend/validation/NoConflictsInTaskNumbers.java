package com.a6raywa1cher.coursejournalbackend.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;

@Constraint(validatedBy = NoConflictsInTaskNumbersValidator.class)
@Retention(RetentionPolicy.RUNTIME)
@Target(FIELD)
public @interface NoConflictsInTaskNumbers {
    String message() default "The input list cannot contain more than 4 movies.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
