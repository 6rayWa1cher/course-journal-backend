package com.a6raywa1cher.coursejournalbackend.dto.mapper;

import org.mapstruct.Mapping;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.CLASS)
@Mapping(target = "refreshTokens", ignore = true)
@Mapping(target = "lastVisitAt", ignore = true)
@Mapping(target = "lastModifiedAt", ignore = true)
@Mapping(target = "id", ignore = true)
@Mapping(target = "createdAt", ignore = true)
@Mapping(target = "courseList", ignore = true)
@Mapping(target = "password", qualifiedByName = {"MapperHelper", "EncodePassword"})
public @interface CreateEditUserDtoToUserMapping {
}
