package com.a6raywa1cher.coursejournalbackend.rest.dto;

import com.a6raywa1cher.coursejournalbackend.model.UserRole;
import lombok.Data;

import javax.validation.constraints.Size;

@Data
public class EditUserDto {
    @Size(min = 5, max = 25)
    private String username;

    private String password;

    private UserRole userRole;

    @Size(max = 30)
    private String firstName;

    @Size(max = 30)
    private String middleName;

    @Size(max = 30)
    private String lastName;
}
