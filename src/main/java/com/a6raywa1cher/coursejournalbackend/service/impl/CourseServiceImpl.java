package com.a6raywa1cher.coursejournalbackend.service.impl;

import com.a6raywa1cher.coursejournalbackend.dto.CourseDto;
import com.a6raywa1cher.coursejournalbackend.dto.CourseFullDto;
import com.a6raywa1cher.coursejournalbackend.dto.exc.ConflictException;
import com.a6raywa1cher.coursejournalbackend.dto.exc.NotFoundException;
import com.a6raywa1cher.coursejournalbackend.dto.mapper.MapStructMapper;
import com.a6raywa1cher.coursejournalbackend.model.Course;
import com.a6raywa1cher.coursejournalbackend.model.Employee;
import com.a6raywa1cher.coursejournalbackend.model.Group;
import com.a6raywa1cher.coursejournalbackend.model.Student;
import com.a6raywa1cher.coursejournalbackend.model.repo.CourseRepository;
import com.a6raywa1cher.coursejournalbackend.service.CourseService;
import com.a6raywa1cher.coursejournalbackend.service.EmployeeService;
import com.a6raywa1cher.coursejournalbackend.service.GroupService;
import com.a6raywa1cher.coursejournalbackend.service.StudentService;
import com.a6raywa1cher.coursejournalbackend.utils.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.StreamSupport;

import static com.a6raywa1cher.coursejournalbackend.utils.CommonUtils.coalesce;

@Service
@Transactional
public class CourseServiceImpl implements CourseService {
    private final CourseRepository repository;

    private final MapStructMapper mapper;

    private final EmployeeService employeeService;

    private final StudentService studentService;

    private final GroupService groupService;

    @Autowired
    public CourseServiceImpl(CourseRepository repository, MapStructMapper mapper, EmployeeService employeeService, @Lazy StudentService studentService, GroupService groupService) {
        this.repository = repository;
        this.mapper = mapper;
        this.employeeService = employeeService;
        this.studentService = studentService;
        this.groupService = groupService;
    }


    @Override
    public CourseFullDto getById(long id) {
        return mapper.mapFull($getById(id));
    }

    @Override
    public Optional<Course> findRawById(long id) {
        return repository.findById(id);
    }

    @Override
    public List<Course> findAllRawById(Collection<Long> ids) {
        return StreamSupport.stream(repository.findAllById(ids).spliterator(), false).toList();
    }

    @Override
    public Page<CourseDto> getPage(Pageable pageable) {
        return repository.findAll(pageable).map(mapper::map);
    }

    @Override
    public Page<CourseDto> getByNameContains(String query, Pageable pageable) {
        return repository.findByNameContains(query.toLowerCase(Locale.ROOT), pageable).map(mapper::map);
    }

    @Override
    public Page<CourseDto> getByOwner(long ownerId, Pageable pageable) {
        Employee owner = getUserById(ownerId);
        return repository.findByOwner(owner, pageable).map(mapper::map);
    }

    @Override
    public Page<CourseDto> getByOwnerAndNameContains(long ownerId, String name, Pageable pageable) {
        Employee owner = getUserById(ownerId);
        return repository.findByOwnerAndNameContains(owner, name.toLowerCase(Locale.ROOT), pageable).map(mapper::map);
    }

    @Override
    public Page<CourseFullDto> getByGroupId(long groupId, Pageable pageable) {
        Group group = getGroupById(groupId);
        return repository.findAllByGroup(group, pageable).map(mapper::mapFull);
    }

    @Override
    public CourseFullDto create(CourseFullDto dto) {
        Course entity = new Course();
        Employee owner = getUserById(dto.getOwner());
        List<Student> students = getStudentListByIds(dto.getStudents());

        assertNameAvailable(dto.getName(), owner);

        mapper.put(dto, entity);

        setStudentList(entity, students);
        entity.setOwner(owner);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setLastModifiedAt(LocalDateTime.now());

        return mapper.mapFull(repository.save(entity));
    }

    @Override
    public CourseFullDto update(long id, CourseFullDto dto) {
        Course entity = $getById(id);
        Employee newOwner = employeeService.findRawById(dto.getOwner())
                .orElseThrow(() -> new NotFoundException(Employee.class, dto.getOwner()));
        List<Student> students = getStudentListByIds(dto.getStudents());

        assertNameNotChangedOrAvailable(entity.getName(), dto.getName(), entity.getOwner(), newOwner);

        mapper.put(dto, entity);

        setStudentList(entity, students);
        entity.setOwner(newOwner);
        entity.setLastModifiedAt(LocalDateTime.now());

        return mapper.mapFull(repository.save(entity));
    }

    @Override
    public CourseFullDto patch(long id, CourseFullDto dto) {
        Course entity = $getById(id);
        Employee owner = dto.getOwner() != null ? getUserById(dto.getOwner()) : entity.getOwner();
        List<Student> students = dto.getStudents() != null ? getStudentListByIds(dto.getStudents()) : entity.getStudents();

        assertNameNotChangedOrAvailable(
                entity.getName(), coalesce(dto.getName(), entity.getName()),
                entity.getOwner(), owner
        );

        mapper.patch(dto, entity);

        setStudentList(entity, students);
        entity.setOwner(owner);
        entity.setLastModifiedAt(LocalDateTime.now());

        return mapper.mapFull(repository.save(entity));
    }

    @Override
    public void delete(long id) {
        Course entity = $getById(id);
        repository.delete(entity);
    }

    private Course $getById(long id) {
        return repository.findById(id).orElseThrow(() -> new NotFoundException(Course.class, id));
    }

    private Employee getUserById(long id) {
        return employeeService.findRawById(id).orElseThrow(() -> new NotFoundException(Employee.class, id));
    }

    private List<Student> getStudentListByIds(List<Long> ids) {
        if (ids == null) {
            return new ArrayList<>();
        }
        List<Student> rawById = studentService.findRawById(ids);
        if (rawById.size() != ids.size()) {
            throw new NotFoundException(Student.class, EntityUtils.getAnyNotFound(rawById, ids));
        }
        return new ArrayList<>(rawById);
    }

    private Group getGroupById(long groupId) {
        Optional<Group> rawById = groupService.findRawById(groupId);
        if (rawById.isEmpty()) {
            throw new NotFoundException(Group.class, groupId);
        }
        return rawById.get();
    }

    private void assertNameNotChangedOrAvailable(String before, String now, Employee beforeEmployee, Employee afterEmployee) {
        if (!Objects.equals(before, now) || !Objects.equals(beforeEmployee, afterEmployee)) {
            assertNameAvailable(now, afterEmployee);
        }
    }

    private void assertNameAvailable(String name, Employee employee) {
        if (repository.existsByNameAndOwner(name, employee)) {
            throw new ConflictException(Employee.class, "name", name, "owner", Long.toString(employee.getId()));
        }
    }

    private void setStudentList(Course course, List<Student> newList) {
        List<Student> original = course.getStudents();
        if (original.equals(newList)) return;
        for (Student originalStudent : original) {
            if (!newList.contains(originalStudent)) {
                originalStudent.getCourses().remove(course);
            }
        }
        for (Student newStudent : newList) {
            if (!original.contains(newStudent)) {
                newStudent.getCourses().add(course);
            }
        }
        course.setStudents(newList);
    }
}
