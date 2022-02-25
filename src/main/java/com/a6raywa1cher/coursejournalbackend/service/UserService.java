package com.a6raywa1cher.coursejournalbackend.service;


import com.a6raywa1cher.coursejournalbackend.dto.CreateEditUserDto;
import com.a6raywa1cher.coursejournalbackend.dto.UserDto;
import com.a6raywa1cher.coursejournalbackend.model.User;
import org.mapstruct.Named;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

@Named("UserService")
public interface UserService {
    Page<UserDto> getPage(Pageable pageable);

    UserDto getById(long id);

    @Named("GetRawById")
    Optional<User> findRawById(long id);

    UserDto getByUsername(String username);

    UserDto createUser(CreateEditUserDto dto);

    UserDto updateUser(long id, CreateEditUserDto dto);

    UserDto patchUser(long id, CreateEditUserDto dto);

    void delete(long id);
}
