package com.a6raywa1cher.coursejournalbackend.dto;

import lombok.Builder;
import lombok.Data;

import java.time.ZonedDateTime;
import java.util.List;

@Data
@Builder
public class CourseFullDto {
    private Long id;

    private String name;

    private List<Long> students;

    private Long owner;

    private ZonedDateTime createdAt;

    private ZonedDateTime lastModifiedAt;
}
