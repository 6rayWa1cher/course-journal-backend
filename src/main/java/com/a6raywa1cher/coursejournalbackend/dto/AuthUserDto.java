package com.a6raywa1cher.coursejournalbackend.dto;

import com.a6raywa1cher.coursejournalbackend.model.UserRole;
import lombok.Builder;
import lombok.Data;

import java.time.ZonedDateTime;

@Data
@Builder
public class AuthUserDto {
    private Long id;

    private String username;

    private Long student;

    private Long employee;

    private UserRole userRole;

    private ZonedDateTime createdAt;

    private ZonedDateTime lastModifiedAt;

    private ZonedDateTime lastVisitAt;
}
