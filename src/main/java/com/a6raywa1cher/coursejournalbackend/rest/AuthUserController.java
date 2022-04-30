package com.a6raywa1cher.coursejournalbackend.rest;

import com.a6raywa1cher.coursejournalbackend.dto.AuthUserDto;
import com.a6raywa1cher.coursejournalbackend.rest.dto.AuthUserRestDto;
import com.a6raywa1cher.coursejournalbackend.rest.dto.MapStructRestDtoMapper;
import com.a6raywa1cher.coursejournalbackend.rest.dto.groups.OnCreate;
import com.a6raywa1cher.coursejournalbackend.rest.dto.groups.OnPatch;
import com.a6raywa1cher.coursejournalbackend.rest.dto.groups.OnUpdate;
import com.a6raywa1cher.coursejournalbackend.service.AuthUserService;
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
@RequestMapping("/auth-user")
public class AuthUserController {
    private final AuthUserService service;
    private final MapStructRestDtoMapper mapper;

    @Autowired
    public AuthUserController(AuthUserService service, MapStructRestDtoMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @GetMapping("/")
    public Page<AuthUserDto> getAuthUserList(@ParameterObject Pageable page) {
        return service.getPage(page);
    }

    @GetMapping("/{id}")
    public AuthUserDto getAuthUserById(@PathVariable long id) {
        return service.getById(id);
    }

    @GetMapping("/employee/{id}")
    public AuthUserDto getAuthUserByEmployeeId(@PathVariable long id) {
        return service.getByEmployeeId(id);
    }

    @GetMapping("/student/{id}")
    public AuthUserDto getAuthUserByStudentId(@PathVariable long id) {
        return service.getByStudentId(id);
    }

    @PostMapping("/")
    @PreAuthorize("@accessChecker.createEmployeeAccess(authentication)")
    @ResponseStatus(HttpStatus.CREATED)
    @Validated(OnCreate.class)
    public AuthUserDto createAuthUser(@RequestBody @Valid AuthUserRestDto dto) {
        return service.create(mapper.map(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@accessChecker.editEmployeeAccess(#id, authentication)")
    @Validated(OnUpdate.class)
    public AuthUserDto updateEmployee(@RequestBody @Valid AuthUserRestDto dto, @PathVariable long id) {
        return service.update(id, mapper.map(dto));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("@accessChecker.editEmployeeAccess(#id, authentication)")
    @Validated(OnPatch.class)
    public AuthUserDto patchEmployee(@RequestBody @Valid AuthUserRestDto dto, @PathVariable long id) {
        return service.patch(id, mapper.map(dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@accessChecker.editEmployeeAccess(#id, authentication)")
    public void deleteEmployee(@PathVariable long id) {
        service.delete(id);
    }
}
