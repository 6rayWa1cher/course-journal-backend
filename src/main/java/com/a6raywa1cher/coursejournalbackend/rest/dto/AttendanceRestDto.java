package com.a6raywa1cher.coursejournalbackend.rest.dto;

import com.a6raywa1cher.coursejournalbackend.model.AttendanceType;
import com.a6raywa1cher.coursejournalbackend.rest.dto.groups.OnCreate;
import com.a6raywa1cher.coursejournalbackend.rest.dto.groups.OnUpdate;
import lombok.Data;

import javax.validation.constraints.*;
import java.time.LocalDate;

@Data
public class AttendanceRestDto {
    @NotNull(groups = {OnCreate.class, OnUpdate.class})
    @Positive
    private Long student;

    @NotNull(groups = {OnCreate.class, OnUpdate.class})
    @Positive
    private Long course;

    @NotNull(groups = {OnCreate.class, OnUpdate.class})
    @PastOrPresent
    private LocalDate attendedDate;

    @NotNull(groups = {OnCreate.class, OnUpdate.class})
    @Min(value = 1, message = "Class number cannot be less than 1")
    @Max(value = 16, message = "Class number cannot be more than 16")
    private Integer attendedClass;

    @NotNull(groups = {OnCreate.class, OnUpdate.class})
    private AttendanceType attendanceType;
}
