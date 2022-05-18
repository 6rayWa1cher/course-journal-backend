package com.a6raywa1cher.coursejournalbackend.service.impl;

import com.a6raywa1cher.coursejournalbackend.dto.FacultyDto;
import com.a6raywa1cher.coursejournalbackend.dto.exc.ConflictException;
import com.a6raywa1cher.coursejournalbackend.dto.exc.NotFoundException;
import com.a6raywa1cher.coursejournalbackend.dto.mapper.MapStructMapper;
import com.a6raywa1cher.coursejournalbackend.model.Faculty;
import com.a6raywa1cher.coursejournalbackend.model.repo.FacultyRepository;
import com.a6raywa1cher.coursejournalbackend.service.FacultyService;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class FacultyServiceImpl implements FacultyService {

    private final FacultyRepository repository;

    private final MapStructMapper mapper;

    public FacultyServiceImpl(FacultyRepository repository, MapStructMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public FacultyDto getById(long id) {
        return mapper.map(getFacultyById(id));
    }

    @Override
    public List<FacultyDto> getAllFaculties(Sort sort) {
        return repository.findAll(sort).stream().map(mapper::map).toList();
    }

    @Override
    public Optional<Faculty> findRawById(long id) {
        return repository.findById(id);
    }

    @Override
    public FacultyDto create(FacultyDto dto) {
        Faculty faculty = new Faculty();
        String name = dto.getName();
        LocalDateTime createdAndModifiedTime = LocalDateTime.now();

        assertUniqueName(name);
        mapper.put(dto, faculty);

        faculty.setCreatedAt(createdAndModifiedTime);
        faculty.setLastModifiedAt(createdAndModifiedTime);
        return mapper.map(repository.save(faculty));
    }

    @Override
    public FacultyDto update(long id, FacultyDto dto) {
        Faculty faculty = getFacultyById(id);

        assertUniqueNameOrNotChanged(faculty.getName(), dto.getName());
        mapper.put(dto, faculty);

        faculty.setLastModifiedAt(LocalDateTime.now());

        return mapper.map(repository.save(faculty));
    }

    @Override
    public FacultyDto patch(long id, FacultyDto dto) {
        Faculty faculty = getFacultyById(id);

        if (dto.getName() != null) assertUniqueNameOrNotChanged(faculty.getName(), dto.getName());
        mapper.patch(dto, faculty);

        faculty.setLastModifiedAt(LocalDateTime.now());

        return mapper.map(repository.save(faculty));
    }

    @Override
    public void delete(long id) {
        Faculty faculty = getFacultyById(id);
        repository.delete(faculty);
    }

    private Faculty getFacultyById(long id) {
        return repository.findById(id).orElseThrow(() -> new NotFoundException(Faculty.class, id));
    }

    private void assertUniqueName(String name) {
        if (repository.findByName(name).isPresent()) {
            throw new ConflictException(Faculty.class,
                    "name", name
            );
        }
    }

    private void assertUniqueNameOrNotChanged(String prevName, String newName) {
        if (!Objects.equals(prevName, newName)) {
            assertUniqueName(newName);
        }
    }
}
