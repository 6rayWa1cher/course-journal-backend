package com.a6raywa1cher.coursejournalbackend.dto;

import com.a6raywa1cher.coursejournalbackend.model.UserRole;
import lombok.Data;

import java.time.ZonedDateTime;

@Data
public class UserDto {
    private Long id;

    private String username;

    private UserRole userRole;

    private String firstName;

    private String middleName;

    private String lastName;

    private ZonedDateTime createdAt;

    private ZonedDateTime lastModifiedAt;

    private ZonedDateTime lastVisitAt;
}
