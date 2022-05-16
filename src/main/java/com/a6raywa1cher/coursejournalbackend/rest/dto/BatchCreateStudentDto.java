package com.a6raywa1cher.coursejournalbackend.rest.dto;

import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.*;
import java.util.List;

import static com.a6raywa1cher.coursejournalbackend.validation.RegexLibrary.COMMON_NAME;

@Data
public class BatchCreateStudentDto {
    @NotNull
    @Positive
    private Long group;

    @NotNull
    @Size(min = 1)
    @Valid
    private List<StudentInfo> students;

    @Data
    public static final class StudentInfo {
        @NotBlank
        @Pattern(regexp = COMMON_NAME)
        private String firstName;

        @Pattern(regexp = COMMON_NAME)
        private String middleName;

        @NotBlank
        @Pattern(regexp = COMMON_NAME)
        private String lastName;
    }
}
