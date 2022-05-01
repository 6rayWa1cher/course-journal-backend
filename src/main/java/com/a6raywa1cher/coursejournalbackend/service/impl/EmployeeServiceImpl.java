package com.a6raywa1cher.coursejournalbackend.service.impl;

import com.a6raywa1cher.coursejournalbackend.dto.EmployeeDto;
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
import java.util.Optional;

@Service
@Transactional
public class EmployeeServiceImpl implements EmployeeService {
    private final EmployeeRepository employeeRepository;
    private final MapStructMapper mapper;

    @Autowired
    public EmployeeServiceImpl(EmployeeRepository employeeRepository, MapStructMapper mapper) {
        this.employeeRepository = employeeRepository;
        this.mapper = mapper;
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
    public EmployeeDto create(EmployeeDto dto) {
        Employee employee = new Employee();

        mapper.put(dto, employee);

        employee.setCreatedAt(LocalDateTime.now());
        employee.setLastModifiedAt(LocalDateTime.now());

        return mapper.map(employeeRepository.save(employee));
    }

    @Override
    public EmployeeDto update(long id, EmployeeDto dto) {
        Employee employee = $getById(id);

        mapper.put(dto, employee);

        employee.setLastModifiedAt(LocalDateTime.now());
        return mapper.map(employeeRepository.save(employee));
    }

    @Override
    public EmployeeDto patch(long id, EmployeeDto dto) {
        Employee employee = $getById(id);

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
}
