package com.a6raywa1cher.coursejournalbackend.dto.mapper;

import com.a6raywa1cher.coursejournalbackend.dto.UserDto;
import com.a6raywa1cher.coursejournalbackend.model.User;
import org.mapstruct.Mapper;

@Mapper(uses = {EntityResolver.class})
public interface MapStructMapper {
    UserDto toUserDto(User user);

    User toUser(UserDto dto);
}
