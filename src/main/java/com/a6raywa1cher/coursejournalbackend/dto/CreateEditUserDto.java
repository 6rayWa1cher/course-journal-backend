package com.a6raywa1cher.coursejournalbackend.dto;

import com.a6raywa1cher.coursejournalbackend.model.UserRole;
import lombok.Data;

@Data
public class CreateEditUserDto {
    private String username;

    private String password;

    private UserRole userRole;

    private String firstName;

    private String middleName;

    private String lastName;
}
