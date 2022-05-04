package com.a6raywa1cher.coursejournalbackend.rest.dto;

import com.a6raywa1cher.coursejournalbackend.model.UserRole;
import com.a6raywa1cher.coursejournalbackend.rest.dto.groups.OnCreate;
import com.a6raywa1cher.coursejournalbackend.rest.dto.groups.OnUpdate;
import com.a6raywa1cher.coursejournalbackend.validation.RegexLibrary;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Data
public class AuthUserRestDto {
    @NotBlank(groups = {OnCreate.class, OnUpdate.class})
    @Pattern(regexp = RegexLibrary.USERNAME)
    private String username;

    @NotBlank(groups = {OnCreate.class, OnUpdate.class})
    private String password;

    @NotNull(groups = {OnCreate.class, OnUpdate.class})
    private UserRole userRole;

    private Long userInfo;
}
