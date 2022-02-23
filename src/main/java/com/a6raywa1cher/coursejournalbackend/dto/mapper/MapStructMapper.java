package com.a6raywa1cher.coursejournalbackend.dto.mapper;

import com.a6raywa1cher.coursejournalbackend.dto.CourseDto;
import com.a6raywa1cher.coursejournalbackend.dto.CreateEditUserDto;
import com.a6raywa1cher.coursejournalbackend.dto.UserDto;
import com.a6raywa1cher.coursejournalbackend.model.Course;
import com.a6raywa1cher.coursejournalbackend.model.User;
import com.a6raywa1cher.coursejournalbackend.service.UserService;
import org.mapstruct.*;

@Mapper(uses = {MapperHelper.class, UserService.class}, componentModel = "spring")
public abstract class MapStructMapper {

    // ================================================================================================================
    // User
    // ================================================================================================================

    @Mapping(target = "createdAt", qualifiedByName = {"MapperHelper", "FromLocalDateTime"})
    @Mapping(target = "lastModifiedAt", qualifiedByName = {"MapperHelper", "FromLocalDateTime"})
    @Mapping(target = "lastVisitAt", qualifiedByName = {"MapperHelper", "FromLocalDateTime"})
    public abstract UserDto toUserDto(User user);

    @CreateEditUserDtoToUserMapping
    public abstract void putUser(CreateEditUserDto dto, @MappingTarget User user);

    @CreateEditUserDtoToUserMapping
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    public abstract void patchUser(CreateEditUserDto dto, @MappingTarget User user);

    // ================================================================================================================
    // Course
    // ================================================================================================================

    @CreatedModifiedMapping
    @Mapping(target = "owner", source = "owner.id")
    public abstract CourseDto map(Course course);

    @CreatedModifiedRestrictMapping
    @Mapping(source = "owner", target = "owner", qualifiedByName = {"UserService", "GetRawById"})
    @Mapping(target = "students", ignore = true)
    public abstract void put(CourseDto dto, @MappingTarget Course target);

    @CreatedModifiedRestrictMapping
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(source = "owner", target = "owner", qualifiedByName = {"UserService", "GetRawById"})
    @Mapping(target = "students", ignore = true)
    public abstract void patch(CourseDto dto, @MappingTarget Course target);
}
