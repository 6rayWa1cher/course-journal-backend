package com.a6raywa1cher.coursejournalbackend.dto;

import com.a6raywa1cher.coursejournalbackend.model.UserRole;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreateEditAuthUserDto {
    private String username;

    private String password;

    private UserRole userRole;

    private Long userInfo;
}
