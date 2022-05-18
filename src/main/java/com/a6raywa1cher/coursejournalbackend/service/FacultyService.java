package com.a6raywa1cher.coursejournalbackend.service;

import com.a6raywa1cher.coursejournalbackend.dto.FacultyDto;
import com.a6raywa1cher.coursejournalbackend.model.Faculty;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;

public interface FacultyService {
    FacultyDto getById(long id);

    List<FacultyDto> getAllFaculties(Sort sort);

    Optional<Faculty> findRawById(long id);

    FacultyDto create(FacultyDto dto);

    FacultyDto update(long id, FacultyDto dto);

    FacultyDto patch(long id, FacultyDto dto);

    void delete(long id);
}
