package com.a6raywa1cher.coursejournalbackend.rest.dto;

import com.a6raywa1cher.coursejournalbackend.validation.UniqueByName;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.*;
import java.util.List;

import static com.a6raywa1cher.coursejournalbackend.validation.RegexLibrary.CRITERIA_NAME;

@Data
public class BatchSetForTaskCriteriaDto {
    @NotNull
    @UniqueByName
    @Valid
    private List<CriteriaSetForTaskDto> criteria;

    @Data
    public static final class CriteriaSetForTaskDto {
        @NotBlank
        @Pattern(regexp = CRITERIA_NAME)
        private String name;

        @NotNull
        @Min(0)
        @Max(100)
        private Integer criteriaPercent;
    }
}
