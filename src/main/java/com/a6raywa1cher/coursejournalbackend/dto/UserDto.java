package com.a6raywa1cher.coursejournalbackend.dto;

import com.a6raywa1cher.coursejournalbackend.model.UserRole;
import lombok.Data;

@Data
public class UserDto {
    private Long id;

    private String username;

    private String password;

    private UserRole userRole;

    private FullNameDto fullName;
}
