package com.a6raywa1cher.coursejournalbackend.model.repo;

import com.a6raywa1cher.coursejournalbackend.model.Course;
import com.a6raywa1cher.coursejournalbackend.model.Employee;
import com.a6raywa1cher.coursejournalbackend.model.Group;
import com.a6raywa1cher.coursejournalbackend.model.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseRepository extends PagingAndSortingRepository<Course, Long> {
    boolean existsByNameAndOwner(String name, Employee owner);

    @Query("from Course c where lower(c.name) like %:query%")
    Page<Course> findByNameContains(@Param("query") String query, Pageable pageable);

    Page<Course> findByOwner(Employee owner, Pageable pageable);

    @Query("from Course c where c.owner = :owner and lower(c.name) like %:name%")
    Page<Course> findByOwnerAndNameContains(@Param("owner") Employee owner, @Param("name") String name, Pageable pageable);

    @Query("select c.id from Course c where c.owner = :owner")
    List<Long> findByOwner(@Param("owner") Employee owner);

    @Query("select c from Course c, Student s where s.group = :group group by c.id")
    Page<Course> findAllByGroup(@Param("group") Group group, Pageable pageable);

    @Query("select c from Course c, Student s where s.group = :group group by c.id")
    List<Course> findAllByGroupWithoutPage(@Param("group") Group group);
}