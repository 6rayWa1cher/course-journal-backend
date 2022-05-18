package com.a6raywa1cher.coursejournalbackend.model.repo;

import com.a6raywa1cher.coursejournalbackend.model.Course;
import com.a6raywa1cher.coursejournalbackend.model.Criteria;
import com.a6raywa1cher.coursejournalbackend.model.Task;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CriteriaRepository extends JpaRepository<Criteria, Long>, CustomCriteriaRepository {
    List<Criteria> getAllByTask(Task task);

    List<Criteria> getAllByTask(Task task, Sort sort);

    @Query("select c from Criteria c join c.task t where t.course = :course")
    List<Criteria> getAllByCourse(@Param("course") Course course);
}