package com.a6raywa1cher.coursejournalbackend.rest;

import com.a6raywa1cher.coursejournalbackend.dto.StudentDto;
import com.a6raywa1cher.coursejournalbackend.rest.dto.BatchCreateStudentDto;
import com.a6raywa1cher.coursejournalbackend.rest.dto.MapStructRestDtoMapper;
import com.a6raywa1cher.coursejournalbackend.rest.dto.StudentRestDto;
import com.a6raywa1cher.coursejournalbackend.rest.dto.groups.OnCreate;
import com.a6raywa1cher.coursejournalbackend.rest.dto.groups.OnPatch;
import com.a6raywa1cher.coursejournalbackend.rest.dto.groups.OnUpdate;
import com.a6raywa1cher.coursejournalbackend.service.StudentService;
import org.springdoc.api.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/students")
public class StudentController {
    private final StudentService service;
    private final MapStructRestDtoMapper mapper;

    @Autowired
    public StudentController(StudentService service, MapStructRestDtoMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @GetMapping("/{id}")
    @PreAuthorize("@accessChecker.readStudentAccess(#id, authentication)")
    public StudentDto getById(@PathVariable long id) {
        return service.getById(id);
    }

    @GetMapping("/course/{id}")
    @PreAuthorize("@accessChecker.readCourseAccess(#id, authentication)")
    public Page<StudentDto> getByCourse(@PathVariable long id, @ParameterObject Pageable pageable) {
        return service.getByCourseId(id, pageable);
    }

    @GetMapping("/course/{id}/all")
    @PreAuthorize("@accessChecker.readCourseAccess(#id, authentication)")
    public List<StudentDto> getAllByCourse(@PathVariable long id) {
        return service.getByCourseId(id).stream()
                .sorted(Comparator.comparing(s -> s.getLastName() + "1" + s.getFirstName() + "1" + s.getLastName()))
                .toList();
    }

    @PostMapping("/")
    @PreAuthorize("@accessChecker.createStudentAccess(#dto.course, authentication)")
    @ResponseStatus(HttpStatus.CREATED)
    @Validated(OnCreate.class)
    public StudentDto create(@RequestBody @Valid StudentRestDto dto) {
        return service.create(mapper.map(dto));
    }

    @PostMapping("/batch")
    @PreAuthorize("@accessChecker.createStudentAccess(#dto.course, authentication)")
    @ResponseStatus(HttpStatus.CREATED)
    @Validated(OnCreate.class)
    public List<StudentDto> batchCreate(@RequestBody @Valid BatchCreateStudentDto dto) {
        return service.batchCreate(dto.getStudents().stream()
                .map(mapper::map)
                .peek(d -> d.setCourse(dto.getCourse()))
                .toList());
    }

    @PutMapping("/{id}")
    @PreAuthorize("@accessChecker.editStudentAccess(#id, authentication)")
    @Validated(OnUpdate.class)
    public StudentDto update(@RequestBody @Valid StudentRestDto dto, @PathVariable long id) {
        return service.update(id, mapper.map(dto));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("@accessChecker.editStudentAccess(#id, authentication)")
    @Validated(OnPatch.class)
    public StudentDto patch(@RequestBody @Valid StudentRestDto dto, @PathVariable long id) {
        return service.patch(id, mapper.map(dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@accessChecker.editStudentAccess(#id, authentication)")
    public void delete(@PathVariable long id) {
        service.delete(id);
    }
}
