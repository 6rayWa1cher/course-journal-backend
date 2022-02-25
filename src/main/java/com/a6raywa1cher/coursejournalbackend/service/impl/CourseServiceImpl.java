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
import java.util.Locale;
import java.util.Objects;

import static com.a6raywa1cher.coursejournalbackend.utils.CommonUtils.coalesce;

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
        return repository.findByNameContains(query.toLowerCase(Locale.ROOT), pageable).map(mapper::map);
    }

    @Override
    public Page<CourseDto> getByOwner(long ownerId, Pageable pageable) {
        User owner = getUserById(ownerId);
        return repository.findByOwner(owner, pageable).map(mapper::map);
    }

    @Override
    public Page<CourseDto> getByOwnerAndNameContains(long ownerId, String name, Pageable pageable) {
        User owner = getUserById(ownerId);
        return repository.findByOwnerAndNameContains(owner, name.toLowerCase(Locale.ROOT), pageable).map(mapper::map);
    }

    @Override
    public CourseDto create(CourseDto dto) {
        Course entity = new Course();
        User owner = getUserById(dto.getOwner());

        assertNameAvailable(dto.getName(), owner);

        mapper.put(dto, entity);

        entity.setOwner(owner);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setLastModifiedAt(LocalDateTime.now());

        return mapper.map(repository.save(entity));
    }

    @Override
    public CourseDto update(long id, CourseDto dto) {
        Course entity = $getById(id);
        User newOwner = userService.findRawById(dto.getOwner())
                .orElseThrow(() -> new NotFoundException(User.class, dto.getOwner()));

        assertNameNotChangedOrAvailable(entity.getName(), dto.getName(), entity.getOwner(), newOwner);

        mapper.put(dto, entity);

        entity.setOwner(newOwner);
        entity.setLastModifiedAt(LocalDateTime.now());

        return mapper.map(repository.save(entity));
    }

    @Override
    public CourseDto patch(long id, CourseDto dto) {
        Course entity = $getById(id);
        User owner = dto.getOwner() != null ? getUserById(dto.getOwner()) : entity.getOwner();

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

    private User getUserById(long id) {
        return userService.findRawById(id).orElseThrow(() -> new NotFoundException(User.class, id));
    }

    private void assertNameNotChangedOrAvailable(String before, String now, User beforeUser, User afterUser) {
        if (!Objects.equals(before, now) || !Objects.equals(beforeUser, afterUser)) {
            assertNameAvailable(now, afterUser);
        }
    }

    private void assertNameAvailable(String name, User user) {
        if (repository.existsByNameAndOwner(name, user)) {
            throw new ConflictException(User.class, "name", name, "owner", Long.toString(user.getId()));
        }
    }
}
