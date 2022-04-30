package com.a6raywa1cher.coursejournalbackend.dto.mapper;

import com.a6raywa1cher.coursejournalbackend.dto.*;
import com.a6raywa1cher.coursejournalbackend.model.*;
import com.a6raywa1cher.coursejournalbackend.service.EmployeeService;
import org.mapstruct.*;

@Mapper(uses = {MapperHelper.class, EmployeeService.class}, componentModel = "spring")
public abstract class MapStructMapper {
    // ================================================================================================================
    // AuthUser
    // ================================================================================================================

    @Mapping(target = "createdAt", qualifiedByName = {"MapperHelper", "FromLocalDateTime"})
    @Mapping(target = "lastModifiedAt", qualifiedByName = {"MapperHelper", "FromLocalDateTime"})
    @Mapping(target = "lastVisitAt", qualifiedByName = {"MapperHelper", "FromLocalDateTime"})
    @Mapping(target = "student", source = "student.id")
    @Mapping(target = "employee", source = "employee.id")
    public abstract AuthUserDto map(AuthUser authUser);

    @CreateEditUserDtoToUserMapping
    @Mapping(target = "employee", ignore = true)
    @Mapping(target = "student", ignore = true)
    public abstract void put(CreateEditAuthUserDto dto, @MappingTarget AuthUser authUser);

