package com.a6raywa1cher.coursejournalbackend.service.impl;

import com.a6raywa1cher.coursejournalbackend.dto.AttendanceDto;
import com.a6raywa1cher.coursejournalbackend.dto.StudentDto;
import com.a6raywa1cher.coursejournalbackend.dto.exc.ConflictException;
import com.a6raywa1cher.coursejournalbackend.dto.exc.NotFoundException;
import com.a6raywa1cher.coursejournalbackend.dto.exc.TransferNotAllowedException;
import com.a6raywa1cher.coursejournalbackend.dto.exc.VariousParentEntitiesException;
import com.a6raywa1cher.coursejournalbackend.dto.mapper.MapStructMapper;
import com.a6raywa1cher.coursejournalbackend.model.Attendance;
import com.a6raywa1cher.coursejournalbackend.model.Course;
import com.a6raywa1cher.coursejournalbackend.model.Student;
import com.a6raywa1cher.coursejournalbackend.model.repo.AttendanceRepository;
import com.a6raywa1cher.coursejournalbackend.service.*;
import com.a6raywa1cher.coursejournalbackend.utils.CommonUtils;
import org.checkerframework.checker.units.qual.A;
import org.checkerframework.checker.units.qual.C;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class AttendanceServiceImpl implements AttendanceService {
    private final AttendanceRepository repository;

    private final MapStructMapper mapper;

    private final StudentService studentService;

    private final CourseService courseService;

    public AttendanceServiceImpl(AttendanceRepository attendanceRepository, MapStructMapper mapper, StudentService studentService, CourseService courseService) {
        this.repository = attendanceRepository;
        this.mapper = mapper;
        this.studentService = studentService;
        this.courseService = courseService;
    }

    @Override
    public AttendanceDto getById(long id) {
        return mapper.map(getAttendanceById(id));
    }

    @Override
    public Optional<Attendance> findRawById(long id) {
        return repository.findById(id);
    }

    @Override
    public List<AttendanceDto> getByStudentId(long studentId, Sort sort) {
        Student student = getStudentById(studentId);
        return repository.getAllByStudent(student, sort).stream()
                .map(mapper::map)
                .toList();
    }

    @Override
    public List<AttendanceDto> getByCourseId(long courseId, Sort sort) {
        Course course = getCourseById(courseId);
        return repository.getAllByCourse(course, sort).stream()
                .map(mapper::map)
                .toList();
    }

    @Override
    public List<AttendanceDto> getByStudentAndCourseIds(long studentId, long courseId, Sort sort) {
        Student student = getStudentById(studentId);
        Course course = getCourseById(courseId);
        return repository.getAllByStudentAndCourse(student, course, sort).stream()
                .map(mapper::map)
                .toList();
    }

    @Override
    public AttendanceDto create(AttendanceDto dto) {
        Course course = getCourseById(dto.getCourse());

        return mapper.map(repository.save(getAttendanceByDtoAndCourse(dto, course)));
    }

    @Override
    public List<AttendanceDto> batchCreate(List<AttendanceDto> dtoList) {
        List<Attendance> attendances = new ArrayList<>();
        Course course = extractCourse(dtoList);

        for (AttendanceDto dto : dtoList) {
            attendances.add(getAttendanceByDtoAndCourse(dto, course));
        }

        return StreamSupport.stream(repository.saveAll(attendances).spliterator(), false)
                .map(mapper::map)
                .toList();
    }

    @Override
    public AttendanceDto update(long id, AttendanceDto dto) {
        Attendance attendance = getAttendanceById(id);
        Student student = getStudentById(dto.getStudent());
        Course course = getCourseById(dto.getCourse());
        LocalDate attendedDate = dto.getAttendedDate();
        Integer attendedClass = dto.getAttendedClass();

        assertNoStudentChanged(attendance.getStudent(), student);
        assertNoCourseChanged(attendance.getCourse(), course);
        assertNoAttendanceClassChanged(attendance.getAttendedClass(), attendedClass);
        assertNoAttendanceDateChanged(attendance.getAttendedDate(), attendedDate);
        mapper.put(dto, attendance);

        attendance.setLastModifiedAt(LocalDateTime.now());

        return mapper.map(repository.save(attendance));
    }

    @Override
    public AttendanceDto patch(long id, AttendanceDto dto) {
        Attendance attendance = getAttendanceById(id);
        Student student = dto.getStudent() != null ? getStudentById(dto.getStudent()) : attendance.getStudent();
        Course course = dto.getCourse() != null ? getCourseById(dto.getCourse()) : attendance.getCourse();
        LocalDate attendedDate = dto.getAttendedDate();
        Integer attendedClass = dto.getAttendedClass();

        assertNoStudentChanged(attendance.getStudent(), student);
        assertNoCourseChanged(attendance.getCourse(), course);
        assertNoAttendanceClassChanged(attendance.getAttendedClass(), CommonUtils.coalesce(attendedClass, attendance.getAttendedClass()));
        assertNoAttendanceDateChanged(attendance.getAttendedDate(), CommonUtils.coalesce(attendedDate, attendance.getAttendedDate()));
        mapper.patch(dto, attendance);

        attendance.setLastModifiedAt(LocalDateTime.now());

        return mapper.map(repository.save(attendance));
    }

    @Override
    public void delete(long id) {
        Attendance attendance = getAttendanceById(id);
        repository.delete(attendance);
    }

    private Attendance getAttendanceById(long id) {
        return repository.findById(id).orElseThrow(() -> new NotFoundException(Attendance.class, id));
    }

    private Student getStudentById(long id) {
        return studentService.findRawById(id).orElseThrow(() -> new NotFoundException(Student.class, id));
    }

    private Course getCourseById(long id) {
        return courseService.findRawById(id).orElseThrow(() -> new NotFoundException(Course.class, id));
    }

    private void assertUniqueByStudentAttendedDateAndAttendedClass(Student student, LocalDate date, Integer attendedClass) {
        if (repository.findByStudentAndAttendedDateAndAttendedClass(student, date, attendedClass).isPresent()) {
            throw new ConflictException(Attendance.class,
                    "student", Long.toString(student.getId()),
                    "date", date.toString(),
                    "attendedClass", Integer.toString(attendedClass));
        }
    }

    private void assertNoCourseChanged(Course oldCourse, Course newCourse) {
        if (!Objects.equals(oldCourse, newCourse)) {
            throw new TransferNotAllowedException(Course.class, "course", oldCourse.getId(), newCourse.getId());
        }
    }

    private void assertNoStudentChanged(Student oldStudent, Student newStudent) {
        if (!Objects.equals(oldStudent, newStudent)) {
            throw new TransferNotAllowedException(Attendance.class, "attendance", oldStudent.getId(), newStudent.getId());
        }
    }

    private void assertNoAttendanceDateChanged(LocalDate oldDate, LocalDate newDate) {
        if (!Objects.equals(oldDate, newDate)) {
            throw new TransferNotAllowedException(Attendance.class, "attendance", oldDate.toString(), newDate.toString());
        }
    }

    private void assertNoAttendanceClassChanged(Integer oldClass, Integer newClass) {
        if (!Objects.equals(oldClass, newClass)) {
            throw new TransferNotAllowedException(Attendance.class, "attendance", oldClass.toString(), newClass.toString());
        }
    }

    private Course extractCourse(List<AttendanceDto> dtoList) {
        Set<Long> courseIds = dtoList.stream()
                .map(AttendanceDto::getCourse)
                .collect(Collectors.toSet());
        if (courseIds.size() != 1) {
            throw new VariousParentEntitiesException(new ArrayList<>(courseIds));
        }
        return getCourseById(courseIds.iterator().next());
    }

    private Attendance getAttendanceByDtoAndCourse(AttendanceDto dto, Course course) {
        Attendance attendance = new Attendance();
        Student student = getStudentById(dto.getStudent());
        Integer attendedClass = dto.getAttendedClass();
        LocalDate attendedDate = dto.getAttendedDate();
        LocalDateTime createAndModifyDateTime = LocalDateTime.now();

        assertUniqueByStudentAttendedDateAndAttendedClass(student, attendedDate, attendedClass);
        mapper.put(dto, attendance);

        attendance.setStudent(student);
        attendance.setCourse(course);
        attendance.setAttendedClass(attendedClass);
        attendance.setAttendedDate(attendedDate);
        attendance.setCreatedAt(createAndModifyDateTime);
        attendance.setLastModifiedAt(createAndModifyDateTime);

        return attendance;
    };
}
