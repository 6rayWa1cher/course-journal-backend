package com.a6raywa1cher.coursejournalbackend.rest;

import com.a6raywa1cher.coursejournalbackend.dto.EmployeeDto;
import com.a6raywa1cher.coursejournalbackend.rest.dto.CreateEmployeeDto;
import com.a6raywa1cher.coursejournalbackend.rest.dto.EditUserDto;
import com.a6raywa1cher.coursejournalbackend.rest.dto.MapStructRestDtoMapper;
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
    @PreAuthorize("@accessChecker.createUserAccess(#dto.userRole, authentication)")
    @ResponseStatus(HttpStatus.CREATED)
    public EmployeeDto createUser(@RequestBody @Valid CreateEmployeeDto dto) {
        return employeeService.createEmployee(mapper.map(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@accessChecker.editUserAccessWithRole(#id, #dto.userRole, authentication)")
    public EmployeeDto updateUser(@RequestBody @Valid EditUserDto dto, @PathVariable long id) {
        return employeeService.updateEmployee(id, mapper.map(dto));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("@accessChecker.editUserAccessWithRole(#id, #dto.userRole, authentication)")
    @Validated(OnUpdate.class)
    public EmployeeDto patchUser(@RequestBody @Valid EditUserDto dto, @PathVariable long id) {
        return employeeService.patchEmployee(id, mapper.map(dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@accessChecker.editUserAccess(#id, authentication)")
    public void deleteUser(@PathVariable long id) {
        employeeService.delete(id);
    }
}
