package com.a6raywa1cher.coursejournalbackend.dto;

import lombok.Builder;
import lombok.Data;

import java.time.ZonedDateTime;

@Data
@Builder
public class CriteriaDto {
    private Long id;

    private Long task;

    private String name;

    private Integer criteriaPercent;

    private ZonedDateTime createdAt;

    private ZonedDateTime lastModifiedAt;
}
