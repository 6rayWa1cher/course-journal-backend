package com.a6raywa1cher.coursejournalbackend.rest.dto;

import com.a6raywa1cher.coursejournalbackend.rest.dto.groups.OnCreate;
import com.a6raywa1cher.coursejournalbackend.rest.dto.groups.OnUpdate;
import com.a6raywa1cher.coursejournalbackend.validation.RegexLibrary;
import com.a6raywa1cher.coursejournalbackend.validation.UniqueElements;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Positive;
import java.util.List;

@Data
public class CourseRestDto {
    @NotBlank(groups = {OnCreate.class, OnUpdate.class})
    @Pattern(regexp = RegexLibrary.COMMON_NAME)
    private String name;

    @NotNull(groups = {OnCreate.class, OnUpdate.class})
    @Positive
    private Long owner;

    @NotNull(groups = {OnCreate.class, OnUpdate.class})
    @UniqueElements
    private List<Long> students;
}
