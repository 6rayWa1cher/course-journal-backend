package com.a6raywa1cher.coursejournalbackend.dto.mapper;

import org.mapstruct.Mapping;

@Mapping(target = "createdAt", qualifiedByName = {"MapperHelper", "FromLocalDateTime"})
@Mapping(target = "lastModifiedAt", qualifiedByName = {"MapperHelper", "FromLocalDateTime"})
public @interface CreatedModifiedMapping {
}
