package com.a6raywa1cher.coursejournalbackend.dto.mapper;

import com.a6raywa1cher.coursejournalbackend.dto.*;
import com.a6raywa1cher.coursejournalbackend.model.*;
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
    @Mapping(target = "tasks", ignore = true)
    public abstract void put(CourseDto dto, @MappingTarget Course target);

    @CreatedModifiedRestrictMapping
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "owner", ignore = true)
    @Mapping(target = "students", ignore = true)
    @Mapping(target = "tasks", ignore = true)
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
    @Mapping(target = "submissions", ignore = true)
    public abstract void put(TaskDto dto, @MappingTarget Task target);

    @CreatedModifiedRestrictMapping
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "course", ignore = true)
    @Mapping(target = "submissions", ignore = true)
    public abstract void patch(TaskDto dto, @MappingTarget Task target);

    // ================================================================================================================
    // Criteria
    // ================================================================================================================

    @CreatedModifiedMapping
    @Mapping(target = "task", source = "task.id")
    public abstract CriteriaDto map(Criteria criteria);

    @CreatedModifiedRestrictMapping
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "task", ignore = true)
    public abstract void put(CriteriaDto dto, @MappingTarget Criteria target);

    @CreatedModifiedRestrictMapping
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "task", ignore = true)
    public abstract void patch(CriteriaDto dto, @MappingTarget Criteria target);

    // ================================================================================================================
    // Student
    // ================================================================================================================

    @CreatedModifiedMapping
    @Mapping(target = "course", source = "course.id")
    public abstract StudentDto map(Student student);

    @CreatedModifiedRestrictMapping
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "course", ignore = true)
    @Mapping(target = "submissions", ignore = true)
    public abstract void put(StudentDto dto, @MappingTarget Student target);

    @CreatedModifiedRestrictMapping
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "course", ignore = true)
    @Mapping(target = "submissions", ignore = true)
    public abstract void patch(StudentDto dto, @MappingTarget Student target);
}