    @CreateEditUserDtoToUserMapping
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "employee", ignore = true)
    @Mapping(target = "student", ignore = true)
    public abstract void patch(CreateEditAuthUserDto dto, @MappingTarget AuthUser authUser);

    // ================================================================================================================
    // Employee
    // ================================================================================================================

    @CreatedModifiedMapping
    public abstract EmployeeDto map(Employee employee);

    @CreatedModifiedRestrictMapping
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "courseList", ignore = true)
    @Mapping(target = "authUser", ignore = true)
    public abstract void put(EmployeeDto dto, @MappingTarget Employee employee);

    @CreatedModifiedRestrictMapping
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "courseList", ignore = true)
    @Mapping(target = "authUser", ignore = true)
    public abstract void patch(EmployeeDto dto, @MappingTarget Employee employee);

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
    @Mapping(target = "courseToken", ignore = true)
    public abstract void put(CourseDto dto, @MappingTarget Course target);

    @CreatedModifiedRestrictMapping
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "owner", ignore = true)
    @Mapping(target = "students", ignore = true)
    @Mapping(target = "tasks", ignore = true)
    @Mapping(target = "courseToken", ignore = true)
    public abstract void patch(CourseDto dto, @MappingTarget Course target);

    // ================================================================================================================
    // Course
    // ================================================================================================================

    @CreatedModifiedMapping
    @Mapping(target = "course", source = "course.id")
    @Mapping(target = "announcementAt", qualifiedByName = {"MapperHelper", "FromLocalDateTime"})
    @Mapping(target = "softDeadlineAt", qualifiedByName = {"MapperHelper", "FromLocalDateTime"})
    @Mapping(target = "hardDeadlineAt", qualifiedByName = {"MapperHelper", "FromLocalDateTime"})
    public abstract TaskDto map(Task task);

    @CreatedModifiedRestrictMapping
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "course", ignore = true)
    @Mapping(target = "criteria", ignore = true)
    @Mapping(target = "submissions", ignore = true)
    @Mapping(target = "announcementAt", qualifiedByName = {"MapperHelper", "ToLocalDateTime"})
    @Mapping(target = "softDeadlineAt", qualifiedByName = {"MapperHelper", "ToLocalDateTime"})
    @Mapping(target = "hardDeadlineAt", qualifiedByName = {"MapperHelper", "ToLocalDateTime"})
    public abstract void put(TaskDto dto, @MappingTarget Task target);

    @CreatedModifiedRestrictMapping
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "course", ignore = true)
    @Mapping(target = "criteria", ignore = true)
    @Mapping(target = "submissions", ignore = true)
    @Mapping(target = "announcementAt", qualifiedByName = {"MapperHelper", "ToLocalDateTime"})
    @Mapping(target = "softDeadlineAt", qualifiedByName = {"MapperHelper", "ToLocalDateTime"})
    @Mapping(target = "hardDeadlineAt", qualifiedByName = {"MapperHelper", "ToLocalDateTime"})
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
    @Mapping(target = "group", source = "group.id")
    public abstract StudentDto map(Student student);

    @CreatedModifiedRestrictMapping
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "course", ignore = true)
    @Mapping(target = "submissions", ignore = true)
    @Mapping(target = "group", ignore = true)
    @Mapping(target = "authUser", ignore = true)
    public abstract void put(StudentDto dto, @MappingTarget Student target);

    @CreatedModifiedRestrictMapping
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "course", ignore = true)
    @Mapping(target = "submissions", ignore = true)
    @Mapping(target = "group", ignore = true)
    @Mapping(target = "authUser", ignore = true)
    public abstract void patch(StudentDto dto, @MappingTarget Student target);

    // ================================================================================================================
    // Attendance
    // ================================================================================================================

    @CreatedModifiedMapping
    @Mapping(target = "student", source = "student.id")
    @Mapping(target = "course", source = "course.id")
    public abstract AttendanceDto map(Attendance attendance);

    @CreatedModifiedRestrictMapping
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "student", ignore = true)
    @Mapping(target = "course", ignore = true)
    @Mapping(target = "attendedClass", ignore = true)
    @Mapping(target = "attendedDate", ignore = true)
    public abstract void put(AttendanceDto dto, @MappingTarget Attendance target);


    @CreatedModifiedRestrictMapping
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "student", ignore = true)
    @Mapping(target = "course", ignore = true)
    @Mapping(target = "attendedClass", ignore = true)
    @Mapping(target = "attendedDate", ignore = true)
    public abstract void patch(AttendanceDto dto, @MappingTarget Attendance target);

    // ================================================================================================================
    // Submission
    // ================================================================================================================

    @CreatedModifiedMapping
    @Mapping(target = "task", source = "task.id")
    @Mapping(target = "student", source = "student.id")
    @Mapping(target = "submittedAt", qualifiedByName = {"MapperHelper", "FromLocalDateTime"})
    @Mapping(target = "satisfiedCriteria", qualifiedByName = {"MapperHelper", "ExtractIds"})
    public abstract SubmissionDto map(Submission submission);

    @CreatedModifiedRestrictMapping
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "mainScore", ignore = true)
    @Mapping(target = "satisfiedCriteria", ignore = true)
    @Mapping(target = "task", ignore = true)
    @Mapping(target = "student", ignore = true)
    @Mapping(target = "submittedAt", qualifiedByName = {"MapperHelper", "ToLocalDateTime"})
    public abstract void put(SubmissionDto dto, @MappingTarget Submission target);

    @CreatedModifiedRestrictMapping
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "mainScore", ignore = true)
    @Mapping(target = "satisfiedCriteria", ignore = true)
    @Mapping(target = "task", ignore = true)
    @Mapping(target = "student", ignore = true)
    @Mapping(target = "submittedAt", qualifiedByName = {"MapperHelper", "ToLocalDateTime"})
    public abstract void patch(SubmissionDto dto, @MappingTarget Submission target);

    // ================================================================================================================
    // CourseToken
    // ================================================================================================================

    @CreatedModifiedMapping
    @Mapping(target = "course", source = "course.id")
    public abstract CourseTokenDto map(CourseToken criteria);

    // ================================================================================================================
    // Group
    // ================================================================================================================

    @CreatedModifiedMapping
    @Mapping(target = "course", source = "course.id")
    @Mapping(target = "faculty", source = "faculty.id")
    public abstract GroupDto map(Group group);

    @CreatedModifiedRestrictMapping
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "course", ignore = true)
    @Mapping(target = "faculty", ignore = true)
    @Mapping(target = "students", ignore = true)
    public abstract void put(GroupDto dto, @MappingTarget Group target);

    @CreatedModifiedRestrictMapping
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "course", ignore = true)
    @Mapping(target = "faculty", ignore = true)
    @Mapping(target = "students", ignore = true)
    public abstract void patch(GroupDto dto, @MappingTarget Group target);

    // ================================================================================================================
    // Faculty
    // ================================================================================================================

    @CreatedModifiedMapping
    public abstract FacultyDto map(Faculty faculty);

    @CreatedModifiedRestrictMapping
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "groups", ignore = true)
    public abstract void put(FacultyDto dto, @MappingTarget Faculty target);

    @CreatedModifiedRestrictMapping
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "groups", ignore = true)
    public abstract void patch(FacultyDto dto, @MappingTarget Faculty target);
}
