package com.a6raywa1cher.coursejournalbackend.model.repo;

import com.a6raywa1cher.coursejournalbackend.model.Course;
import com.a6raywa1cher.coursejournalbackend.model.Group;
import com.a6raywa1cher.coursejournalbackend.model.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudentRepository extends PagingAndSortingRepository<Student, Long> {
    Page<Student> getAllByCourse(Course course, Pageable pageable);

    List<Student> getAllByCourse(Course course);

    List<Student> getAllByGroup(Group group);
}