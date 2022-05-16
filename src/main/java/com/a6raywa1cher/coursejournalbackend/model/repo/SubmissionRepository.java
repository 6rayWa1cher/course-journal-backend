package com.a6raywa1cher.coursejournalbackend.model.repo;

import com.a6raywa1cher.coursejournalbackend.model.Course;
import com.a6raywa1cher.coursejournalbackend.model.Student;
import com.a6raywa1cher.coursejournalbackend.model.Submission;
import com.a6raywa1cher.coursejournalbackend.model.Task;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubmissionRepository extends CrudRepository<Submission, Long> {
    @Query("from Submission where student = :student and task.course = :course")
    List<Submission> getAllByStudentAndCourse(@Param("student") Student student, @Param("course") Course course);

    @Query("from Submission where task.course = :course")
    List<Submission> getAllByCourse(@Param("course") Course course);

    List<Submission> getAllByTask(Task task);

    Optional<Submission> findByTaskAndStudent(Task task, Student student);
}
