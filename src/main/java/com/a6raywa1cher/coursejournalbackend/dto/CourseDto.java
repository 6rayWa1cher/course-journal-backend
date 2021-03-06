package com.a6raywa1cher.coursejournalbackend.dto;

import lombok.Builder;
import lombok.Data;

import java.time.ZonedDateTime;

@Data
@Builder
public class CourseDto {
    private Long id;

    private String name;

    private Long owner;

    private ZonedDateTime createdAt;

    private ZonedDateTime lastModifiedAt;
}
