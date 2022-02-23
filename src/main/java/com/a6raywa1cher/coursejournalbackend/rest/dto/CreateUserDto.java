package com.a6raywa1cher.coursejournalbackend.rest.dto;

import com.a6raywa1cher.coursejournalbackend.model.UserRole;
import com.a6raywa1cher.coursejournalbackend.validation.RegexLibrary;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Data
public class CreateUserDto {
    @NotBlank
    @Pattern(regexp = RegexLibrary.USERNAME)
    private String username;

    @NotBlank
    private String password;

    private UserRole userRole = UserRole.TEACHER;

    @Size(max = 30)
    private String firstName;

    @Size(max = 30)
    private String middleName;

    @Size(max = 30)
    private String lastName;
}
