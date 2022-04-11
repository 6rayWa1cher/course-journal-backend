package com.a6raywa1cher.coursejournalbackend.rest.dto;

import com.a6raywa1cher.coursejournalbackend.model.AttendanceType;
import com.a6raywa1cher.coursejournalbackend.rest.dto.groups.OnCreate;
import com.a6raywa1cher.coursejournalbackend.rest.dto.groups.OnUpdate;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.*;
import java.time.LocalDate;
import java.util.List;

@Data
public class BatchCreateAttendancesDto {
    @NotNull
    @Positive
    private Long course;

    @NotNull
    @Size(min = 1)
    @Valid
    private List<AttendanceInfo> attendances;

    @Data
    public static final class AttendanceInfo {
        @NotNull
        @Positive
        private Long student;

        @NotNull
        @PastOrPresent
        private LocalDate attendedDate;

        @NotNull
        @Min(value = 1, message = "Class number cannot be less than 1")
        @Max(value = 6, message = "Class number cannot be more than 6")
        private Integer attendedClass;

        @NotNull
        private AttendanceType attendanceType;
    }
}
