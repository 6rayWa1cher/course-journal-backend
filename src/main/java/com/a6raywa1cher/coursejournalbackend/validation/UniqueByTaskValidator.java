package com.a6raywa1cher.coursejournalbackend.validation;

import com.a6raywa1cher.coursejournalbackend.rest.dto.BatchSetForStudentAndCourseSubmissionRestDto;
import org.springframework.stereotype.Component;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class UniqueByTaskValidator implements ConstraintValidator<UniqueByTask,
        List<BatchSetForStudentAndCourseSubmissionRestDto.SubmissionSetRestDto>> {
    @Override
    public boolean isValid(
            List<BatchSetForStudentAndCourseSubmissionRestDto.SubmissionSetRestDto> value,
            ConstraintValidatorContext context
    ) {
        if (value == null) return true;
        Set<?> set = value.stream()
                .map(BatchSetForStudentAndCourseSubmissionRestDto.SubmissionSetRestDto::getTask)
                .collect(Collectors.toSet());
        return value.size() == set.size();
    }
}
