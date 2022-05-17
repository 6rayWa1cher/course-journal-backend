package com.a6raywa1cher.coursejournalbackend.rest.dto;

import lombok.Data;

import java.time.ZonedDateTime;

@Data
public class ShortTaskRestDto {
    private Long id;

    private Long course;

    private Integer taskNumber;

    private String title;

    private Integer maxScore;

    private Integer maxPenaltyPercent;

    private Boolean announced;

    private ZonedDateTime announcementAt;

    private Boolean deadlinesEnabled;

    private ZonedDateTime softDeadlineAt;

    private ZonedDateTime hardDeadlineAt;

    private ZonedDateTime createdAt;

    private ZonedDateTime lastModifiedAt;
}
