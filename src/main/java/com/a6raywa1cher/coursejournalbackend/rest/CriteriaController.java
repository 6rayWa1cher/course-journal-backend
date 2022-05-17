package com.a6raywa1cher.coursejournalbackend.rest;

import com.a6raywa1cher.coursejournalbackend.dto.CriteriaDto;
import com.a6raywa1cher.coursejournalbackend.rest.dto.BatchSetForTaskCriteriaDto;
import com.a6raywa1cher.coursejournalbackend.rest.dto.CriteriaRestDto;
import com.a6raywa1cher.coursejournalbackend.rest.dto.MapStructRestDtoMapper;
import com.a6raywa1cher.coursejournalbackend.rest.dto.groups.OnCreate;
import com.a6raywa1cher.coursejournalbackend.rest.dto.groups.OnPatch;
import com.a6raywa1cher.coursejournalbackend.rest.dto.groups.OnUpdate;
import com.a6raywa1cher.coursejournalbackend.service.CriteriaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/criteria")
public class CriteriaController {
    private final CriteriaService service;
    private final MapStructRestDtoMapper mapper;

    @Autowired
    public CriteriaController(CriteriaService service, MapStructRestDtoMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @GetMapping("/{id}")
    @PreAuthorize("@accessChecker.readCriteriaAccess(#id, authentication)")
    public CriteriaDto getById(@PathVariable long id) {
        return service.getById(id);
    }

    @GetMapping("/task/{id}")
    @PreAuthorize("@accessChecker.readTaskAccess(#id, authentication)")
    public List<CriteriaDto> getByTask(@PathVariable long id) {
        return service.getByTaskId(id).stream()
                .sorted(Comparator.comparing(CriteriaDto::getId))
                .toList();
    }

    @PostMapping("/")
    @PreAuthorize("@accessChecker.createCriteriaAccess(#dto.task, authentication)")
    @ResponseStatus(HttpStatus.CREATED)
    @Validated(OnCreate.class)
    public CriteriaDto create(@RequestBody @Valid CriteriaRestDto dto) {
        return service.create(mapper.map(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@accessChecker.editCriteriaAccess(#id, authentication)")
    @Validated(OnUpdate.class)
    public CriteriaDto update(@RequestBody @Valid CriteriaRestDto dto, @PathVariable long id) {
        return service.update(id, mapper.map(dto));
    }

    @PostMapping("/task/{id}/set")
    @PreAuthorize("@accessChecker.editTaskAccess(#id, authentication)")
    public List<CriteriaDto> setForTask(@RequestBody @Valid BatchSetForTaskCriteriaDto dto, @PathVariable long id) {
        return service.setForTask(id, dto.getCriteria().stream().map(mapper::map).toList());
    }

    @PatchMapping("/{id}")
    @PreAuthorize("@accessChecker.editCriteriaAccess(#id, authentication)")
    @Validated(OnPatch.class)
    public CriteriaDto patch(@RequestBody @Valid CriteriaRestDto dto, @PathVariable long id) {
        return service.patch(id, mapper.map(dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@accessChecker.editCriteriaAccess(#id, authentication)")
    public void delete(@PathVariable long id) {
        service.delete(id);
    }
}
