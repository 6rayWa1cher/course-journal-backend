package com.a6raywa1cher.coursejournalbackend.rest.dto;

import com.a6raywa1cher.coursejournalbackend.rest.dto.groups.OnCreate;
import com.a6raywa1cher.coursejournalbackend.rest.dto.groups.OnUpdate;
import lombok.Data;

import javax.validation.constraints.*;
import java.time.ZonedDateTime;

import static com.a6raywa1cher.coursejournalbackend.validation.RegexLibrary.COMMON_NAME;

@Data
public class TaskRestDto {
    @NotNull(groups = {OnCreate.class, OnUpdate.class})
    @Positive
    private Long course;

    @NotNull(groups = {OnUpdate.class})
    private Integer taskNumber;

    @NotBlank(groups = {OnCreate.class, OnUpdate.class})
    @Pattern(regexp = COMMON_NAME)
    private String title;

    @Size(max = 25000)
    private String description;

    @PositiveOrZero
    private Integer maxScore;

    @Min(0)
    @Max(100)
    private Integer maxPenaltyPercent;

    private Boolean announced;

    private ZonedDateTime announcementAt;

    private Boolean deadlinesEnabled;

    private ZonedDateTime softDeadlineAt;

    private ZonedDateTime hardDeadlineAt;
}
