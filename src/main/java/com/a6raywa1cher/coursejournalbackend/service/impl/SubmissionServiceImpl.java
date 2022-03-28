package com.a6raywa1cher.coursejournalbackend.service.impl;

import com.a6raywa1cher.coursejournalbackend.dto.SubmissionDto;
import com.a6raywa1cher.coursejournalbackend.dto.exc.NotFoundException;
import com.a6raywa1cher.coursejournalbackend.dto.mapper.MapStructMapper;
import com.a6raywa1cher.coursejournalbackend.model.Submission;
import com.a6raywa1cher.coursejournalbackend.model.repo.SubmissionRepository;
import com.a6raywa1cher.coursejournalbackend.service.CriteriaService;
import com.a6raywa1cher.coursejournalbackend.service.StudentService;
import com.a6raywa1cher.coursejournalbackend.service.SubmissionService;
import com.a6raywa1cher.coursejournalbackend.service.TaskService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SubmissionServiceImpl implements SubmissionService {
    private final SubmissionRepository repository;

    private final StudentService studentService;

    private final TaskService taskService;

    private final CriteriaService criteriaService;

    private final MapStructMapper mapper;

    public SubmissionServiceImpl(SubmissionRepository repository, StudentService studentService, TaskService taskService, CriteriaService criteriaService, MapStructMapper mapper) {
        this.repository = repository;
        this.studentService = studentService;
        this.taskService = taskService;
        this.criteriaService = criteriaService;
        this.mapper = mapper;
    }

    @Override
    public SubmissionDto getById(long id) {
        return repository.findById(id).map(mapper::map).orElseThrow(() -> new NotFoundException(Submission.class, id));
    }

    @Override
    public Optional<Submission> findRawById(long id) {
        return Optional.empty();
    }

    @Override
    public List<SubmissionDto> getByStudentAndCourse(long studentId, long courseId) {
        return null;
    }

    @Override
    public List<SubmissionDto> getByCourse(long taskId) {
        return null;
    }

    @Override
    public SubmissionDto create(SubmissionDto dto) {
        return null;
    }

    @Override
    public SubmissionDto update(long id, SubmissionDto dto) {
        return null;
    }

    @Override
    public SubmissionDto patch(long id, SubmissionDto dto) {
        return null;
    }

    @Override
    public void delete(long id) {

    }
}
