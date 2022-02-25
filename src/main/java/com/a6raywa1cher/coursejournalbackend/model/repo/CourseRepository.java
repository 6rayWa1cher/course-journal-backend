package com.a6raywa1cher.coursejournalbackend.model.repo;

import com.a6raywa1cher.coursejournalbackend.model.Course;
import com.a6raywa1cher.coursejournalbackend.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CourseRepository extends PagingAndSortingRepository<Course, Long> {
    boolean existsByNameAndOwner(String name, User owner);

    @Query("from Course c where lower(c.name) like %:query%")
    Page<Course> findByNameContains(@Param("query") String query, Pageable pageable);

    Page<Course> findByOwner(User owner, Pageable pageable);

    @Query("from Course c where c.owner = :owner and lower(c.name) like %:name%")
    Page<Course> findByOwnerAndNameContains(@Param("owner") User owner, @Param("name") String name, Pageable pageable);
}