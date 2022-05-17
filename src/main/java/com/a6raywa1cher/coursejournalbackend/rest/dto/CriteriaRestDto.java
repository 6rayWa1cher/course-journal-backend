package com.a6raywa1cher.coursejournalbackend.rest.dto;

import com.a6raywa1cher.coursejournalbackend.rest.dto.groups.OnCreate;
import com.a6raywa1cher.coursejournalbackend.rest.dto.groups.OnUpdate;
import lombok.Data;

import javax.validation.constraints.*;

import static com.a6raywa1cher.coursejournalbackend.validation.RegexLibrary.CRITERIA_NAME;

@Data
public class CriteriaRestDto {
    @NotNull(groups = {OnCreate.class, OnUpdate.class})
    @Positive
    private Long task;

    @NotBlank(groups = {OnCreate.class, OnUpdate.class})
    @Pattern(regexp = CRITERIA_NAME)
    private String name;

    @NotNull(groups = {OnCreate.class, OnUpdate.class})
    @Min(0)
    @Max(100)
    private Integer criteriaPercent;
}
