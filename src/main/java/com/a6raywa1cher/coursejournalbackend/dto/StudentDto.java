package com.a6raywa1cher.coursejournalbackend.dto;

import lombok.Builder;
import lombok.Data;

import java.time.ZonedDateTime;

@Data
@Builder
public class StudentDto {
    private Long id;

    private Long course;

    private Long group;

    private String firstName;

    private String lastName;

    private String middleName;

    private ZonedDateTime createdAt;

    private ZonedDateTime lastModifiedAt;
}
