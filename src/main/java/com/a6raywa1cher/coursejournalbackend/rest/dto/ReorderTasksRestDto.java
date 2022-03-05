package com.a6raywa1cher.coursejournalbackend.rest.dto;

import com.a6raywa1cher.coursejournalbackend.validation.NoConflictsInTaskNumbers;
import com.a6raywa1cher.coursejournalbackend.validation.UniqueIds;
import lombok.Data;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;
import java.util.List;

@Data
public class ReorderTasksRestDto {
    @NotNull
    @Size(min = 1)
    @Valid
    @NoConflictsInTaskNumbers
    @UniqueIds
    private List<ReorderRequest> order;

    @Data
    @Validated
    public static class ReorderRequest {
        @NotNull
        @Positive
        private Long id;

        @NotNull
        private Integer number;
    }
}
