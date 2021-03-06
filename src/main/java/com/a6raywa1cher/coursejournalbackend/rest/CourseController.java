package com.a6raywa1cher.coursejournalbackend.rest;

import com.a6raywa1cher.coursejournalbackend.dto.CourseDto;
import com.a6raywa1cher.coursejournalbackend.dto.CourseFullDto;
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
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Pattern;

import static com.a6raywa1cher.coursejournalbackend.validation.RegexLibrary.COMMON_NAME;

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
    @Secured("ROLE_ADMIN")
    public Page<CourseDto> getList(@ParameterObject Pageable page) {
        return service.getPage(page);
    }

    @GetMapping("/{id}")
    @PreAuthorize("@accessChecker.readCourseAccess(#id, authentication)")
    public CourseFullDto getById(@PathVariable long id) {
        return service.getById(id);
    }

    @GetMapping("/name")
    @Secured("ROLE_ADMIN")
    public Page<CourseDto> findByName(@RequestParam @Pattern(regexp = COMMON_NAME) @Valid String query,
                                      @ParameterObject Pageable page) {
        return service.getByNameContains(query, page);
    }

    @GetMapping("/owner/{id}")
    @PreAuthorize("@accessChecker.readEmployeeAccess(#id, authentication)")
    public Page<CourseDto> findByOwner(@PathVariable long id,
                                       @RequestParam(required = false) @Pattern(regexp = COMMON_NAME) @Valid String name,
                                       @ParameterObject Pageable pageable) {
        if (name == null) {
            return service.getByOwner(id, pageable);
        } else {
            return service.getByOwnerAndNameContains(id, name, pageable);
        }
    }

    @GetMapping("/group/{id}")
    @PreAuthorize("@accessChecker.readCourseByHeadman(#id, authentication)")
    public Page<CourseFullDto> findByGroup(@PathVariable long id, @ParameterObject Pageable pageable) {
        return service.getByGroupId(id, pageable);
    }

    @PostMapping("/")
    @PreAuthorize("@accessChecker.createCourseAccess(#dto.owner, authentication)")
    @ResponseStatus(HttpStatus.CREATED)
    @Validated(OnCreate.class)
    public CourseFullDto create(@RequestBody @Valid CourseRestDto dto) {
        return service.create(mapper.mapFull(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@accessChecker.editCourseAccessWithDto(#id, #dto, authentication)")
    @Validated(OnUpdate.class)
    public CourseFullDto update(@RequestBody @Valid CourseRestDto dto, @PathVariable long id) {
        return service.update(id, mapper.mapFull(dto));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("@accessChecker.editCourseAccessWithDto(#id, #dto, authentication)")
    @Validated(OnPatch.class)
    public CourseFullDto patch(@RequestBody @Valid CourseRestDto dto, @PathVariable long id) {
        return service.patch(id, mapper.mapFull(dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@accessChecker.editCourseAccess(#id, authentication)")
    public void delete(@PathVariable long id) {
        service.delete(id);
    }
}
