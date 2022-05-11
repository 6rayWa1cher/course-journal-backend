package com.a6raywa1cher.coursejournalbackend.rest.dto;

import com.a6raywa1cher.coursejournalbackend.rest.dto.groups.OnCreate;
import com.a6raywa1cher.coursejournalbackend.rest.dto.groups.OnUpdate;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Positive;

import static com.a6raywa1cher.coursejournalbackend.validation.RegexLibrary.COMMON_NAME;

@Data
public class StudentRestDto {
    @NotNull(groups = {OnCreate.class, OnUpdate.class})
    @Positive
    private Long group;

    @NotBlank(groups = {OnCreate.class, OnUpdate.class})
    @Pattern(regexp = COMMON_NAME)
    private String firstName;

    @NotBlank(groups = {OnCreate.class, OnUpdate.class})
    @Pattern(regexp = COMMON_NAME)
    private String lastName;

    @Pattern(regexp = COMMON_NAME)
    private String middleName;
}
