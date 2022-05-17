package com.a6raywa1cher.coursejournalbackend.rest.dto;

import com.a6raywa1cher.coursejournalbackend.validation.UniqueByName;
import lombok.Data;

import javax.validation.constraints.*;
import java.util.List;

import static com.a6raywa1cher.coursejournalbackend.validation.RegexLibrary.COMMON_NAME;

@Data
public class BatchSetForTaskCriteriaDto {
    @NotNull
    @UniqueByName
    private List<CriteriaSetForTaskDto> criteria;

    @Data
    public static final class CriteriaSetForTaskDto {
        @NotBlank
        @Pattern(regexp = COMMON_NAME)
        private String name;

        @NotNull
        @Min(0)
        @Max(100)
        private Integer criteriaPercent;
    }
}
