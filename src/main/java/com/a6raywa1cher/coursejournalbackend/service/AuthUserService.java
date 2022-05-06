package com.a6raywa1cher.coursejournalbackend.service;

import com.a6raywa1cher.coursejournalbackend.dto.AuthUserDto;
import com.a6raywa1cher.coursejournalbackend.dto.CreateEditAuthUserDto;
import com.a6raywa1cher.coursejournalbackend.model.AuthUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface AuthUserService {
    AuthUserDto getById(long id);

    Page<AuthUserDto> getPage(Pageable pageable);

    Optional<AuthUser> findRawById(long id);

    AuthUserDto getByEmployeeId(long id);

    AuthUserDto getByStudentId(long id);

    AuthUserDto create(CreateEditAuthUserDto dto);

    AuthUserDto update(long id, CreateEditAuthUserDto dto);

    AuthUserDto patch(long id, CreateEditAuthUserDto dto);

    void delete(long id);
}
