package com.a6raywa1cher.coursejournalbackend.dto;

import com.a6raywa1cher.coursejournalbackend.model.AttendanceType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.ZonedDateTime;

@Data
@Builder
public class AttendanceDto {
    private Long id;

    private Long student;

    private Long course;

    private Integer attendedClass;

    private LocalDate attendedDate;

    private ZonedDateTime createdAt;

    private ZonedDateTime lastModifiedAt;

    private AttendanceType attendanceType;
}
