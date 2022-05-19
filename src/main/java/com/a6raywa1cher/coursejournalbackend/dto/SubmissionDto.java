package com.a6raywa1cher.coursejournalbackend.dto;

import lombok.Builder;
import lombok.Data;

import java.time.ZonedDateTime;
import java.util.List;

@Data
@Builder
public class SubmissionDto {
    private Long id;

    private Long task;

    private Long student;

    private ZonedDateTime submittedAt;

    private List<Long> satisfiedCriteria;

    private Double mainScore;

    private Double additionalScore;

    private ZonedDateTime createdAt;

    private ZonedDateTime lastModifiedAt;
}
