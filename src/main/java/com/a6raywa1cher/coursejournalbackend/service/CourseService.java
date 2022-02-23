package com.a6raywa1cher.coursejournalbackend.service;

import com.a6raywa1cher.coursejournalbackend.dto.CourseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CourseService {
    CourseDto getById(long id);

    Page<CourseDto> getPage(Pageable pageable);

    Page<CourseDto> getByNameContains(String query, Pageable pageable);

    CourseDto create(CourseDto dto);

    CourseDto update(long id, CourseDto dto);

    CourseDto patch(long id, CourseDto dto);

    void delete(long id);
}
