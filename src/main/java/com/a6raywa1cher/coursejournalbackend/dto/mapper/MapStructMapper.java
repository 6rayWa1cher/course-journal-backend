package com.a6raywa1cher.coursejournalbackend.dto.mapper;

import com.a6raywa1cher.coursejournalbackend.dto.CourseDto;
import com.a6raywa1cher.coursejournalbackend.dto.CreateEditUserDto;
import com.a6raywa1cher.coursejournalbackend.dto.TaskDto;
import com.a6raywa1cher.coursejournalbackend.dto.UserDto;
import com.a6raywa1cher.coursejournalbackend.model.Course;
import com.a6raywa1cher.coursejournalbackend.model.Task;
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
    public abstract UserDto map(User user);

    @CreateEditUserDtoToUserMapping
    public abstract void put(CreateEditUserDto dto, @MappingTarget User user);

    @CreateEditUserDtoToUserMapping
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    public abstract void patch(CreateEditUserDto dto, @MappingTarget User user);

    // ================================================================================================================
    // Course
    // ================================================================================================================

    @CreatedModifiedMapping
    @Mapping(target = "owner", source = "owner.id")
    public abstract CourseDto map(Course course);

    @CreatedModifiedRestrictMapping
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "owner", ignore = true)
    @Mapping(target = "students", ignore = true)
    public abstract void put(CourseDto dto, @MappingTarget Course target);

    @CreatedModifiedRestrictMapping
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "owner", ignore = true)
    @Mapping(target = "students", ignore = true)
    public abstract void patch(CourseDto dto, @MappingTarget Course target);

    // ================================================================================================================
    // Course
    // ================================================================================================================

    @CreatedModifiedMapping
    @Mapping(target = "course", source = "course.id")
    public abstract TaskDto map(Task task);

    @CreatedModifiedRestrictMapping
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "course", ignore = true)
    public abstract void put(TaskDto dto, @MappingTarget Task target);

    @CreatedModifiedRestrictMapping
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "course", ignore = true)
    public abstract void patch(TaskDto dto, @MappingTarget Task target);
}
