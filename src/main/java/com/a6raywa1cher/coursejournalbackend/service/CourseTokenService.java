package com.a6raywa1cher.coursejournalbackend.service;

import com.a6raywa1cher.coursejournalbackend.dto.CourseTokenDto;
import com.a6raywa1cher.coursejournalbackend.model.CourseToken;

import java.util.Optional;

public interface CourseTokenService {
    CourseTokenDto getById(long id);

    Optional<CourseToken> findRawById(long id);

    Optional<CourseTokenDto> findByToken(String token);

    CourseTokenDto getByCourseId(long courseId);

    CourseTokenDto create(CourseTokenDto dto);

    void delete(long id);
}
