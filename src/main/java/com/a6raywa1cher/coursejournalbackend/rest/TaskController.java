package com.a6raywa1cher.coursejournalbackend.rest;

import com.a6raywa1cher.coursejournalbackend.dto.TaskDto;
import com.a6raywa1cher.coursejournalbackend.rest.dto.MapStructRestDtoMapper;
import com.a6raywa1cher.coursejournalbackend.rest.dto.ReorderTasksRestDto;
import com.a6raywa1cher.coursejournalbackend.rest.dto.ShortTaskRestDto;
import com.a6raywa1cher.coursejournalbackend.rest.dto.TaskRestDto;
import com.a6raywa1cher.coursejournalbackend.rest.dto.groups.OnCreate;
import com.a6raywa1cher.coursejournalbackend.rest.dto.groups.OnPatch;
import com.a6raywa1cher.coursejournalbackend.rest.dto.groups.OnUpdate;
import com.a6raywa1cher.coursejournalbackend.service.TaskService;
import org.springdoc.api.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/tasks")
public class TaskController {
    private final TaskService service;
    private final MapStructRestDtoMapper mapper;

    @Autowired
    public TaskController(TaskService service, MapStructRestDtoMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @GetMapping("/{id}")
    @PreAuthorize("@accessChecker.isOwnedByClientOrAdmin(#id, T(com.a6raywa1cher.coursejournalbackend.model.Task), authentication)")
    public TaskDto getById(@PathVariable long id) {
        return service.getById(id);
    }

    @GetMapping("/course/{id}")
    @PreAuthorize("@accessChecker.isOwnedByClientOrAdmin(#id, T(com.a6raywa1cher.coursejournalbackend.model.Course), authentication)")
    public Page<ShortTaskRestDto> getByCourse(@PathVariable long id, @ParameterObject Pageable pageable) {
        return service.getByCourseId(id, pageable).map(mapper::map);
    }

    @PostMapping("/course/{id}/reorder")
    @PreAuthorize("@accessChecker.isOwnedByClientOrAdmin(#id, T(com.a6raywa1cher.coursejournalbackend.model.Course), authentication)")
    public void reorderCourse(
            @PathVariable long id,
            @RequestBody ReorderTasksRestDto dto
    ) {
        service.reorder(id,
                dto.getOrder()
                        .stream()
                        .collect(Collectors.toMap(
                                ReorderTasksRestDto.ReorderRequest::getId,
                                ReorderTasksRestDto.ReorderRequest::getNumber
                        ))
        );
    }

    @GetMapping("/course/{id}/all")
    @PreAuthorize("@accessChecker.isOwnedByClientOrAdmin(#id, T(com.a6raywa1cher.coursejournalbackend.model.Course), authentication)")
    public List<ShortTaskRestDto> getAllByCourse(@PathVariable long id) {
        return service.getByCourseId(id).stream().map(mapper::map).toList();
    }

    @PostMapping("/")
    @PreAuthorize("@accessChecker.isOwnedByClientOrAdmin(#dto.course, T(com.a6raywa1cher.coursejournalbackend.model.Course), authentication)")
    @ResponseStatus(HttpStatus.CREATED)
    @Validated(OnCreate.class)
    public TaskDto create(@RequestBody @Valid TaskRestDto dto) {
        return service.create(mapper.map(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@accessChecker.isOwnedByClientOrAdmin(#id, T(com.a6raywa1cher.coursejournalbackend.model.Task), authentication)")
    @Validated(OnUpdate.class)
    public TaskDto update(@RequestBody @Valid TaskRestDto dto, @PathVariable long id) {
        return service.update(id, mapper.map(dto));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("@accessChecker.isOwnedByClientOrAdmin(#id, T(com.a6raywa1cher.coursejournalbackend.model.Task), authentication)")
    @Validated(OnPatch.class)
    public TaskDto patch(@RequestBody @Valid TaskRestDto dto, @PathVariable long id) {
        return service.patch(id, mapper.map(dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@accessChecker.isOwnedByClientOrAdmin(#id, T(com.a6raywa1cher.coursejournalbackend.model.Task), authentication)")
    public void delete(@PathVariable long id) {
        service.delete(id);
    }
}
