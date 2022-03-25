package com.a6raywa1cher.coursejournalbackend.dto;

import lombok.Builder;
import lombok.Data;

import java.time.ZonedDateTime;

@Data
@Builder
public class SubmissionDto {
    private Long task;

    private Long student;

    private ZonedDateTime submittedAt;

    private Integer mainScore;

    private Integer additionalScore;

    private ZonedDateTime createdAt;

    private ZonedDateTime lastModifiedAt;
}
