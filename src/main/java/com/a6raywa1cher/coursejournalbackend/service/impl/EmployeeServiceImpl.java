package com.a6raywa1cher.coursejournalbackend.service.impl;

import com.a6raywa1cher.coursejournalbackend.dto.CreateEditAuthUserDto;
import com.a6raywa1cher.coursejournalbackend.dto.EmployeeDto;
import com.a6raywa1cher.coursejournalbackend.dto.exc.ConflictException;
import com.a6raywa1cher.coursejournalbackend.dto.exc.NotFoundException;
import com.a6raywa1cher.coursejournalbackend.dto.mapper.MapStructMapper;
import com.a6raywa1cher.coursejournalbackend.model.Employee;
import com.a6raywa1cher.coursejournalbackend.model.repo.EmployeeRepository;
import com.a6raywa1cher.coursejournalbackend.service.AuthUserService;
import com.a6raywa1cher.coursejournalbackend.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;

import static com.a6raywa1cher.coursejournalbackend.utils.CommonUtils.coalesce;

@Service
@Transactional
public class EmployeeServiceImpl implements EmployeeService {
    private final EmployeeRepository employeeRepository;
    private final MapStructMapper mapper;
    private final AuthUserService authUserService;

    @Autowired
    public EmployeeServiceImpl(EmployeeRepository employeeRepository, MapStructMapper mapper,
                               AuthUserService authUserService) {
        this.employeeRepository = employeeRepository;
        this.mapper = mapper;
        this.authUserService = authUserService;
    }

    @Override
    public Page<EmployeeDto> getPage(Pageable pageable) {
        return employeeRepository.findAll(pageable).map(mapper::map);
    }

    @Override
    public EmployeeDto getById(long id) {
        return mapper.map($getById(id));
    }

    @Override
    public Optional<Employee> findRawById(long id) {
        return employeeRepository.findById(id);
    }

    @Override
    public EmployeeDto createEmployee(EmployeeDto dto) {
        Employee employee = new Employee();

        assertUsernameAvailable(dto.getUsername());

        mapper.put(dto, employee);

        employee.setRefreshTokens(new ArrayList<>());
        employee.setCreatedAt(LocalDateTime.now());
        employee.setLastModifiedAt(LocalDateTime.now());

        return mapper.map(employeeRepository.save(employee));
    }

    @Override
    public EmployeeDto updateEmployee(long id, CreateEditAuthUserDto dto) {
        Employee employee = $getById(id);

        assertUsernameNotChangedOrAvailable(employee.getUsername(), dto.getUsername());

        mapper.put(dto, employee);

        employee.setLastModifiedAt(LocalDateTime.now());
        return mapper.map(employeeRepository.save(employee));
    }

    @Override
    public EmployeeDto patchEmployee(long id, CreateEditAuthUserDto dto) {
        Employee employee = $getById(id);

        assertUsernameNotChangedOrAvailable(
                employee.getUsername(), coalesce(dto.getUsername(), employee.getUsername())
        );

        mapper.patch(dto, employee);
        employee.setLastModifiedAt(LocalDateTime.now());
        return mapper.map(employeeRepository.save(employee));
    }

    @Override
    public void delete(long id) {
        Employee employee = $getById(id);
        employeeRepository.delete(employee);
    }

    private Employee $getById(long id) {
        return employeeRepository.findById(id).orElseThrow(() -> new NotFoundException(Employee.class, id));
    }

    private void assertUsernameNotChangedOrAvailable(String before, String now) {
        if (!Objects.equals(before, now)) {
            assertUsernameAvailable(now);
        }
    }

    private void assertUsernameAvailable(String username) {
        if (employeeRepository.existsByUsername(username)) {
            throw new ConflictException(Employee.class, "username", username);
        }
    }
}
