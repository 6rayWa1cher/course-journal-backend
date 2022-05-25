package com.a6raywa1cher.coursejournalbackend.rest;

import com.a6raywa1cher.coursejournalbackend.dto.AttendanceConflictListDto;
import com.a6raywa1cher.coursejournalbackend.dto.AttendanceDto;
import com.a6raywa1cher.coursejournalbackend.dto.TableDto;
import com.a6raywa1cher.coursejournalbackend.rest.dto.AttendanceRestDto;
import com.a6raywa1cher.coursejournalbackend.rest.dto.BatchCreateAttendancesDto;
import com.a6raywa1cher.coursejournalbackend.rest.dto.MapStructRestDtoMapper;
import com.a6raywa1cher.coursejournalbackend.rest.dto.groups.OnCreate;
import com.a6raywa1cher.coursejournalbackend.rest.dto.groups.OnPatch;
import com.a6raywa1cher.coursejournalbackend.rest.dto.groups.OnUpdate;
import com.a6raywa1cher.coursejournalbackend.service.AttendanceService;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDate;
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
    public AttendanceDto getById(@PathVariable long id) {
        return service.getById(id);
    }

    @GetMapping("/course/{id}")
    @PreAuthorize("@accessChecker.readCourseAccess(#id, authentication)")
    public List<AttendanceDto> getByCourse(@PathVariable long id) {
        return service.getByCourseId(id, Sort.by("id"));
    }

    @GetMapping("/course/{courseId}/student/{studentId}")
    @PreAuthorize("@accessChecker.readCourseAccess(#courseId, authentication)")
    public List<AttendanceDto> getByCourseAndStudent(@PathVariable long courseId, @PathVariable long studentId) {
        return service.getByCourseAndStudentIds(courseId, studentId, Sort.by("id"));
    }

    @GetMapping("/table/{courseId}")
    @PreAuthorize("@accessChecker.readCourseAccess(#courseId, authentication)")
    public TableDto getTableByCourseAndDatePeriod(@PathVariable long courseId, @RequestParam String fromDate,
                                             @RequestParam String toDate) {
        return service.getAttendancesTableByDatePeriod(courseId, LocalDate.parse(fromDate), LocalDate.parse(toDate));
    }

    @GetMapping("/conflicts/{courseId}")
    @PreAuthorize("@accessChecker.readCourseAccess(#courseId, authentication)")
    public AttendanceConflictListDto getConflictsInTableByCourseAndDatePeriod(@PathVariable long courseId, @RequestParam String fromDate,
                                                                              @RequestParam String toDate) {
        return service.getAttendanceConflictsByDatePeriodAndClass(courseId, LocalDate.parse(fromDate), LocalDate.parse(toDate));
    }

    @PostMapping("/")
    @PreAuthorize("@accessChecker.createAttendanceAccess(#dto.course, authentication)")
    @ResponseStatus(HttpStatus.CREATED)
    @Validated(OnCreate.class)
    public AttendanceDto create(@RequestBody @Valid AttendanceRestDto dto) {
        return service.create(mapper.map(dto));
    }

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
    public void delete(@PathVariable long id) {
        service.delete(id);
    }
}
