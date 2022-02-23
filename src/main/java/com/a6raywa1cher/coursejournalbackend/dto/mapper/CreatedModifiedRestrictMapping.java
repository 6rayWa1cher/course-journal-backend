package com.a6raywa1cher.coursejournalbackend.dto.mapper;

import org.mapstruct.Mapping;

@Mapping(target = "createdAt", ignore = true)
@Mapping(target = "lastModifiedAt", ignore = true)
public @interface CreatedModifiedRestrictMapping {
}
