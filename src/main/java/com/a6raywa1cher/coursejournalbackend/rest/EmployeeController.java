package com.a6raywa1cher.coursejournalbackend.rest;

import com.a6raywa1cher.coursejournalbackend.dto.EmployeeDto;
import com.a6raywa1cher.coursejournalbackend.rest.dto.EmployeeRestDto;
import com.a6raywa1cher.coursejournalbackend.rest.dto.MapStructRestDtoMapper;
import com.a6raywa1cher.coursejournalbackend.rest.dto.groups.OnCreate;
import com.a6raywa1cher.coursejournalbackend.rest.dto.groups.OnPatch;
import com.a6raywa1cher.coursejournalbackend.rest.dto.groups.OnUpdate;
import com.a6raywa1cher.coursejournalbackend.service.EmployeeService;
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
@RequestMapping("/employees")
public class EmployeeController {
    private final EmployeeService employeeService;
    private final MapStructRestDtoMapper mapper;

    @Autowired
    public EmployeeController(EmployeeService employeeService, MapStructRestDtoMapper mapper) {
        this.employeeService = employeeService;
        this.mapper = mapper;
    }

    @GetMapping("/")
    public Page<EmployeeDto> getUserList(@ParameterObject Pageable page) {
        return employeeService.getPage(page);
    }

    @GetMapping("/{id}")
    public EmployeeDto getUserById(@PathVariable long id) {
        return employeeService.getById(id);
    }

    @PostMapping("/")
    @PreAuthorize("@accessChecker.createEmployeeAccess(authentication)")
    @ResponseStatus(HttpStatus.CREATED)
    @Validated(OnCreate.class)
    public EmployeeDto createEmployee(@RequestBody @Valid EmployeeRestDto dto) {
        return employeeService.create(mapper.map(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@accessChecker.editEmployeeAccess(#id, authentication)")
    @Validated(OnUpdate.class)
    public EmployeeDto updateEmployee(@RequestBody @Valid EmployeeRestDto dto, @PathVariable long id) {
        return employeeService.update(id, mapper.map(dto));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("@accessChecker.editEmployeeAccess(#id, authentication)")
    @Validated(OnPatch.class)
    public EmployeeDto patchEmployee(@RequestBody @Valid EmployeeRestDto dto, @PathVariable long id) {
        return employeeService.patch(id, mapper.map(dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@accessChecker.editEmployeeAccess(#id, authentication)")
    public void deleteEmployee(@PathVariable long id) {
        employeeService.delete(id);
    }
}
