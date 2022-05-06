package com.a6raywa1cher.coursejournalbackend.model.repo;

import com.a6raywa1cher.coursejournalbackend.model.AuthUser;
import com.a6raywa1cher.coursejournalbackend.model.Employee;
import com.a6raywa1cher.coursejournalbackend.model.Student;
import com.a6raywa1cher.jsonrestsecurity.dao.repo.IUserRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@Primary
public interface AuthUserRepository extends PagingAndSortingRepository<AuthUser, Long>, IUserRepository<AuthUser> {
    Optional<AuthUser> findByStudent(Student student);

    Optional<AuthUser> findByEmployee(Employee employee);
}
