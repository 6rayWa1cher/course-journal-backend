package com.a6raywa1cher.coursejournalbackend.model.repo;

import com.a6raywa1cher.coursejournalbackend.model.Attendance;
import com.a6raywa1cher.coursejournalbackend.model.Course;
import com.a6raywa1cher.coursejournalbackend.model.Student;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends PagingAndSortingRepository<Attendance, Long> {
    List<Attendance> getAllByStudent(@Param("student") Student student, Sort sort);

    List<Attendance> getAllByCourse(@Param("course") Course course, Sort sort);

    List<Attendance> getAllByStudentAndCourse(@Param("student") Student student, @Param("course") Course course, Sort sort);

    Optional<Attendance> findByStudentAndAttendedDateAndAttendedClass(Student student, LocalDate attendedDate, Integer attendedClass);
}
