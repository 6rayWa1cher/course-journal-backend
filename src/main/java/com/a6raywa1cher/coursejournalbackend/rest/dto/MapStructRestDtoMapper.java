package com.a6raywa1cher.coursejournalbackend.rest.dto;

import com.a6raywa1cher.coursejournalbackend.dto.CreateEditUserDto;
import com.a6raywa1cher.coursejournalbackend.dto.mapper.MapperHelper;
import org.mapstruct.Mapper;

@Mapper(uses = {MapperHelper.class}, componentModel = "spring")
public interface MapStructRestDtoMapper {
    CreateEditUserDto map(CreateUserDto dto);

    CreateEditUserDto map(EditUserDto dto);
}
