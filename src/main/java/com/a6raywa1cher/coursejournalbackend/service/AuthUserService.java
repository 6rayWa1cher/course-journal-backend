package com.a6raywa1cher.coursejournalbackend.service;

import com.a6raywa1cher.coursejournalbackend.dto.AuthUserDto;
import com.a6raywa1cher.coursejournalbackend.dto.CreateEditAuthUserDto;

import java.util.Optional;

public interface AuthUserService {
    Optional<AuthUserDto> getById(long id);

    Optional<AuthUserDto> getByEmployeeId(long id);

    Optional<AuthUserDto> getByStudentId(long id);

    AuthUserDto create(CreateEditAuthUserDto dto);

    AuthUserDto put(CreateEditAuthUserDto dto);

    AuthUserDto patch(CreateEditAuthUserDto dto);

    void delete(AuthUserDto dto);
}
