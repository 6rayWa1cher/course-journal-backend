package com.a6raywa1cher.coursejournalbackend.validation;

import com.a6raywa1cher.coursejournalbackend.rest.dto.BatchSetSubmissionsForStudentAndCourseRestDto;
import org.springframework.stereotype.Component;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class UniqueByTaskValidator implements ConstraintValidator<UniqueByTask,
        List<BatchSetSubmissionsForStudentAndCourseRestDto.SubmissionSetForStudentAndCourseRestDto>> {
    @Override
    public boolean isValid(
            List<BatchSetSubmissionsForStudentAndCourseRestDto.SubmissionSetForStudentAndCourseRestDto> value,
            ConstraintValidatorContext context
    ) {
        if (value == null) return true;
        Set<?> set = value.stream()
                .map(BatchSetSubmissionsForStudentAndCourseRestDto.SubmissionSetForStudentAndCourseRestDto::getTask)
                .collect(Collectors.toSet());
        return value.size() == set.size();
    }
}
