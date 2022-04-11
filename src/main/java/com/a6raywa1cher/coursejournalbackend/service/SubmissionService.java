package com.a6raywa1cher.coursejournalbackend.service;

import com.a6raywa1cher.coursejournalbackend.dto.SubmissionDto;
import com.a6raywa1cher.coursejournalbackend.model.Submission;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;

public interface SubmissionService {
    SubmissionDto getById(long id);

    Optional<Submission> findRawById(long id);

    List<SubmissionDto> getByStudentAndCourse(long studentId, long courseId, Sort sort);

    List<SubmissionDto> getByCourse(long courseId, Sort sort);

    List<SubmissionDto> getByTask(long taskId, Sort sort);

    SubmissionDto create(SubmissionDto dto);

    SubmissionDto update(long id, SubmissionDto dto);

    SubmissionDto patch(long id, SubmissionDto dto);

    void delete(long id);
}