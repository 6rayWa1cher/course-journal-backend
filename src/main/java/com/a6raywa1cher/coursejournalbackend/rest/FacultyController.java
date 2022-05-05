package com.a6raywa1cher.coursejournalbackend.rest;

import com.a6raywa1cher.coursejournalbackend.dto.FacultyDto;
import com.a6raywa1cher.coursejournalbackend.rest.dto.FacultyRestDto;
import com.a6raywa1cher.coursejournalbackend.rest.dto.MapStructRestDtoMapper;
import com.a6raywa1cher.coursejournalbackend.rest.dto.groups.OnCreate;
import com.a6raywa1cher.coursejournalbackend.rest.dto.groups.OnPatch;
import com.a6raywa1cher.coursejournalbackend.rest.dto.groups.OnUpdate;
import com.a6raywa1cher.coursejournalbackend.service.FacultyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/faculties")
public class FacultyController {

    private final FacultyService service;

    private final MapStructRestDtoMapper mapper;

    @Autowired
    public FacultyController(FacultyService facultyService, MapStructRestDtoMapper mapper) {
        this.service = facultyService;
        this.mapper = mapper;
    }

    @GetMapping("/")
    public List<FacultyDto> getAllFaculties() {
        return service.getAll().stream()
                .sorted(Comparator.comparingLong(FacultyDto::getId))
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public FacultyDto getFacultyById(@PathVariable long id) {
        return service.getById(id);
    }

    @PostMapping("/")
    @PreAuthorize("@accessChecker.createFacultyAccess(authentication)")
    @ResponseStatus(HttpStatus.CREATED)
    @Validated(OnCreate.class)
    public FacultyDto create(@RequestBody @Valid FacultyRestDto dto) {
        return service.create(mapper.map(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@accessChecker.editFacultyAccess(authentication)")
    @Validated(OnUpdate.class)
    public FacultyDto update(@RequestBody @Valid FacultyRestDto dto, @PathVariable long id) {
        return service.update(id, mapper.map(dto));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("@accessChecker.editFacultyAccess(authentication)")
    @Validated(OnPatch.class)
    public FacultyDto patch(@RequestBody @Valid FacultyRestDto dto, @PathVariable long id) {
        return service.patch(id, mapper.map(dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@accessChecker.editFacultyAccess(authentication)")
    public void delete(@PathVariable long id) {
        service.delete(id);
    }
}
