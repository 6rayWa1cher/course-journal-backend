package com.a6raywa1cher.coursejournalbackend.rest;

import com.a6raywa1cher.coursejournalbackend.dto.CourseDto;
import com.a6raywa1cher.coursejournalbackend.dto.CourseTokenDto;
import com.a6raywa1cher.coursejournalbackend.dto.exc.CrossTokenRequestException;
import com.a6raywa1cher.coursejournalbackend.dto.exc.NotFoundException;
import com.a6raywa1cher.coursejournalbackend.rest.dto.CourseTokenRestDto;
import com.a6raywa1cher.coursejournalbackend.rest.dto.MapStructRestDtoMapper;
import com.a6raywa1cher.coursejournalbackend.rest.dto.ResolveCourseTokenRestDto;
import com.a6raywa1cher.coursejournalbackend.rest.dto.groups.OnCreate;
import com.a6raywa1cher.coursejournalbackend.service.CourseTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/courses")
public class CourseTokenController {
    private final CourseTokenService service;
    private final MapStructRestDtoMapper mapper;

    @Autowired
    public CourseTokenController(CourseTokenService service, MapStructRestDtoMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @GetMapping("/tokens/{id}")
    @PreAuthorize("@accessChecker.readCourseTokenAccess(#id, authentication)")
    public CourseTokenDto getById(@PathVariable long id) {
        return service.getById(id);
    }

    @GetMapping("/{id}/token")
    @PreAuthorize("@accessChecker.readCourseAccess(#id, authentication)")
    public CourseTokenDto getByCourseId(@PathVariable long id) {
        return service.getByCourseId(id);
    }

    @PostMapping("/tokens/resolve")
    @PostAuthorize("""
            @accessChecker.readCourseAccess(returnObject.id, authentication) and
            @accessChecker.courseTokenAuth(authentication)""")
    public CourseDto resolveToken(@RequestBody @Valid ResolveCourseTokenRestDto dto) {
        try {
            return service.resolveToken(dto.getToken());
        } catch (NotFoundException e) {
            throw new CrossTokenRequestException(e);
        }
    }

    @PostMapping("/tokens")
    @PreAuthorize("@accessChecker.createCourseTokenAccess(#dto.course, authentication)")
    @ResponseStatus(HttpStatus.CREATED)
    @Validated(OnCreate.class)
    public CourseTokenDto create(@RequestBody @Valid CourseTokenRestDto dto) {
        return service.create(mapper.map(dto));
    }

    @DeleteMapping("/tokens/{id}")
    @PreAuthorize("@accessChecker.editCourseTokenAccess(#id, authentication)")
    public void delete(@PathVariable long id) {
        service.delete(id);
    }
}
