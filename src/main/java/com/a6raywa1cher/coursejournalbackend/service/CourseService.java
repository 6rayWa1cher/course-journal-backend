package com.a6raywa1cher.coursejournalbackend.service;

import com.a6raywa1cher.coursejournalbackend.dto.CourseDto;
import com.a6raywa1cher.coursejournalbackend.model.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface CourseService {
    CourseDto getById(long id);

    Optional<Course> findRawById(long id);

    List<Course> findAllRawById(Collection<Long> id);

    Page<CourseDto> getPage(Pageable pageable);

    Page<CourseDto> getByNameContains(String query, Pageable pageable);

    Page<CourseDto> getByOwner(long ownerId, Pageable pageable);

    Page<CourseDto> getByOwnerAndNameContains(long ownerId, String name, Pageable pageable);

    CourseDto create(CourseDto dto);

    CourseDto update(long id, CourseDto dto);

    CourseDto patch(long id, CourseDto dto);

    void delete(long id);
}
