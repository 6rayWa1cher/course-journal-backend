package com.a6raywa1cher.coursejournalbackend.rest.dto;

import com.a6raywa1cher.coursejournalbackend.rest.dto.groups.OnCreate;
import com.a6raywa1cher.coursejournalbackend.rest.dto.groups.OnUpdate;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
public class EmployeeRestDto {
    @NotBlank(groups = {OnCreate.class, OnUpdate.class})
    @Size(max = 50)
    private String firstName;

    @Size(max = 50)
    private String middleName;

    @NotBlank(groups = {OnCreate.class, OnUpdate.class})
    @Size(max = 50)
    private String lastName;
}
