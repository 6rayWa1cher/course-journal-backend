package com.a6raywa1cher.coursejournalbackend.service.impl;

import com.a6raywa1cher.coursejournalbackend.dto.CourseDto;
import com.a6raywa1cher.coursejournalbackend.dto.exc.ConflictException;
import com.a6raywa1cher.coursejournalbackend.dto.exc.NotFoundException;
import com.a6raywa1cher.coursejournalbackend.dto.mapper.MapStructMapper;
import com.a6raywa1cher.coursejournalbackend.model.Course;
import com.a6raywa1cher.coursejournalbackend.model.User;
import com.a6raywa1cher.coursejournalbackend.model.repo.CourseRepository;
import com.a6raywa1cher.coursejournalbackend.service.CourseService;
import com.a6raywa1cher.coursejournalbackend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Objects;

@Service
public class CourseServiceImpl implements CourseService {
    private final CourseRepository repository;
    private final MapStructMapper mapper;
    private final UserService userService;

    @Autowired
    public CourseServiceImpl(CourseRepository repository, MapStructMapper mapper, UserService userService) {
        this.repository = repository;
        this.mapper = mapper;
        this.userService = userService;
    }


    @Override
    public CourseDto getById(long id) {
        return mapper.map($getById(id));
    }

    @Override
    public Page<CourseDto> getPage(Pageable pageable) {
        return repository.findAll(pageable).map(mapper::map);
    }

    @Override
    public Page<CourseDto> getByNameContains(String query, Pageable pageable) {
        return repository.findByNameContains(query, pageable).map(mapper::map);
    }

    @Override
    public CourseDto create(CourseDto dto) {
        Course entity = new Course();
        User owner = userService.getRawById(dto.getOwner());

        assertNameAvailable(dto.getName(), owner);

        mapper.put(dto, entity);

        entity.setCreatedAt(LocalDateTime.now());
        entity.setLastModifiedAt(LocalDateTime.now());

        return mapper.map(repository.save(entity));
    }

    @Override
    public CourseDto update(long id, CourseDto dto) {
        return null;
    }

    @Override
    public CourseDto patch(long id, CourseDto dto) {
        return null;
    }

    @Override
    public void delete(long id) {
        Course entity = $getById(id);
        repository.delete(entity);
    }

    private Course $getById(long id) {
        return repository.findById(id).orElseThrow(() -> new NotFoundException(Course.class, id));
    }

    private void assertNameNotChangedOrAvailable(String before, String now, User user) {
        if (!Objects.equals(before, now)) {
            assertNameAvailable(now, user);
        }
    }

    private void assertNameAvailable(String name, User user) {
        if (repository.existsByNameAndOwner(name, user)) {
            throw new ConflictException(User.class, "name", name, "owner", Long.toString(user.getId()));
        }
    }
}
