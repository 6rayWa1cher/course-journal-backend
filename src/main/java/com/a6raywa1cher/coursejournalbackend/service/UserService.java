package com.a6raywa1cher.coursejournalbackend.service;


import com.a6raywa1cher.coursejournalbackend.dto.CreateEditUserDto;
import com.a6raywa1cher.coursejournalbackend.dto.UserDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {
    Page<UserDto> getPage(Pageable pageable);

    UserDto getById(long id);

    UserDto getByUsername(String username);

    UserDto createUser(CreateEditUserDto dto);

    UserDto updateUser(long id, CreateEditUserDto dto);

    UserDto patchUser(long id, CreateEditUserDto dto);

    void delete(long id);
}
