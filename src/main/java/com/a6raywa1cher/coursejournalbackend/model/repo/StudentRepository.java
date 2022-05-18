package com.a6raywa1cher.coursejournalbackend.model.repo;

import com.a6raywa1cher.coursejournalbackend.model.Course;
import com.a6raywa1cher.coursejournalbackend.model.Group;
import com.a6raywa1cher.coursejournalbackend.model.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudentRepository extends PagingAndSortingRepository<Student, Long> {
    @Query("select s from Student s join s.courses c where c = :course")
    Page<Student> getAllByCourse(@Param("course") Course course, Pageable pageable);

    @Query("select s from Student s join s.courses c where c = :course")
    List<Student> getAllByCourse(@Param("course") Course course, Sort sort);

    List<Student> getAllByGroup(Group group, Sort sort);
}