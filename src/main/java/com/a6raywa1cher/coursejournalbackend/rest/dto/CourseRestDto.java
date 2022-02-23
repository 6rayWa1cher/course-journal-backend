package com.a6raywa1cher.coursejournalbackend.rest.dto;

import com.a6raywa1cher.coursejournalbackend.rest.dto.groups.OnCreate;
import com.a6raywa1cher.coursejournalbackend.rest.dto.groups.OnUpdate;
import com.a6raywa1cher.coursejournalbackend.validation.RegexLibrary;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Data
public class CourseRestDto {
    @NotBlank(groups = {OnCreate.class, OnUpdate.class})
    @Pattern(regexp = RegexLibrary.GENERAL_NAME)
    private String name;
}
