package com.a6raywa1cher.coursejournalbackend.rest;

import com.a6raywa1cher.coursejournalbackend.dto.CourseDto;
import com.a6raywa1cher.coursejournalbackend.rest.dto.CourseRestDto;
import com.a6raywa1cher.coursejournalbackend.rest.dto.MapStructRestDtoMapper;
import com.a6raywa1cher.coursejournalbackend.rest.dto.groups.OnCreate;
import com.a6raywa1cher.coursejournalbackend.rest.dto.groups.OnPatch;
import com.a6raywa1cher.coursejournalbackend.rest.dto.groups.OnUpdate;
import com.a6raywa1cher.coursejournalbackend.service.CourseService;
import org.springdoc.api.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/courses")
public class CourseController {
    private final CourseService service;
    private final MapStructRestDtoMapper mapper;

    @Autowired
    public CourseController(CourseService service, MapStructRestDtoMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @GetMapping("/")
    public Page<CourseDto> getList(@ParameterObject Pageable page) {
        return service.getPage(page);
    }

    @GetMapping("/{id}")
    public CourseDto getById(@PathVariable long id) {
        return service.getById(id);
    }

    @GetMapping("/name")
    public Page<CourseDto> findByName(@RequestParam String query, @ParameterObject Pageable page) {
        return service.getByNameContains(query, page);
    }

    @PostMapping("/")
    @ResponseStatus(HttpStatus.CREATED)
    @Validated(OnCreate.class)
    public CourseDto create(@RequestBody @Valid CourseRestDto dto) {
        return service.create(mapper.map(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@accessChecker.editCourseAccess(#id, authentication)")
    @Validated(OnUpdate.class)
    public CourseDto update(@RequestBody @Valid CourseRestDto dto, @PathVariable long id) {
        return service.update(id, mapper.map(dto));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("@accessChecker.editCourseAccess(#id, authentication)")
    @Validated(OnPatch.class)
    public CourseDto patch(@RequestBody @Valid CourseRestDto dto, @PathVariable long id) {
        return service.patch(id, mapper.map(dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@accessChecker.editCourseAccess(#id, authentication)")
    public void delete(@PathVariable long id) {
        service.delete(id);
    }
}
