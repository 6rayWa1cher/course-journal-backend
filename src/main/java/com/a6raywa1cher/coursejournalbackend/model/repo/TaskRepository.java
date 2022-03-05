package com.a6raywa1cher.coursejournalbackend.model.repo;

import com.a6raywa1cher.coursejournalbackend.model.Course;
import com.a6raywa1cher.coursejournalbackend.model.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends PagingAndSortingRepository<Task, Long> {
    List<Task> getAllByCourse(Course course);

    Page<Task> getAllByCourse(Course course, Pageable pageable);

    @Query("select coalesce(max(t.taskNumber) + 1, 1) from Task t where t.course = :course")
    int getNextNumber(@Param("course") Course course);
}