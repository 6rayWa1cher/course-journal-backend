package com.a6raywa1cher.coursejournalbackend.dto;

import com.a6raywa1cher.coursejournalbackend.model.AttendanceType;
import lombok.Builder;
import lombok.Data;

import java.time.ZonedDateTime;

@Data
@Builder
public class AttendanceDto {
    private Long id;

    private Long student;

    private Long course;

    private ZonedDateTime attendedAt;

    private ZonedDateTime createdAt;

    private ZonedDateTime lastModifiedAt;

    private AttendanceType attendanceType;
}
