package com.a6raywa1cher.coursejournalbackend.rest.dto;

import com.a6raywa1cher.coursejournalbackend.dto.TableDto;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class TableRestDto {
    @NotNull
    @Valid
    private List<TableDto.TableBodyElement> body;

    @NotNull
    @Valid
    private List<TableDto.TableHeaderElement> header;
}
