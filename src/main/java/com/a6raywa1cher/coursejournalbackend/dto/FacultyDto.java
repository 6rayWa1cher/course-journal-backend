package com.a6raywa1cher.coursejournalbackend.dto;

import com.a6raywa1cher.coursejournalbackend.model.Group;
import lombok.Builder;
import lombok.Data;

import java.time.ZonedDateTime;
import java.util.List;

@Data
@Builder
public class FacultyDto {
    private Long id;

    private String name;

    private List<Group> groups;

    private ZonedDateTime createdAt;

    private ZonedDateTime lastModifiedAt;
}
