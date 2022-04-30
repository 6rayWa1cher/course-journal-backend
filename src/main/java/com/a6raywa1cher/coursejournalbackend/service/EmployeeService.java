package com.a6raywa1cher.coursejournalbackend.service;


import com.a6raywa1cher.coursejournalbackend.dto.EmployeeDto;
import com.a6raywa1cher.coursejournalbackend.model.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface EmployeeService {
    Page<EmployeeDto> getPage(Pageable pageable);

    EmployeeDto getById(long id);

    Optional<Employee> findRawById(long id);

    EmployeeDto createEmployee(EmployeeDto dto);

    EmployeeDto updateEmployee(long id, EmployeeDto dto);

    EmployeeDto patchEmployee(long id, EmployeeDto dto);

    void delete(long id);
}
