package com.a6raywa1cher.coursejournalbackend.service;

import com.a6raywa1cher.coursejournalbackend.dto.SubmissionDto;
import com.a6raywa1cher.coursejournalbackend.model.Submission;

import java.util.List;
import java.util.Optional;

public interface SubmissionService {
    SubmissionDto getById(long id);

    Optional<Submission> findRawById(long id);

    List<SubmissionDto> getByStudentAndCourse(long studentId, long courseId);

    List<SubmissionDto> getByCourse(long taskId);

    SubmissionDto create(SubmissionDto dto);

    SubmissionDto update(long id, SubmissionDto dto);

    SubmissionDto patch(long id, SubmissionDto dto);

    void delete(long id);
}
