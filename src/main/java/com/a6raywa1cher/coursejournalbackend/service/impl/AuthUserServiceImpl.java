package com.a6raywa1cher.coursejournalbackend.service.impl;

import com.a6raywa1cher.coursejournalbackend.dto.AuthUserDto;
import com.a6raywa1cher.coursejournalbackend.dto.CreateEditAuthUserDto;
import com.a6raywa1cher.coursejournalbackend.dto.exc.*;
import com.a6raywa1cher.coursejournalbackend.dto.mapper.MapStructMapper;
import com.a6raywa1cher.coursejournalbackend.model.*;
import com.a6raywa1cher.coursejournalbackend.model.repo.AuthUserRepository;
import com.a6raywa1cher.coursejournalbackend.service.AuthUserService;
import com.a6raywa1cher.coursejournalbackend.service.EmployeeService;
import com.a6raywa1cher.coursejournalbackend.service.StudentService;
import com.a6raywa1cher.coursejournalbackend.utils.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;

import static com.a6raywa1cher.coursejournalbackend.utils.CommonUtils.coalesce;

@Service
public class AuthUserServiceImpl implements AuthUserService {
    private final AuthUserRepository repository;
    private final MapStructMapper mapper;
    private final StudentService studentService;
    private final EmployeeService employeeService;

    @Autowired
    public AuthUserServiceImpl(AuthUserRepository repository, MapStructMapper mapper, StudentService studentService, EmployeeService employeeService) {
        this.repository = repository;
        this.mapper = mapper;
        this.studentService = studentService;
        this.employeeService = employeeService;
    }


    @Override
    public AuthUserDto getById(long id) {
        return mapper.map(getAuthUserById(id));
    }

    @Override
    public Page<AuthUserDto> getPage(Pageable pageable) {
        return repository.findAll(pageable).map(mapper::map);
    }

    @Override
    public Optional<AuthUser> findRawById(long id) {
        return repository.findById(id);
    }

    @Override
    public AuthUserDto getByEmployeeId(long id) {
        Employee employee = getEmployeeById(id);
        return repository.findByEmployee(employee).map(mapper::map).orElseThrow(
                () -> new NotFoundException(AuthUser.class, "employee", EntityUtils.getId(employee))
        );
    }

    @Override
    public AuthUserDto getByStudentId(long id) {
        Student student = getStudentById(id);
        return repository.findByStudent(student).map(mapper::map).orElseThrow(
                () -> new NotFoundException(AuthUser.class, "student", EntityUtils.getId(student))
        );
    }

    @Override
    public AuthUserDto create(CreateEditAuthUserDto dto) {
        AuthUser authUser = new AuthUser();
        Employee employee = dto.getEmployee() != null ? getEmployeeById(dto.getEmployee()) : null;
        Student student = dto.getStudent() != null ? getStudentById(dto.getStudent()) : null;
        LocalDateTime now = LocalDateTime.now();

        IdEntity<Long> target = getTarget(employee, student);
        assertValidTarget(dto.getUserRole(), target);
        assertUniqueTarget(target);
        mapper.put(dto, authUser);

        authUser.setEmployee(employee);
        authUser.setStudent(student);
        authUser.setCreatedAt(now);
        authUser.setLastModifiedAt(now);
        authUser.setRefreshTokens(new ArrayList<>());
        return mapper.map(repository.save(authUser));
    }

    @Override
    public AuthUserDto update(long id, CreateEditAuthUserDto dto) {
        AuthUser authUser = getAuthUserById(id);
        Employee employee = dto.getEmployee() != null ? getEmployeeById(dto.getEmployee()) : null;
        Student student = dto.getStudent() != null ? getStudentById(dto.getStudent()) : null;
        LocalDateTime now = LocalDateTime.now();

        IdEntity<Long> prevTarget = getTarget(authUser.getEmployee(), authUser.getStudent());
        IdEntity<Long> newTarget = getTarget(employee, student);
        assertTargetNotChanged(prevTarget, newTarget);
        assertUserRoleNotChanged(authUser.getUserRole(), dto.getUserRole());
        mapper.put(dto, authUser);

        authUser.setEmployee(employee);
        authUser.setStudent(student);
        authUser.setLastModifiedAt(now);
        return mapper.map(repository.save(authUser));
    }

