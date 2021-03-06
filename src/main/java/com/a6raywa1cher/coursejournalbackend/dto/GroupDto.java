package com.a6raywa1cher.coursejournalbackend.dto;

import lombok.Builder;
import lombok.Data;

import java.time.ZonedDateTime;

@Data
@Builder
public class GroupDto {
    private Long id;

    private Long faculty;

    private String name;

    private ZonedDateTime createdAt;

    private ZonedDateTime lastModifiedAt;

}
