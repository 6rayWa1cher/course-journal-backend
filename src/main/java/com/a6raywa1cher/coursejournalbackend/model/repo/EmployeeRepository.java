package com.a6raywa1cher.coursejournalbackend.model.repo;

import com.a6raywa1cher.coursejournalbackend.model.Employee;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmployeeRepository extends PagingAndSortingRepository<Employee, Long> {
    Optional<Employee> findByFirstNameAndMiddleNameAndLastNameAndDepartment(
            String firstName, String middleName, String lastName, String department
    );
}