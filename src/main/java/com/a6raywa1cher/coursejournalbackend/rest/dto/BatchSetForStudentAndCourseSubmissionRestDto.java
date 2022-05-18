package com.a6raywa1cher.coursejournalbackend.rest.dto;

import com.a6raywa1cher.coursejournalbackend.validation.UniqueByTask;
import com.a6raywa1cher.coursejournalbackend.validation.UniqueElements;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.time.ZonedDateTime;
import java.util.List;

@Data
public class BatchSetForStudentAndCourseSubmissionRestDto {
    @NotNull
    @UniqueByTask
    @Valid
    private List<SubmissionSetRestDto> submissions;

    @Data
    public static final class SubmissionSetRestDto {
        @NotNull
        @Positive
        private Long task;

        @NotNull
        @UniqueElements
        private List<Long> satisfiedCriteria;

        @NotNull
        private ZonedDateTime submittedAt;

        @NotNull
        private Integer additionalScore;
    }
}