    @Override
    public AuthUserDto patch(long id, CreateEditAuthUserDto dto) {
        AuthUser authUser = getAuthUserById(id);
        Employee employee = dto.getEmployee() != null ? getEmployeeById(dto.getEmployee()) : null;
        Student student = dto.getStudent() != null ? getStudentById(dto.getStudent()) : null;
        LocalDateTime now = LocalDateTime.now();

        IdEntity<Long> prevTarget = getTarget(authUser.getEmployee(), authUser.getStudent());
        IdEntity<Long> newTarget = coalesce(getTarget(employee, student), prevTarget);
        assertTargetNotChanged(prevTarget, newTarget);
        if (dto.getUserRole() != null) assertUserRoleNotChanged(authUser.getUserRole(), dto.getUserRole());
        mapper.patch(dto, authUser);

        authUser.setEmployee(employee);
        authUser.setStudent(student);
        authUser.setLastModifiedAt(now);
        return mapper.map(repository.save(authUser));
    }

    @Override
    public void delete(long id) {
        AuthUser authUser = getAuthUserById(id);
        repository.delete(authUser);
    }

    private AuthUser getAuthUserById(long id) {
        return repository.findById(id).orElseThrow(() -> new NotFoundException(AuthUser.class, id));
    }

    private Employee getEmployeeById(long id) {
        return employeeService.findRawById(id).orElseThrow(() -> new NotFoundException(Employee.class, id));
    }

    private Student getStudentById(long id) {
        return studentService.findRawById(id).orElseThrow(() -> new NotFoundException(Student.class, id));
    }

    private IdEntity<Long> getTarget(Employee employee, Student student) {
        if (employee != null && student != null) {
            throw new MultipleTargetsException(employee, student);
        }
        return employee != null ? employee : student;
    }

    private void assertTargetNotChanged(IdEntity<Long> prevTarget, IdEntity<Long> newTarget) {
        if (!Objects.equals(prevTarget, newTarget)) {
            throw new TransferNotAllowedException(AuthUser.class, "target", prevTarget, newTarget);
        }
    }

    private void assertValidTarget(UserRole userRole, IdEntity<Long> target) {
        switch (userRole) {
            case ADMIN -> {
                if (target != null) {
                    throw new IncorrectTargetOnUserRoleException(
                            UserRole.ADMIN, target, "admin can't have a target"
                    );
                }
            }
            case TEACHER -> {
                if (target instanceof Student) {
                    throw new IncorrectTargetOnUserRoleException(
                            UserRole.TEACHER, target, "teacher can't have a student as a target"
                    );
                }
                if (target == null) {
                    throw new IncorrectTargetOnUserRoleException(
                            UserRole.TEACHER, null, "teacher has to have an employee as a target"
                    );
                }
            }
            case HEADMAN -> {
                if (target instanceof Employee) {
                    throw new IncorrectTargetOnUserRoleException(
                            UserRole.HEADMAN, target, "headman can't have an employee as a target"
                    );
                }
                if (target == null) {
                    throw new IncorrectTargetOnUserRoleException(
                            UserRole.HEADMAN, null, "headman has to have a student as a target"
                    );
                }
            }
            default -> throw new IllegalArgumentException("Unknown role %s".formatted(userRole));
        }
    }

    private void assertUniqueTarget(IdEntity<Long> target) {
        if (target instanceof Student student && repository.findByStudent(student).isPresent()) {
            throw new ConflictException(AuthUser.class, "student", EntityUtils.getId(student));
        }
        if (target instanceof Employee employee && repository.findByEmployee(employee).isPresent()) {
            throw new ConflictException(AuthUser.class, "employee", EntityUtils.getId(employee));
        }
    }

    private void assertUserRoleNotChanged(UserRole prevUserRole, UserRole newUserRole) {
        if (!Objects.equals(prevUserRole, newUserRole)) {
            throw new TransferNotAllowedException(AuthUser.class, "userRole", prevUserRole.name(), newUserRole.name());
        }
    }
}
