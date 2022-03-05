package com.a6raywa1cher.coursejournalbackend.rest.dto;

import lombok.Data;

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
    private List<ReorderRequest> order;

    @Data
    public static class ReorderRequest {
        @NotNull
        @Positive
        private Long id;

        @NotNull
        private Integer number;
    }
}
