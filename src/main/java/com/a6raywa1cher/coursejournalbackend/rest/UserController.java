package com.a6raywa1cher.coursejournalbackend.rest;

import com.a6raywa1cher.coursejournalbackend.dto.UserDto;
import com.a6raywa1cher.coursejournalbackend.rest.dto.CreateUserDto;
import com.a6raywa1cher.coursejournalbackend.rest.dto.EditUserDto;
import com.a6raywa1cher.coursejournalbackend.rest.dto.MapStructRestDtoMapper;
import com.a6raywa1cher.coursejournalbackend.rest.dto.groups.OnUpdate;
import com.a6raywa1cher.coursejournalbackend.service.UserService;
import com.a6raywa1cher.coursejournalbackend.validation.RegexLibrary;
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

@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;
    private final MapStructRestDtoMapper mapper;

    @Autowired
    public UserController(UserService userService, MapStructRestDtoMapper mapper) {
        this.userService = userService;
        this.mapper = mapper;
    }

    @GetMapping("/")
    public Page<UserDto> getUserList(@ParameterObject Pageable page) {
        return userService.getPage(page);
    }

    @GetMapping("/{id}")
    public UserDto getUserById(@PathVariable long id) {
        return userService.getById(id);
    }

    @GetMapping("/username/{username}")
    public UserDto findByUsername(@PathVariable @Pattern(regexp = RegexLibrary.USERNAME) @Valid String username) {
        return userService.getByUsername(username);
    }

    @PostMapping("/")
    @Secured("ROLE_ADMIN")
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto createUser(@RequestBody @Valid CreateUserDto dto) {
        return userService.createUser(mapper.map(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@accessChecker.editUserAccess(#id, #dto.userRole, authentication)")
    public UserDto updateUser(@RequestBody @Valid EditUserDto dto, @PathVariable long id) {
        return userService.updateUser(id, mapper.map(dto));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("@accessChecker.editUserAccess(#id, #dto.userRole, authentication)")
    @Validated(OnUpdate.class)
    public UserDto patchUser(@RequestBody @Valid EditUserDto dto, @PathVariable long id) {
        return userService.patchUser(id, mapper.map(dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@accessChecker.isUserModificationAuthorized(#id, authentication)")
    public void deleteUser(@PathVariable long id) {
        userService.delete(id);
    }
}
