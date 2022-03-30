package com.a6raywa1cher.coursejournalbackend.rest.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class ResolveCourseTokenRestDto {
    @NotBlank
    private String token;
}
