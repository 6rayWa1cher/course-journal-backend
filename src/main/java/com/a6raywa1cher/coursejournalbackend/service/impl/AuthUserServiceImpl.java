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


enum UserInfoType {
    STUDENT, EMPLOYEE
}

record UserInfoLink(UserInfoType type, IdEntity<Long> entity) {

}

@Service
public class AuthUserServiceImpl implements AuthUserService {
    private final AuthUserRepository repository;
    private final MapStructMapper mapper;
    private final StudentService studentService;
    private final EmployeeService employeeService;

    @Autowired
    public AuthUserServiceImpl(AuthUserRepository repository, MapStructMapper mapper,
                               StudentService studentService, EmployeeService employeeService) {
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
        UserInfoLink userInfoLink = getTargetFromId(dto.getUserRole(), dto.getUserInfo());
        LocalDateTime now = LocalDateTime.now();

        assertUniqueTarget(userInfoLink);
        assertUniqueUsername(dto.getUsername());
        mapper.put(dto, authUser);

        setTarget(userInfoLink, authUser);
        authUser.setCreatedAt(now);
        authUser.setLastModifiedAt(now);
        authUser.setRefreshTokens(new ArrayList<>());
        return mapper.map(repository.save(authUser));
    }

    @Override
    public AuthUserDto update(long id, CreateEditAuthUserDto dto) {
        AuthUser authUser = getAuthUserById(id);
        LocalDateTime now = LocalDateTime.now();

        UserInfoLink prevUserInfoLink = getTargetFromEntity(authUser.getEmployee(), authUser.getStudent());
        UserInfoLink newUserInfoLink = getTargetFromId(dto.getUserRole(), dto.getUserInfo());
        assertTargetNotChanged(prevUserInfoLink, newUserInfoLink);
        assertUserRoleNotChanged(authUser.getUserRole(), dto.getUserRole());
        assertUniqueUsernameOrNotChanged(authUser.getUsername(), dto.getUsername());
        mapper.put(dto, authUser);

        authUser.setLastModifiedAt(now);
        return mapper.map(repository.save(authUser));
    }

    @Override
    public AuthUserDto patch(long id, CreateEditAuthUserDto dto) {
        AuthUser authUser = getAuthUserById(id);
        LocalDateTime now = LocalDateTime.now();

        UserInfoLink prevUserInfoLink = getTargetFromEntity(authUser.getEmployee(), authUser.getStudent());
        UserInfoLink newUserInfoLink = dto.getUserInfo() != null ?
                getTargetFromId(
                        coalesce(dto.getUserRole(), authUser.getUserRole()),
                        dto.getUserInfo()
                ) :
                prevUserInfoLink;
        assertTargetNotChanged(prevUserInfoLink, newUserInfoLink);
        if (dto.getUserRole() != null) {
            assertUserRoleNotChanged(authUser.getUserRole(), dto.getUserRole());
        }
        if (dto.getUsername() != null) {
            assertUniqueUsernameOrNotChanged(authUser.getUsername(), dto.getUsername());
        }
        mapper.patch(dto, authUser);

        authUser.setLastModifiedAt(now);
        return mapper.map(repository.save(authUser));
    }

    @Override
    public void delete(long id) {
        AuthUser authUser = getAuthUserById(id);
        if (authUser.getEmployee() != null) {
            authUser.getEmployee().setAuthUser(null);
        }
        if (authUser.getStudent() != null) {
            authUser.getStudent().setAuthUser(null);
        }
        repository.delete(authUser);
    }

    private AuthUser getAuthUserById(long id) {
        return repository.findById(id).orElseThrow(() -> new NotFoundException(AuthUser.class, id));
    }

    private Employee getEmployeeById(Long id) {
        if (id == null) throw new NoDataPresentedException(AuthUser.class, "userInfo");
        return employeeService.findRawById(id)
                .orElseThrow(() -> new NotFoundException(Employee.class, id));
    }

    private Student getStudentById(Long id) {
        if (id == null) throw new NoDataPresentedException(AuthUser.class, "userInfo");
        return studentService.findRawById(id)
                .orElseThrow(() -> new NotFoundException(Student.class, id));
    }

    private UserInfoLink getTargetFromEntity(Employee employee, Student student) {
        if (employee != null && student != null) {
            throw new MultipleTargetsException(employee, student);
        } else if (employee == null && student == null) {
            return null;
        }
        return employee != null ? new UserInfoLink(UserInfoType.EMPLOYEE, employee) : new UserInfoLink(UserInfoType.STUDENT, student);
    }

    private UserInfoLink getTargetFromId(UserRole userRole, Long id) {
        return switch (userRole) {
            case TEACHER -> new UserInfoLink(UserInfoType.EMPLOYEE, getEmployeeById(id));
            case HEADMAN -> new UserInfoLink(UserInfoType.STUDENT, getStudentById(id));
            default -> {
                if (id != null) {
                    throw new IncorrectTargetOnUserRoleException(userRole, Long.toString(id), "target must be null");
                }
                yield null;
            }
        };
    }

    private void setTarget(UserInfoLink userInfoLink, AuthUser authUser) {
        if (userInfoLink == null) return;
        switch (userInfoLink.type()) {
            case STUDENT -> authUser.setStudent((Student) userInfoLink.entity());
            case EMPLOYEE -> authUser.setEmployee((Employee) userInfoLink.entity());
            default -> throw new IllegalArgumentException("Unknown target type %s".formatted(userInfoLink.type()));
        }
    }

    private void assertTargetNotChanged(UserInfoLink prevUserInfoLink, UserInfoLink newUserInfoLink) {
        if (!Objects.equals(prevUserInfoLink, newUserInfoLink)) {
            throw new TransferNotAllowedException(AuthUser.class, "target", Objects.toString(prevUserInfoLink), Objects.toString(newUserInfoLink));
        }
    }

    private void assertUniqueTarget(UserInfoLink userInfoLink) {
        if (userInfoLink == null) return;
        IdEntity<Long> entity = userInfoLink.entity();
        if (userInfoLink.type() == UserInfoType.STUDENT && repository.findByStudent((Student) entity).isPresent()) {
            throw new ConflictException(AuthUser.class, "student", EntityUtils.getId(entity));
        }
        if (userInfoLink.type() == UserInfoType.EMPLOYEE && repository.findByEmployee((Employee) entity).isPresent()) {
            throw new ConflictException(AuthUser.class, "employee", EntityUtils.getId(entity));
        }
    }

    private void assertUserRoleNotChanged(UserRole prevUserRole, UserRole newUserRole) {
        if (!Objects.equals(prevUserRole, newUserRole)) {
            throw new TransferNotAllowedException(AuthUser.class, "userRole", prevUserRole.name(), newUserRole.name());
        }
    }

    private void assertUniqueUsername(String username) {
        if (repository.findByUsername(username).isPresent()) {
            throw new ConflictException(AuthUser.class, "username", username);
        }
    }

    private void assertUniqueUsernameOrNotChanged(String prevUsername, String newUsername) {
        if (!Objects.equals(prevUsername, newUsername)) {
            assertUniqueUsername(newUsername);
        }
    }
}
