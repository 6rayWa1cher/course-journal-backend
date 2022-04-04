package com.a6raywa1cher.coursejournalbackend.validation;

import org.springframework.stereotype.Component;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class UniqueElementsValidator implements ConstraintValidator<UniqueElements,
        List<?>> {
    @Override
    public boolean isValid(List<?> value, ConstraintValidatorContext context) {
        if (value == null) return true;
        Set<?> set = new HashSet<>(value);
        return value.size() == set.size();
    }
}
