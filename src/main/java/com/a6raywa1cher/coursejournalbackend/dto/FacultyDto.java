package com.a6raywa1cher.coursejournalbackend.dto;

import lombok.Builder;
import lombok.Data;

import java.time.ZonedDateTime;

@Data
@Builder
public class FacultyDto {
    private Long id;

    private String name;

    private ZonedDateTime createdAt;

    private ZonedDateTime lastModifiedAt;
}
