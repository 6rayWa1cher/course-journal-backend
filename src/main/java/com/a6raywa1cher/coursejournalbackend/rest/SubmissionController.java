package com.a6raywa1cher.coursejournalbackend.rest;

import com.a6raywa1cher.coursejournalbackend.dto.SubmissionDto;
import com.a6raywa1cher.coursejournalbackend.rest.dto.BatchSetForStudentAndCourseSubmissionRestDto;
import com.a6raywa1cher.coursejournalbackend.rest.dto.MapStructRestDtoMapper;
import com.a6raywa1cher.coursejournalbackend.rest.dto.SubmissionRestDto;
import com.a6raywa1cher.coursejournalbackend.rest.dto.groups.OnCreate;
import com.a6raywa1cher.coursejournalbackend.rest.dto.groups.OnPatch;
import com.a6raywa1cher.coursejournalbackend.rest.dto.groups.OnUpdate;
import com.a6raywa1cher.coursejournalbackend.service.SubmissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/submissions")
public class SubmissionController {
    private final SubmissionService service;
    private final MapStructRestDtoMapper mapper;

    @Autowired
    public SubmissionController(SubmissionService service, MapStructRestDtoMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @GetMapping("/{id}")
    @PreAuthorize("@accessChecker.readSubmissionAccess(#id, authentication)")
    public SubmissionDto getById(@PathVariable long id) {
        return service.getById(id);
    }

    @GetMapping("/task/{id}")
    @PreAuthorize("@accessChecker.readTaskAccess(#id, authentication)")
    public List<SubmissionDto> getByTask(@PathVariable long id) {
        return service.getByTask(id, Sort.by("id"));
    }

    @GetMapping("/course/{id}")
    @PreAuthorize("@accessChecker.readCourseAccess(#id, authentication)")
    public List<SubmissionDto> getByCourse(@PathVariable long id) {
        return service.getByCourse(id, Sort.by("id"));
    }

    @GetMapping("/course/{cid}/student/{sid}")
    @PreAuthorize("@accessChecker.readCourseAccess(#cid, authentication)")
    public List<SubmissionDto> getByCourseAndStudent(@PathVariable long cid, @PathVariable long sid) {
        return service.getByStudentAndCourse(sid, cid, Sort.by("id"));
    }

    @PostMapping("/")
    @PreAuthorize("@accessChecker.createSubmissionAccess(#dto.task, authentication)")
    @ResponseStatus(HttpStatus.CREATED)
    @Validated(OnCreate.class)
    public SubmissionDto create(@RequestBody @Valid SubmissionRestDto dto) {
        return service.create(mapper.map(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@accessChecker.editSubmissionAccess(#id, authentication)")
    @Validated(OnUpdate.class)
    public SubmissionDto update(@RequestBody @Valid SubmissionRestDto dto, @PathVariable long id) {
        return service.update(id, mapper.map(dto));
    }

    @PostMapping("/course/{cid}/student/{sid}/set")
    @PreAuthorize("@accessChecker.editCourseAccess(#cid, authentication)")
    @Validated(OnUpdate.class)
    public List<SubmissionDto> setForStudentAndCourse(
            @RequestBody @Valid BatchSetForStudentAndCourseSubmissionRestDto dto,
            @PathVariable long cid,
            @PathVariable long sid
    ) {
        return service.setForStudentAndCourse(
                        sid,
                        cid,
                        dto.getSubmissions().stream()
                                .map(mapper::map)
                                .toList()
                )
                .stream()
                .sorted(Comparator.comparingLong(SubmissionDto::getId))
                .toList();
    }

    @PatchMapping("/{id}")
    @PreAuthorize("@accessChecker.editSubmissionAccess(#id, authentication)")
    @Validated(OnPatch.class)
    public SubmissionDto patch(@RequestBody @Valid SubmissionRestDto dto, @PathVariable long id) {
        return service.patch(id, mapper.map(dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@accessChecker.editSubmissionAccess(#id, authentication)")
    public void delete(@PathVariable long id) {
        service.delete(id);
    }
}
