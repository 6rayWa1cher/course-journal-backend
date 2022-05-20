package com.a6raywa1cher.coursejournalbackend.service.impl;

import com.a6raywa1cher.coursejournalbackend.dto.GroupDto;
import com.a6raywa1cher.coursejournalbackend.dto.exc.ConflictException;
import com.a6raywa1cher.coursejournalbackend.dto.exc.NotFoundException;
import com.a6raywa1cher.coursejournalbackend.dto.exc.TransferNotAllowedException;
import com.a6raywa1cher.coursejournalbackend.dto.mapper.MapStructMapper;
import com.a6raywa1cher.coursejournalbackend.model.Course;
import com.a6raywa1cher.coursejournalbackend.model.Faculty;
import com.a6raywa1cher.coursejournalbackend.model.Group;
import com.a6raywa1cher.coursejournalbackend.model.repo.CourseRepository;
import com.a6raywa1cher.coursejournalbackend.model.repo.FacultyRepository;
import com.a6raywa1cher.coursejournalbackend.model.repo.GroupRepository;
import com.a6raywa1cher.coursejournalbackend.service.GroupService;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class GroupServiceImpl implements GroupService {
    private final GroupRepository repository;

    private final FacultyRepository facultyRepository;

    private final CourseRepository courseRepository;

    private final MapStructMapper mapper;

    public GroupServiceImpl(GroupRepository repository, FacultyRepository facultyRepository, CourseRepository courseRepository, MapStructMapper mapper) {
        this.repository = repository;
        this.facultyRepository = facultyRepository;
        this.courseRepository = courseRepository;
        this.mapper = mapper;
    }

    @Override
    public GroupDto getById(long id) {
        return mapper.map(getGroupById(id));
    }

    @Override
    public Optional<Group> findRawById(long id) {
        return repository.findById(id);
    }

    @Override
    public List<GroupDto> getByFaculty(long facultyId, Sort sort) {
        Faculty faculty = getFacultyById(facultyId);
        return repository.getAllByFaculty(faculty, sort).stream()
                .map(mapper::map)
                .toList();
    }

    @Override
    public List<GroupDto> getByCourse(long courseId, Sort sort) {
        Course course = getCourseById(courseId);
        return repository.getAllByCourse(course, sort).stream()
                .map(mapper::map)
                .toList();
    }

    @Override
    public GroupDto create(GroupDto dto) {
        Group group = new Group();
        Faculty faculty = getFacultyById(dto.getFaculty());
        String name = dto.getName();
        LocalDateTime createAndModifyDateTime = LocalDateTime.now();

        assertUniqueFacultyAndNamePair(faculty, name);
        mapper.put(dto, group);

        group.setFaculty(faculty);
        group.setCreatedAt(createAndModifyDateTime);
        group.setLastModifiedAt(createAndModifyDateTime);
        return mapper.map(repository.save(group));
    }

    @Override
    public GroupDto update(long id, GroupDto dto) {
        Group group = getGroupById(id);
        Faculty faculty = getFacultyById(dto.getFaculty());

        assertNoFacultyChanged(group.getFaculty(), faculty);
        assertUniqueFacultyAndNamePairOrNotChanged(faculty, group.getName(), dto.getName());
        mapper.put(dto, group);

        group.setLastModifiedAt(LocalDateTime.now());
        return mapper.map(repository.save(group));
    }

    @Override
    public GroupDto patch(long id, GroupDto dto) {
        Group group = getGroupById(id);
        Faculty faculty = dto.getFaculty() != null ? getFacultyById(dto.getFaculty()) : group.getFaculty();

        assertNoFacultyChanged(group.getFaculty(), faculty);
        if (dto.getName() != null) assertUniqueFacultyAndNamePairOrNotChanged(faculty, group.getName(), dto.getName());
        mapper.patch(dto, group);

        group.setLastModifiedAt(LocalDateTime.now());
        return mapper.map(repository.save(group));
    }

    @Override
    public void delete(long id) {
        Group group = getGroupById(id);
        repository.delete(group);
    }

    private Group getGroupById(long id) {
        return repository.findById(id).orElseThrow(() -> new NotFoundException(Group.class, id));
    }

    private Faculty getFacultyById(long id) {
        return facultyRepository.findById(id).orElseThrow(() -> new NotFoundException(Faculty.class, id));
    }

    private Course getCourseById(long id) {
        return courseRepository.findById(id).orElseThrow(() -> new NotFoundException(Course.class, id));
    }

    private void assertUniqueFacultyAndNamePair(Faculty faculty, String name) {
        if (repository.findByFacultyAndName(faculty, name).isPresent()) {
            throw new ConflictException(Faculty.class,
                    "faculty", Long.toString(faculty.getId()),
                    "name", name);
        }
    }

    private void assertUniqueFacultyAndNamePairOrNotChanged(Faculty faculty, String prevName, String newName) {
        if (!Objects.equals(prevName, newName)) {
            assertUniqueFacultyAndNamePair(faculty, newName);
        }
    }

    private void assertNoFacultyChanged(Faculty oldFaculty, Faculty newFaculty) {
        if (!Objects.equals(oldFaculty, newFaculty)) {
            throw new TransferNotAllowedException(Faculty.class, "faculty", newFaculty.getId(), oldFaculty.getId());
        }
    }
}
