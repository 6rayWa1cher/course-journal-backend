package com.a6raywa1cher.coursejournalbackend.validation;

import com.a6raywa1cher.coursejournalbackend.rest.dto.BatchSetForTaskCriteriaDto;
import org.springframework.stereotype.Component;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class UniqueByNameValidator implements ConstraintValidator<UniqueByName,
        List<BatchSetForTaskCriteriaDto.CriteriaSetForTaskDto>> {
    @Override
    public boolean isValid(List<BatchSetForTaskCriteriaDto.CriteriaSetForTaskDto> value, ConstraintValidatorContext context) {
        if (value == null) return true;
        Set<?> set = value.stream()
                .map(BatchSetForTaskCriteriaDto.CriteriaSetForTaskDto::getName)
                .collect(Collectors.toSet());
        return value.size() == set.size();
    }
}
