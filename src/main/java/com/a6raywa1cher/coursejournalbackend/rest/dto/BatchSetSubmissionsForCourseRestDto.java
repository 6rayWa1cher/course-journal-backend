package com.a6raywa1cher.coursejournalbackend.rest.dto;

import com.a6raywa1cher.coursejournalbackend.validation.UniqueBy;
import com.a6raywa1cher.coursejournalbackend.validation.UniqueElements;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.time.ZonedDateTime;
import java.util.List;

@Data
public class BatchSetSubmissionsForCourseRestDto {
    @NotNull
    @UniqueBy(fields = {"task", "student"}, clazz = SubmissionSetForCourseRestDto.class)
    @Valid
    private List<SubmissionSetForCourseRestDto> submissions;

    @Data
    public static final class SubmissionSetForCourseRestDto {
        @NotNull
        @Positive
        private Long task;

        @NotNull
        @Positive
        private Long student;

        @NotNull
        @UniqueElements
        private List<Long> satisfiedCriteria;

        @NotNull
        private ZonedDateTime submittedAt;

        @NotNull
        private Double additionalScore;
    }
}
