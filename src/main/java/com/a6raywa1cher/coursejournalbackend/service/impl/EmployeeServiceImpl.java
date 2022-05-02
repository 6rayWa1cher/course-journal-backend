package com.a6raywa1cher.coursejournalbackend.service.impl;

import com.a6raywa1cher.coursejournalbackend.dto.EmployeeDto;
import com.a6raywa1cher.coursejournalbackend.dto.exc.ConflictException;
import com.a6raywa1cher.coursejournalbackend.dto.exc.NotFoundException;
import com.a6raywa1cher.coursejournalbackend.dto.mapper.MapStructMapper;
import com.a6raywa1cher.coursejournalbackend.model.Employee;
import com.a6raywa1cher.coursejournalbackend.model.repo.EmployeeRepository;
import com.a6raywa1cher.coursejournalbackend.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

import static com.a6raywa1cher.coursejournalbackend.utils.CommonUtils.coalesce;

@Service
@Transactional
public class EmployeeServiceImpl implements EmployeeService {
    private final EmployeeRepository repository;
    private final MapStructMapper mapper;

    @Autowired
    public EmployeeServiceImpl(EmployeeRepository repository, MapStructMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Page<EmployeeDto> getPage(Pageable pageable) {
        return repository.findAll(pageable).map(mapper::map);
    }

    @Override
    public EmployeeDto getById(long id) {
        return mapper.map($getById(id));
    }

    @Override
    public Optional<Employee> findRawById(long id) {
        return repository.findById(id);
    }

    @Override
    public EmployeeDto create(EmployeeDto dto) {
        Employee employee = new Employee();

        assertUniqueProperties(
                dto.getFirstName(), dto.getMiddleName(),
                dto.getLastName(), dto.getDepartment()
        );
        mapper.put(dto, employee);

        employee.setCreatedAt(LocalDateTime.now());
        employee.setLastModifiedAt(LocalDateTime.now());

        return mapper.map(repository.save(employee));
    }

    @Override
    public EmployeeDto update(long id, EmployeeDto dto) {
        Employee employee = $getById(id);

        assertUniquePropertiesOrNotChanged(
                employee.getFirstName(), employee.getMiddleName(),
                employee.getLastName(), employee.getDepartment(),
                dto.getFirstName(), dto.getMiddleName(),
                dto.getLastName(), dto.getDepartment()
        );
        mapper.put(dto, employee);

        employee.setLastModifiedAt(LocalDateTime.now());
        return mapper.map(repository.save(employee));
    }

    @Override
    public EmployeeDto patch(long id, EmployeeDto dto) {
        Employee employee = $getById(id);

        assertUniquePropertiesOrNotChanged(
                employee.getFirstName(),
                employee.getMiddleName(),
                employee.getLastName(),
                employee.getDepartment(),

                coalesce(dto.getFirstName(), employee.getFirstName()),
                coalesce(dto.getMiddleName(), employee.getMiddleName()),
                coalesce(dto.getLastName(), employee.getLastName()),
                coalesce(dto.getDepartment(), employee.getDepartment())
        );
        mapper.patch(dto, employee);

        employee.setLastModifiedAt(LocalDateTime.now());
        return mapper.map(repository.save(employee));
    }

    @Override
    public void delete(long id) {
        Employee employee = $getById(id);
        repository.delete(employee);
    }

    private Employee $getById(long id) {
        return repository.findById(id).orElseThrow(() -> new NotFoundException(Employee.class, id));
    }

    private void assertUniqueProperties(
            String firstName, String middleName,
            String lastName, String department
    ) {
        Optional<Employee> optional = repository.findByFirstNameAndMiddleNameAndLastNameAndDepartment(
                firstName, middleName, lastName, department
        );
        if (optional.isPresent()) {
            throw new ConflictException(
                    Employee.class,
                    "firstName", firstName, "middleName", middleName,
                    "lastName", lastName, "department", department
            );
        }
    }

    private void assertUniquePropertiesOrNotChanged(
            String prevFirstName, String prevMiddleName,
            String prevLastName, String prevDepartment,
            String newFirstName, String newMiddleName,
            String newLastName, String newDepartment
    ) {
        if (!Objects.equals(prevFirstName, newFirstName) ||
                !Objects.equals(prevMiddleName, newMiddleName) ||
                !Objects.equals(prevLastName, newLastName) ||
                !Objects.equals(prevDepartment, newDepartment)) {
            assertUniqueProperties(newFirstName, newMiddleName, newLastName, newDepartment);
        }
    }
}
