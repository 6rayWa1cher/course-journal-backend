package com.a6raywa1cher.coursejournalbackend.model.repo;

import com.a6raywa1cher.coursejournalbackend.model.Attendance;
import com.a6raywa1cher.coursejournalbackend.model.Course;
import com.a6raywa1cher.coursejournalbackend.model.Group;
import com.a6raywa1cher.coursejournalbackend.model.Student;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    List<Attendance> getAllByCourse(Course course, Sort sort);

    List<Attendance> getAllByStudentAndCourse(Student student, Course course, Sort sort);

    Optional<Attendance> findByStudentAndAttendedDateAndAttendedClass(Student student,
                                                                      LocalDate attendedDate,
                                                                      Integer attendedClass
    );

    List<Attendance> getAllByCourseAndAttendedDateBetween(Course course, LocalDate start, LocalDate end, Sort sort);

    List<Attendance> getAllByCourseAndStudentGroupAndAttendedDateBetween(Course course, Group group, LocalDate start, LocalDate end, Sort sort);

    @Query("""
            select a1 from Attendance a1, Attendance a2 where 
            a1.attendedClass = a2.attendedClass and 
            a1.attendedDate = a2.attendedDate and 
            a1.attendedDate between :fromDate and :toDate and 
            a1.student.id in (select s.id from Student s join s.courses c where c = :course) and 
            a1.course <> :course group by a1.id
            """)
    List<Attendance> getAllConflictsByCourseAndDatePeriod(Course course,
                                                          LocalDate fromDate,
                                                          LocalDate toDate,
                                                          Sort sort);


    @Query("""
            select a1 from Attendance a1, Attendance a2 where 
            a1.attendedClass = a2.attendedClass and 
            a1.attendedDate = a2.attendedDate and 
            a1.attendedDate between :fromDate and :toDate and 
            a1.student.id in (select s.id from Student s join s.courses c where c = :course) and 
            a1.student.group = :group and 
            a1.course <> :course group by a1.id
            """)
    List<Attendance> getAllConflictsByCourseAndGroupAndDatePeriod(Course course,
                                                          Group group,
                                                          LocalDate fromDate,
                                                          LocalDate toDate,
                                                          Sort sort);

}
