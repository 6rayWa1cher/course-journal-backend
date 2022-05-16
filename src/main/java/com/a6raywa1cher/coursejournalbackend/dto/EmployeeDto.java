package com.a6raywa1cher.coursejournalbackend.dto;

import lombok.Builder;
import lombok.Data;

import java.time.ZonedDateTime;

@Data
@Builder
public class EmployeeDto {
    private Long id;

    private String firstName;

    private String middleName;

    private String lastName;

    private String department;

    private Boolean hasAuthUser;

    private ZonedDateTime createdAt;

    private ZonedDateTime lastModifiedAt;
}
