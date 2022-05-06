package com.a6raywa1cher.coursejournalbackend.service.impl;

import com.a6raywa1cher.coursejournalbackend.dto.CourseDto;
import com.a6raywa1cher.coursejournalbackend.dto.CourseFullDto;
import com.a6raywa1cher.coursejournalbackend.dto.exc.ConflictException;
import com.a6raywa1cher.coursejournalbackend.dto.exc.NotFoundException;
import com.a6raywa1cher.coursejournalbackend.dto.mapper.MapStructMapper;
import com.a6raywa1cher.coursejournalbackend.model.Course;
import com.a6raywa1cher.coursejournalbackend.model.Employee;
import com.a6raywa1cher.coursejournalbackend.model.Student;
import com.a6raywa1cher.coursejournalbackend.model.repo.CourseRepository;
import com.a6raywa1cher.coursejournalbackend.service.CourseService;
import com.a6raywa1cher.coursejournalbackend.service.EmployeeService;
import com.a6raywa1cher.coursejournalbackend.service.StudentService;
import com.a6raywa1cher.coursejournalbackend.utils.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    public CourseServiceImpl(CourseRepository repository, MapStructMapper mapper, EmployeeService employeeService, StudentService studentService) {
        this.repository = repository;
        this.mapper = mapper;
        this.employeeService = employeeService;
        this.studentService = studentService;
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
    public CourseFullDto create(CourseFullDto dto) {
        Course entity = new Course();
        Employee owner = getUserById(dto.getOwner());

        assertNameAvailable(dto.getName(), owner);

        mapper.put(dto, entity);

        entity.setOwner(owner);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setLastModifiedAt(LocalDateTime.now());

        return mapper.mapFull(repository.save(entity));
    }

    @Override
    public CourseDto update(long id, CourseDto dto) {
        Course entity = $getById(id);
        Employee newOwner = employeeService.findRawById(dto.getOwner())
                .orElseThrow(() -> new NotFoundException(Employee.class, dto.getOwner()));

        assertNameNotChangedOrAvailable(entity.getName(), dto.getName(), entity.getOwner(), newOwner);

        mapper.put(dto, entity);

        entity.setOwner(newOwner);
        entity.setLastModifiedAt(LocalDateTime.now());

        return mapper.map(repository.save(entity));
    }

    @Override
    public CourseDto patch(long id, CourseDto dto) {
        Course entity = $getById(id);
        Employee owner = dto.getOwner() != null ? getUserById(dto.getOwner()) : entity.getOwner();

        assertNameNotChangedOrAvailable(
                entity.getName(), coalesce(dto.getName(), entity.getName()),
                entity.getOwner(), owner
        );

        mapper.patch(dto, entity);

        entity.setOwner(owner);
        entity.setLastModifiedAt(LocalDateTime.now());

        return mapper.map(repository.save(entity));
    }

    @Override
    public CourseFullDto update(long id, CourseFullDto dto) {
        Course entity = $getById(id);
        Employee newOwner = employeeService.findRawById(dto.getOwner())
                .orElseThrow(() -> new NotFoundException(Employee.class, dto.getOwner()));
        List<Student> students =

        assertNameNotChangedOrAvailable(entity.getName(), dto.getName(), entity.getOwner(), newOwner);

        mapper.put(dto, entity);

        entity.setOwner(newOwner);
        entity.setLastModifiedAt(LocalDateTime.now());

        return mapper.map(repository.save(entity));
    }

    @Override
    public CourseFullDto patch(long id, CourseFullDto dto) {
        Course entity = $getById(id);
        Employee owner = dto.getOwner() != null ? getUserById(dto.getOwner()) : entity.getOwner();

        assertNameNotChangedOrAvailable(
                entity.getName(), coalesce(dto.getName(), entity.getName()),
                entity.getOwner(), owner
        );

        mapper.patch(dto, entity);

        entity.setOwner(owner);
        entity.setLastModifiedAt(LocalDateTime.now());

        return mapper.map(repository.save(entity));
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
        List<Student> rawById = studentService.(ids);
        if (rawById.size() != ids.size()) {
            throw new NotFoundException(Student.class, EntityUtils.getAnyNotFound(rawById, ids));
        }
        return rawById;
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
}
