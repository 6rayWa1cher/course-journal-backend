package com.a6raywa1cher.coursejournalbackend.rest;

import com.a6raywa1cher.coursejournalbackend.dto.AttendanceDto;
import com.a6raywa1cher.coursejournalbackend.rest.dto.AttendanceRestDto;
import com.a6raywa1cher.coursejournalbackend.rest.dto.BatchCreateAttendancesDto;
import com.a6raywa1cher.coursejournalbackend.rest.dto.MapStructRestDtoMapper;
import com.a6raywa1cher.coursejournalbackend.rest.dto.groups.OnCreate;
import com.a6raywa1cher.coursejournalbackend.rest.dto.groups.OnPatch;
import com.a6raywa1cher.coursejournalbackend.rest.dto.groups.OnUpdate;
import com.a6raywa1cher.coursejournalbackend.service.AttendanceService;
import org.springdoc.api.annotations.ParameterObject;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/attendances")
public class AttendanceController {
    private final AttendanceService service;
    private final MapStructRestDtoMapper mapper;

    public AttendanceController(AttendanceService service, MapStructRestDtoMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @GetMapping("/{id}")
    @PreAuthorize("@accessChecker.readAttendanceAccess(#id, authentication)")
    public AttendanceDto getById(@PathVariable long id) { return service.getById(id); }

    @GetMapping("/student/{id}")
    @PreAuthorize("@accessChecker.readStudentAccess(#id, authentication)")
    public List<AttendanceDto> getByStudent(@PathVariable long id, @ParameterObject Sort sort) { return service.getByStudentId(id, sort); }

    @GetMapping("/course/{id}")
    @PreAuthorize("@accessChecker.readCourseAccess(#id, authentication)")
    public List<AttendanceDto> getByCourse(@PathVariable long id, @ParameterObject Sort sort) { return service.getByCourseId(id, sort); }

    @GetMapping("/course/{courseId}/student/{studentId}")
    @PreAuthorize("@accessChecker.readCourseAccess(#courseId, authentication)")
    public List<AttendanceDto> getByCourseAndStudent(@PathVariable long courseId, @PathVariable long studentId, @ParameterObject Sort sort) {
        return service.getByCourseAndStudentIds(courseId, studentId, sort);
    }

    @PostMapping("/")
    @PreAuthorize("@accessChecker.createAttendanceAccess(#dto.course, authentication)")
    @ResponseStatus(HttpStatus.CREATED)
    @Validated(OnCreate.class)
    public AttendanceDto create(@RequestBody @Valid AttendanceRestDto dto) { return service.create(mapper.map(dto)); }

    @PostMapping("/batch")
    @PreAuthorize("@accessChecker.createAttendanceAccess(#dto.course, authentication)")
    @ResponseStatus(HttpStatus.CREATED)
    @Validated(OnCreate.class)
    public List<AttendanceDto> batchCreate(@RequestBody @Valid BatchCreateAttendancesDto dto) {
        return service.batchCreate(dto.getAttendances().stream()
                .map(mapper::map)
                .peek(d -> d.setCourse(dto.getCourse()))
                .toList());
    }

    @PutMapping("/{id}")
    @PreAuthorize("@accessChecker.editAttendanceAccess(#id, authentication)")
    @Validated(OnUpdate.class)
    public AttendanceDto update(@RequestBody @Valid AttendanceRestDto dto, @PathVariable long id) {
        return service.update(id, mapper.map(dto));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("@accessChecker.editAttendanceAccess(#id, authentication)")
    @Validated(OnPatch.class)
    public AttendanceDto patch(@RequestBody @Valid AttendanceRestDto dto, @PathVariable long id) {
        return service.patch(id, mapper.map(dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@accessChecker.editAttendanceAccess(#id, authentication)")
    public void delete(@PathVariable long id) { service.delete(id); }
}
