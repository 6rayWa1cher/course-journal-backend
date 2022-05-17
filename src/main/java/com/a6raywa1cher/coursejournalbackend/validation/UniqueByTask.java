package com.a6raywa1cher.coursejournalbackend.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;

@Constraint(validatedBy = UniqueByTaskValidator.class)
@Retention(RetentionPolicy.RUNTIME)
@Target(FIELD)
public @interface UniqueByTask {
    String message() default "List cannot contain same task twice";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
