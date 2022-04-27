package com.a6raywa1cher.coursejournalbackend.rest.dto;

import com.a6raywa1cher.coursejournalbackend.rest.dto.groups.OnCreate;
import com.a6raywa1cher.coursejournalbackend.rest.dto.groups.OnUpdate;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;

@Data
public class GroupRestDto {
    @NotNull(groups = {OnCreate.class, OnUpdate.class})
    @Positive
    private long course;

    @NotNull(groups = {OnCreate.class, OnUpdate.class})
    @Positive
    private long faculty;

    @NotBlank(groups = {OnCreate.class, OnUpdate.class})
    @Size(max = 255, message = "Group name's length cannot be more than 255 symbols")
    private String name;
}
