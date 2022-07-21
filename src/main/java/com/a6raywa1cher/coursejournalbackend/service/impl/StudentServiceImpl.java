package com.a6raywa1cher.coursejournalbackend.service.impl;

import com.a6raywa1cher.coursejournalbackend.dto.StudentDto;
import com.a6raywa1cher.coursejournalbackend.dto.exc.NotFoundException;
import com.a6raywa1cher.coursejournalbackend.dto.exc.VariousParentEntitiesException;
import com.a6raywa1cher.coursejournalbackend.dto.mapper.MapStructMapper;
import com.a6raywa1cher.coursejournalbackend.model.Course;
import com.a6raywa1cher.coursejournalbackend.model.Group;
import com.a6raywa1cher.coursejournalbackend.model.Student;
import com.a6raywa1cher.coursejournalbackend.model.repo.StudentRepository;
import com.a6raywa1cher.coursejournalbackend.service.CourseService;
import com.a6raywa1cher.coursejournalbackend.service.GroupService;
import com.a6raywa1cher.coursejournalbackend.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@Transactional
public class StudentServiceImpl implements StudentService {
    private final StudentRepository repository;
    private final GroupService groupService;
    private final MapStructMapper mapper;
    private CourseService courseService;

    @Autowired
    public StudentServiceImpl(StudentRepository repository, GroupService groupService, MapStructMapper mapper) {
        this.repository = repository;
        this.groupService = groupService;
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
    public List<Student> findRawById(List<Long> ids) {
        return StreamSupport.stream(repository.findAllById(ids).spliterator(), false).toList();
    }

    @Override
    public Page<StudentDto> getByCourseId(long courseId, Pageable pageable) {
        return repository.getAllByCourse(getCourseById(courseId), pageable).map(mapper::map);
    }

    @Override
    public List<StudentDto> getByCourseId(long courseId, Sort sort) {
        return repository.getAllByCourse(getCourseById(courseId), sort).stream().map(mapper::map).toList();
    }

    @Override
    public List<Student> getRawByCourseId(long courseId) {
        Course course = getCourseById(courseId);
        return repository.getAllByCourse(course, Sort.unsorted());
    }

    @Override
    public List<StudentDto> getByGroupId(long groupId, Sort sort) {
        return repository.getAllByGroup(getGroupById(groupId), sort).stream().map(mapper::map).toList();
    }

    @Override
    public StudentDto create(StudentDto dto) {
        Student student = new Student();
        Group group = getGroupById(dto.getGroup());

        mapper.put(dto, student);

        student.setGroup(group);
        student.setCreatedAt(LocalDateTime.now());
        student.setLastModifiedAt(LocalDateTime.now());

        return mapper.map(repository.save(student));
    }

    @Override
    public List<StudentDto> batchCreate(List<StudentDto> dtoList) {
        List<Student> students = new ArrayList<>();
        Group group = extractGroup(dtoList);

        LocalDateTime now = LocalDateTime.now();

        for (StudentDto dto : dtoList) {
            Student student = new Student();

            mapper.put(dto, student);

            student.setGroup(group);
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
        Group group = getGroupById(dto.getGroup());

        mapper.put(dto, student);

        student.setGroup(group);
        student.setCreatedAt(LocalDateTime.now());
        student.setLastModifiedAt(LocalDateTime.now());

        return mapper.map(repository.save(student));
    }

    @Override
    public StudentDto patch(long id, StudentDto dto) {
        Student student = getStudentById(id);
        Group group = dto.getGroup() != null ? getGroupById(dto.getGroup()) : student.getGroup();

        mapper.patch(dto, student);

        student.setGroup(group);
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

    private Group getGroupById(long id) {
        return groupService.findRawById(id).orElseThrow(() -> new NotFoundException(Group.class, id));
    }

    private Group extractGroup(List<StudentDto> dtoList) {
        Set<Long> groupIds = dtoList.stream()
                .map(StudentDto::getGroup)
                .collect(Collectors.toSet());
        if (groupIds.size() != 1) {
            throw new VariousParentEntitiesException(new ArrayList<>(groupIds));
        }
        return getGroupById(groupIds.iterator().next());
    }

    @Autowired
    public void setCourseService(CourseService courseService) {
        this.courseService = courseService;
    }
}
