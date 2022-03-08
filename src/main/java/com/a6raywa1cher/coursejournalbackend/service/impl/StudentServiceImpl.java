package com.a6raywa1cher.coursejournalbackend.service.impl;

import com.a6raywa1cher.coursejournalbackend.dto.StudentDto;
import com.a6raywa1cher.coursejournalbackend.dto.exc.NotFoundException;
import com.a6raywa1cher.coursejournalbackend.dto.exc.TransferNotAllowedException;
import com.a6raywa1cher.coursejournalbackend.dto.exc.VariousParentEntitiesException;
import com.a6raywa1cher.coursejournalbackend.dto.mapper.MapStructMapper;
import com.a6raywa1cher.coursejournalbackend.model.Course;
import com.a6raywa1cher.coursejournalbackend.model.Student;
import com.a6raywa1cher.coursejournalbackend.model.Task;
import com.a6raywa1cher.coursejournalbackend.model.repo.StudentRepository;
import com.a6raywa1cher.coursejournalbackend.service.CourseService;
import com.a6raywa1cher.coursejournalbackend.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@Transactional
public class StudentServiceImpl implements StudentService {
    private final StudentRepository repository;
    private final CourseService courseService;
    private final MapStructMapper mapper;

    @Autowired
    public StudentServiceImpl(StudentRepository repository, CourseService courseService, MapStructMapper mapper) {
        this.repository = repository;
        this.courseService = courseService;
        this.mapper = mapper;
    }

    @Override
    public StudentDto getById(long id) {
        return mapper.map(getStudentById(id));
    }

    @Override
    public Optional<Student> findRawById(long id) {
        return repository.findById(id);
    }

    @Override
    public Page<StudentDto> getByCourseId(long courseId, Pageable pageable) {
        return repository.getAllByCourse(getCourseById(courseId), pageable).map(mapper::map);
    }

    @Override
    public List<StudentDto> getByCourseId(long courseId) {
        return repository.getAllByCourse(getCourseById(courseId)).stream().map(mapper::map).toList();
    }

    @Override
    public StudentDto create(StudentDto dto) {
        Student student = new Student();
        Course course = getCourseById(dto.getCourse());

        mapper.put(dto, student);

        student.setCourse(course);
        student.setCreatedAt(LocalDateTime.now());
        student.setLastModifiedAt(LocalDateTime.now());

        return mapper.map(repository.save(student));
    }

    @Override
    public List<StudentDto> batchCreate(List<StudentDto> dtoList) {
        List<Student> students = new ArrayList<>();
        Course course = extractCourse(dtoList);

        LocalDateTime now = LocalDateTime.now();

        for (StudentDto dto : dtoList) {
            Student student = new Student();

            mapper.put(dto, student);

            student.setCourse(course);
            student.setCreatedAt(now);
            student.setLastModifiedAt(now);

            students.add(student);
        }

        return StreamSupport.stream(repository.saveAll(students).spliterator(), false)
                .map(mapper::map)
                .toList();
    }

    @Override
    public StudentDto update(long id, StudentDto dto) {
        Student student = getStudentById(id);
        Course course = getCourseById(dto.getCourse());

        assertNoCourseChange(student.getCourse(), course);
        mapper.put(dto, student);

        student.setCourse(course);
        student.setCreatedAt(LocalDateTime.now());
        student.setLastModifiedAt(LocalDateTime.now());

        return mapper.map(repository.save(student));
    }

    @Override
    public StudentDto patch(long id, StudentDto dto) {
        Student student = getStudentById(id);
        Course course = dto.getCourse() != null ? getCourseById(dto.getCourse()) : student.getCourse();

        assertNoCourseChange(student.getCourse(), course);
        mapper.patch(dto, student);

        student.setCourse(course);
        student.setCreatedAt(LocalDateTime.now());
        student.setLastModifiedAt(LocalDateTime.now());

        return mapper.map(repository.save(student));
    }

    @Override
    public void delete(long id) {
        Student student = getStudentById(id);
        repository.delete(student);
    }

    private Student getStudentById(long id) {
        return repository.findById(id).orElseThrow(() -> new NotFoundException(Student.class, id));
    }

    private Course getCourseById(long id) {
        return courseService.findRawById(id).orElseThrow(() -> new NotFoundException(Course.class, id));
    }


    private void assertNoCourseChange(Course left, Course right) {
        if (!Objects.equals(left, right)) {
            throw new TransferNotAllowedException(Task.class, "course", left.getId(), right.getId());
        }
    }

    private Course extractCourse(List<StudentDto> dtoList) {
        Set<Long> courseIds = dtoList.stream()
                .map(StudentDto::getCourse)
                .collect(Collectors.toSet());
        if (courseIds.size() != 1) {
            throw new VariousParentEntitiesException(new ArrayList<>(courseIds));
        }
        return getCourseById(courseIds.iterator().next());
    }
}
