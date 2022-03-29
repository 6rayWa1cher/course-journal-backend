package com.a6raywa1cher.coursejournalbackend.service.impl;

import com.a6raywa1cher.coursejournalbackend.dto.SubmissionDto;
import com.a6raywa1cher.coursejournalbackend.dto.exc.ConflictException;
import com.a6raywa1cher.coursejournalbackend.dto.exc.NotFoundException;
import com.a6raywa1cher.coursejournalbackend.dto.exc.TransferNotAllowedException;
import com.a6raywa1cher.coursejournalbackend.dto.exc.VariousParentEntitiesException;
import com.a6raywa1cher.coursejournalbackend.dto.mapper.MapStructMapper;
import com.a6raywa1cher.coursejournalbackend.model.*;
import com.a6raywa1cher.coursejournalbackend.model.repo.SubmissionRepository;
import com.a6raywa1cher.coursejournalbackend.service.*;
import com.a6raywa1cher.coursejournalbackend.utils.EntityUtils;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class SubmissionServiceImpl implements SubmissionService {
    private final SubmissionRepository repository;

    private final StudentService studentService;

    private final TaskService taskService;

    private final CriteriaService criteriaService;

    private final MapStructMapper mapper;

    private final CourseService courseService;

    private final SubmissionScoringService scoringService;

    public SubmissionServiceImpl(SubmissionRepository repository, StudentService studentService,
                                 TaskService taskService, CriteriaService criteriaService, MapStructMapper mapper,
                                 CourseService courseService, SubmissionScoringService scoringService) {
        this.repository = repository;
        this.studentService = studentService;
        this.taskService = taskService;
        this.criteriaService = criteriaService;
        this.mapper = mapper;
        this.courseService = courseService;
        this.scoringService = scoringService;
    }

    @Override
    public SubmissionDto getById(long id) {
        return mapper.map(getSubmissionById(id));
    }

    @Override
    public Optional<Submission> findRawById(long id) {
        return repository.findById(id);
    }

    @Override
    public List<SubmissionDto> getByStudentAndCourse(long studentId, long courseId, Sort sort) {
        Student student = getStudentById(studentId);
        Course course = getCourseById(courseId);
        return repository.getAllByStudentAndCourse(student, course, sort).stream()
                .map(mapper::map)
                .toList();
    }

    @Override
    public List<SubmissionDto> getByCourse(long courseId, Sort sort) {
        Course course = getCourseById(courseId);
        return repository.getAllByCourse(course, sort).stream()
                .map(mapper::map)
                .toList();
    }

    @Override
    public List<SubmissionDto> getByTask(long taskId, Sort sort) {
        Task task = getTaskById(taskId);
        return repository.getAllByTask(task, sort).stream()
                .map(mapper::map)
                .toList();
    }

    @Override
    public SubmissionDto create(SubmissionDto dto) {
        Submission submission = new Submission();
        Task task = getTaskById(dto.getTask());
        Student student = getStudentById(dto.getStudent());
        List<Criteria> satisfiedCriteria = getCriteriaListByIds(dto.getSatisfiedCriteria());

        assertUniqueTaskStudentPair(task, student);
        assertSameCourseAndTask(satisfiedCriteria, task, student);
        mapper.put(dto, submission);

        submission.setTask(task);
        submission.setStudent(student);
        submission.setSatisfiedCriteria(satisfiedCriteria);
        submission.setMainScore(scoringService.getMainScore(submission));
        submission.setCreatedAt(LocalDateTime.now());
        submission.setLastModifiedAt(LocalDateTime.now());
        return mapper.map(repository.save(submission));
    }

    @Override
    public SubmissionDto update(long id, SubmissionDto dto) {
        Submission submission = getSubmissionById(id);
        Task task = getTaskById(dto.getTask());
        Student student = getStudentById(dto.getStudent());
        List<Criteria> satisfiedCriteria = getCriteriaListByIds(dto.getSatisfiedCriteria());

        assertNoTaskChange(submission.getTask(), task);
        assertNoStudentChange(submission.getStudent(), student);
        assertSameCourseAndTask(satisfiedCriteria, task, student);
        mapper.put(dto, submission);

        submission.setSatisfiedCriteria(new ArrayList<>(satisfiedCriteria));
        submission.setMainScore(scoringService.getMainScore(submission));
        submission.setLastModifiedAt(LocalDateTime.now());
        return mapper.map(repository.save(submission));
    }

    @Override
    public SubmissionDto patch(long id, SubmissionDto dto) {
        Submission submission = getSubmissionById(id);
        Task task = dto.getTask() != null ? getTaskById(dto.getTask()) : submission.getTask();
        Student student = dto.getStudent() != null ? getStudentById(dto.getStudent()) : submission.getStudent();
        List<Criteria> satisfiedCriteria = dto.getSatisfiedCriteria() != null ?
                getCriteriaListByIds(dto.getSatisfiedCriteria()) :
                submission.getSatisfiedCriteria();

        assertNoTaskChange(submission.getTask(), task);
        assertNoStudentChange(submission.getStudent(), student);
        assertSameCourseAndTask(satisfiedCriteria, task, student);
        mapper.patch(dto, submission);

        submission.setSatisfiedCriteria(new ArrayList<>(satisfiedCriteria));
        submission.setMainScore(scoringService.getMainScore(submission));
        submission.setLastModifiedAt(LocalDateTime.now());
        return mapper.map(repository.save(submission));
    }

    @Override
    public void delete(long id) {
        Submission submission = getSubmissionById(id);
        repository.delete(submission);
    }

    private Submission getSubmissionById(long id) {
        return repository.findById(id).orElseThrow(() -> new NotFoundException(Submission.class, id));
    }

    private Student getStudentById(long id) {
        return studentService.findRawById(id).orElseThrow(() -> new NotFoundException(Student.class, id));
    }

    private Course getCourseById(long id) {
        return courseService.findRawById(id).orElseThrow(() -> new NotFoundException(Course.class, id));
    }

    private Task getTaskById(long id) {
        return taskService.findRawById(id).orElseThrow(() -> new NotFoundException(Task.class, id));
    }

    private List<Criteria> getCriteriaListByIds(List<Long> ids) {
        List<Criteria> rawById = criteriaService.findRawById(ids);
        if (rawById.size() != ids.size()) {
            throw new NotFoundException(Criteria.class, EntityUtils.getAnyNotFound(rawById, ids));
        }
        return rawById;
    }

    private void assertNoTaskChange(Task oldTask, Task newTask) {
        if (!Objects.equals(oldTask, newTask)) {
            throw new TransferNotAllowedException(Task.class, "task", oldTask.getId(), newTask.getId());
        }
    }

    private void assertNoStudentChange(Student oldStudent, Student newStudent) {
        if (!Objects.equals(oldStudent, newStudent)) {
            throw new TransferNotAllowedException(Task.class, "task", oldStudent.getId(), newStudent.getId());
        }
    }

    private void assertSameCourseAndTask(List<Criteria> satisfiedCriteria, Task task, Student student) {
        Set<Long> courseIds = new HashSet<>(
                satisfiedCriteria.stream()
                        .map(c -> c.getTask().getCourse().getId())
                        .toList()
        );
        courseIds.add(task.getCourse().getId());
        courseIds.add(student.getCourse().getId());
        if (courseIds.size() != 1) {
            throw new VariousParentEntitiesException(courseIds.stream().toList());
        }
        Set<Long> taskIds = new HashSet<>(
                satisfiedCriteria.stream()
                        .map(Criteria::getTask)
                        .map(Task::getId)
                        .toList()
        );
        taskIds.add(task.getId());
        if (taskIds.size() != 1) {
            throw new VariousParentEntitiesException(taskIds.stream().toList());
        }
    }

    private void assertUniqueTaskStudentPair(Task task, Student student) {
        if (repository.findByTaskAndStudent(task, student).isPresent()) {
            throw new ConflictException(Submission.class,
                    "task", Long.toString(task.getId()),
                    "student", Long.toString(student.getId()));
        }
    }
}
