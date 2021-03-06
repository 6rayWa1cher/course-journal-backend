package com.a6raywa1cher.coursejournalbackend.rest;

import com.a6raywa1cher.coursejournalbackend.dto.GroupDto;
import com.a6raywa1cher.coursejournalbackend.rest.dto.GroupRestDto;
import com.a6raywa1cher.coursejournalbackend.rest.dto.MapStructRestDtoMapper;
import com.a6raywa1cher.coursejournalbackend.rest.dto.groups.OnCreate;
import com.a6raywa1cher.coursejournalbackend.rest.dto.groups.OnUpdate;
import com.a6raywa1cher.coursejournalbackend.service.GroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/groups")
public class GroupController {

    private final GroupService service;

    private final MapStructRestDtoMapper mapper;

    @Autowired
    public GroupController(GroupService service, MapStructRestDtoMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @GetMapping("/{id}")
    public GroupDto getById(@PathVariable long id) {
        return service.getById(id);
    }


    @GetMapping("/faculty/{id}")
    public List<GroupDto> getByFaculty(@PathVariable long id) {
        return service.getByFaculty(id, Sort.by("id"));
    }

    @GetMapping("/course/{id}")
    @PreAuthorize("@accessChecker.readCourseAccess(#id, authentication)")
    public List<GroupDto> getByCourse(@PathVariable long id) {
        return service.getByCourse(id, Sort.by("id"));
    }

    @PostMapping("/")
    @PreAuthorize("@accessChecker.createGroupAccess(authentication)")
    @ResponseStatus(HttpStatus.CREATED)
    @Validated(OnCreate.class)
    public GroupDto create(@RequestBody @Valid GroupRestDto dto) {
        return service.create(mapper.map(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@accessChecker.editGroupAccess(authentication)")
    @Validated(OnUpdate.class)
    public GroupDto update(@RequestBody @Valid GroupRestDto dto, @PathVariable long id) {
        return service.update(id, mapper.map(dto));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("@accessChecker.editGroupAccess(authentication)")
    @Validated(OnUpdate.class)
    public GroupDto patch(@RequestBody @Valid GroupRestDto dto, @PathVariable long id) {
        return service.patch(id, mapper.map(dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@accessChecker.editGroupAccess(authentication)")
    public void delete(@PathVariable long id) {
        service.delete(id);
    }
}
