package com.a6raywa1cher.coursejournalbackend.rest.dto;

import com.a6raywa1cher.coursejournalbackend.rest.dto.groups.OnCreate;
import com.a6raywa1cher.coursejournalbackend.rest.dto.groups.OnUpdate;
import com.a6raywa1cher.coursejournalbackend.validation.UniqueElements;
import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.PastOrPresent;
import javax.validation.constraints.Positive;
import java.time.ZonedDateTime;
import java.util.List;

@Data
public class SubmissionRestDto {
    @NotNull(groups = {OnCreate.class, OnUpdate.class})
    @Positive
    private Long task;

    @NotNull(groups = {OnCreate.class, OnUpdate.class})
    @Positive
    private Long student;

    @NotNull(groups = {OnCreate.class, OnUpdate.class})
    @PastOrPresent
    private ZonedDateTime submittedAt;

    @NotNull(groups = {OnCreate.class, OnUpdate.class})
    @UniqueElements
    private List<Long> satisfiedCriteria;

    @NotNull(groups = {OnCreate.class, OnUpdate.class})
    private Integer additionalScore;
}
