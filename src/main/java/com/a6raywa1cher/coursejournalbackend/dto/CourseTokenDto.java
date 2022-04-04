package com.a6raywa1cher.coursejournalbackend.dto;

import lombok.Builder;
import lombok.Data;

import java.time.ZonedDateTime;

@Data
@Builder
public class CourseTokenDto {
    private Long id;

    private Long course;

    private String token;

    private ZonedDateTime createdAt;

    private ZonedDateTime lastModifiedAt;
}
