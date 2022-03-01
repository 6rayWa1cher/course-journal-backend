package com.a6raywa1cher.coursejournalbackend.rest.dto;

import com.a6raywa1cher.coursejournalbackend.rest.dto.groups.OnCreate;
import com.a6raywa1cher.coursejournalbackend.rest.dto.groups.OnUpdate;
import lombok.Data;

import javax.validation.constraints.*;
import java.time.ZonedDateTime;

@Data
public class TaskRestDto {
    @NotNull(groups = {OnCreate.class, OnUpdate.class})
    @Positive
    private Long course;

    @NotNull(groups = {OnCreate.class, OnUpdate.class})
    private Integer taskNumber;

    @NotBlank(groups = {OnCreate.class, OnUpdate.class})
    @Size(min = GENERAL_NAME_MIN_LENGTH, max = GENERAL_NAME_MAX_LENGTH)
    private String title;

    @Size(min = GENERAL_DESCRIPTION_MIN_LENGTH, max = GENERAL_DESCRIPTION_MAX_LENGTH)
    private String description;

    @PositiveOrZero
    private Integer maxScore;


    @Min(0)
    @Max(100)
    private Integer maxPenaltyPercent;

    private Boolean announced;

    private ZonedDateTime announcementAt;

    private ZonedDateTime softDeadlineAt;

    private ZonedDateTime hardDeadlineAt;
}
