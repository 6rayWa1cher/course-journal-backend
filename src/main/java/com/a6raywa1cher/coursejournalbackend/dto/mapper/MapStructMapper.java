package com.a6raywa1cher.coursejournalbackend.dto.mapper;

import com.a6raywa1cher.coursejournalbackend.dto.CreateEditUserDto;
import com.a6raywa1cher.coursejournalbackend.dto.UserDto;
import com.a6raywa1cher.coursejournalbackend.model.User;
import org.mapstruct.*;

@Mapper(uses = MapperHelper.class, componentModel = "spring")
public abstract class MapStructMapper {
    @Mapping(target = "id", expression = "java(user.getId())")
    @Mapping(target = "createdAt", qualifiedByName = {"MapperHelper", "FromLocalDateTime"})
    @Mapping(target = "lastModifiedAt", qualifiedByName = {"MapperHelper", "FromLocalDateTime"})
    @Mapping(target = "lastVisitAt", qualifiedByName = {"MapperHelper", "FromLocalDateTime"})
    public abstract UserDto toUserDto(User user);

    @CreateEditUserDtoToUserMapping
    public abstract void toUser(CreateEditUserDto dto, @MappingTarget User user);

    @CreateEditUserDtoToUserMapping
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    public abstract void patchUser(CreateEditUserDto dto, @MappingTarget User user);
}
