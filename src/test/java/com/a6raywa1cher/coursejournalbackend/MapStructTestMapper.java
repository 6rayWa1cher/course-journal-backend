package com.a6raywa1cher.coursejournalbackend;

import com.a6raywa1cher.coursejournalbackend.dto.*;
import com.a6raywa1cher.coursejournalbackend.dto.mapper.MapperHelper;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(uses = {MapperHelper.class}, componentModel = "spring")
public interface MapStructTestMapper {
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void merge(StudentDto dto, @MappingTarget StudentDto target);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void merge(TaskDto dto, @MappingTarget TaskDto target);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void merge(CriteriaDto dto, @MappingTarget CriteriaDto target);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void merge(UserDto dto, @MappingTarget UserDto target);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void merge(CourseDto dto, @MappingTarget CourseDto target);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void merge(CourseTokenDto dto, @MappingTarget CourseTokenDto target);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void merge(SubmissionDto dto, @MappingTarget SubmissionDto target);
}
