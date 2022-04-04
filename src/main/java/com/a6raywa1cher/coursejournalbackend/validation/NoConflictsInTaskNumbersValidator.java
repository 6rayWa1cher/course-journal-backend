package com.a6raywa1cher.coursejournalbackend.validation;

import com.a6raywa1cher.coursejournalbackend.rest.dto.ReorderTasksRestDto;
import org.springframework.stereotype.Component;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.HashSet;
import java.util.List;

@Component
public class NoConflictsInTaskNumbersValidator implements ConstraintValidator<NoConflictsInTaskNumbers,
        List<ReorderTasksRestDto.ReorderRequest>> {
    @Override
    public boolean isValid(List<ReorderTasksRestDto.ReorderRequest> value, ConstraintValidatorContext context) {
        if (value == null) return true;
        List<Integer> values = value.stream().map(ReorderTasksRestDto.ReorderRequest::getNumber).toList();
        return values.size() == new HashSet<>(values).size();
    }
}
