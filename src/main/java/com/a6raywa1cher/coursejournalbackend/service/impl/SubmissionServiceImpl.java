package com.a6raywa1cher.coursejournalbackend.service.impl;

import com.a6raywa1cher.coursejournalbackend.dto.CriteriaDto;
import com.a6raywa1cher.coursejournalbackend.dto.SubmissionDto;
import com.a6raywa1cher.coursejournalbackend.dto.TaskDto;
import com.a6raywa1cher.coursejournalbackend.dto.exc.ConflictException;
import com.a6raywa1cher.coursejournalbackend.dto.exc.NotFoundException;
import com.a6raywa1cher.coursejournalbackend.dto.exc.TransferNotAllowedException;
import com.a6raywa1cher.coursejournalbackend.dto.exc.VariousParentEntitiesException;
import com.a6raywa1cher.coursejournalbackend.dto.mapper.MapStructMapper;
import com.a6raywa1cher.coursejournalbackend.model.*;
import com.a6raywa1cher.coursejournalbackend.model.repo.SubmissionRepository;
import com.a6raywa1cher.coursejournalbackend.service.*;
import com.a6raywa1cher.coursejournalbackend.utils.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SubmissionServiceImpl implements SubmissionService {
    private final SubmissionRepository repository;

    private final StudentService studentService;

    private TaskService taskService;

    private CriteriaService criteriaService;

    private final MapStructMapper mapper;

    private final CourseService courseService;

    private final SubmissionScoringService scoringService;

    public SubmissionServiceImpl(SubmissionRepository repository, StudentService studentService, MapStructMapper mapper,
                                 CourseService courseService, SubmissionScoringService scoringService) {
        this.repository = repository;
        this.studentService = studentService;
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
    public List<SubmissionDto> getByStudentAndCourse(long studentId, long courseId) {
        Student student = getStudentById(studentId);
        Course course = getCourseById(courseId);
        return repository.getAllByStudentAndCourse(student, course).stream()
                .map(mapper::map)
                .toList();
    }

    @Override
    public List<SubmissionDto> getByCourse(long courseId) {
        Course course = getCourseById(courseId);
        return repository.getAllByCourse(course).stream()
                .map(mapper::map)
                .toList();
    }

    @Override
    public List<SubmissionDto> getByTask(long taskId) {
        Task task = getTaskById(taskId);
        return repository.getAllByTask(task).stream()
                .map(mapper::map)
                .toList();
    }

    @Override
    public void recalculateMainScoreForTask(long taskId) {
        Task task = getTaskById(taskId);
        List<Submission> submissions = repository.getAllByTask(task);
        List<CriteriaDto> criteria = criteriaService.getByTaskId(taskId);
        TaskDto taskDto = mapper.map(task);
        for (Submission submission : submissions) {
            submission.setMainScore(scoringService.getMainScore(
                    mapper.map(submission),
                    taskDto,
                    criteria
            ));
        }
        repository.saveAll(submissions);
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
        setSatisfiedCriteria(submission, satisfiedCriteria);
        submission.setMainScore(scoringService.getMainScore(
                mapper.map(submission),
                mapper.map(task),
                criteriaService.getByTaskId(task.getId())
        ));
        submission.setCreatedAt(LocalDateTime.now());
        submission.setLastModifiedAt(LocalDateTime.now());
        return mapper.map(repository.save(submission));
    }

    @Override
    public List<SubmissionDto> setForStudentAndCourse(long studentId, long courseId, List<SubmissionDto> submissionDtoList) {
        Student student = getStudentById(studentId);
        Course course = getCourseById(courseId);
        LocalDateTime now = LocalDateTime.now();

        List<Submission> allSubmissions = repository.getAllByStudentAndCourse(student, course);
        Map<Long, Submission> taskToSubmission = allSubmissions
                .stream()
                .collect(Collectors.toMap(s -> s.getTask().getId(), s -> s));
        List<Criteria> allCriteria = criteriaService.findRawByCourseId(courseId);
        Map<Long, Criteria> idToCriteria = allCriteria
                .stream()
                .collect(Collectors.toMap(Criteria::getId, c -> c));
        // this map contains all criteriaDto for the given task id
        Map<Long, List<CriteriaDto>> taskToCriteriaList = allCriteria
                .stream()
                .map(mapper::map)
                .map(dto -> Pair.of(dto.getTask(), dto))
                .reduce(new HashMap<>(), (map, pair) -> {
                    long taskId = pair.getFirst();
                    map.computeIfAbsent(taskId, (l) -> new ArrayList<>()).add(pair.getSecond());
                    return map;
                }, (a, b) -> {
                    for (var item : b.entrySet()) {
                        a.computeIfAbsent(item.getKey(), (l) -> new ArrayList<>()).addAll(item.getValue());
                    }
                    return a;
                });
        Map<Long, Task> idToTask = taskService.findRawByCourseId(courseId)
                .stream()
                .collect(Collectors.toMap(Task::getId, t -> t));
        Map<Long, TaskDto> idToTaskDto = idToTask.entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> mapper.map(e.getValue())));

        List<Submission> toSave = new ArrayList<>();
        List<Submission> toDelete = new ArrayList<>(allSubmissions);

        for (SubmissionDto req : submissionDtoList) {
            Long taskId = req.getTask();
            Submission db = taskToSubmission.getOrDefault(taskId, new Submission());
            boolean existsInDb = db.getId() != null;

            mapper.put(req, db);

            if (!existsInDb) {
                db.setTask(idToTask.get(taskId));
                db.setStudent(student);
                db.setCreatedAt(now);
            }
            setSatisfiedCriteria(db, pickCriteria(idToCriteria, req.getSatisfiedCriteria()));
            db.setMainScore(scoringService.getMainScore(
                    mapper.map(db),
                    idToTaskDto.get(taskId),
                    taskToCriteriaList.get(taskId)
            ));
            db.setLastModifiedAt(now);

            if (existsInDb) {
                toDelete.remove(db);
            }
            toSave.add(db);
        }
        for (Submission submission : toDelete) {
            submission.getSatisfiedCriteria().forEach(c -> c.getSubmissionList().remove(submission));
            submission.getSatisfiedCriteria().clear();
        }

        repository.deleteAll(toDelete);
        return repository.saveAll(toSave).stream()
                .map(mapper::map)
                .toList();
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

        setSatisfiedCriteria(submission, satisfiedCriteria);
        submission.setMainScore(scoringService.getMainScore(
                mapper.map(submission),
                mapper.map(task),
                task.getCriteria().stream().map(mapper::map).toList()
        ));
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

        setSatisfiedCriteria(submission, satisfiedCriteria);
        submission.setMainScore(scoringService.getMainScore(
                mapper.map(submission),
                mapper.map(task),
                task.getCriteria().stream().map(mapper::map).toList()
        ));
        submission.setLastModifiedAt(LocalDateTime.now());
        return mapper.map(repository.save(submission));
    }

    @Override
    public void delete(long id) {
        Submission submission = getSubmissionById(id);
        submission.getSatisfiedCriteria().forEach(c -> c.getSubmissionList().remove(submission));
        submission.getSatisfiedCriteria().clear();
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
        return new ArrayList<>(rawById);
    }

    private void assertNoTaskChange(Task oldTask, Task newTask) {
        if (!Objects.equals(oldTask, newTask)) {
            throw new TransferNotAllowedException(Task.class, "task", oldTask.getId(), newTask.getId());
        }
    }

    private void assertNoStudentChange(Student oldStudent, Student newStudent) {
        if (!Objects.equals(oldStudent, newStudent)) {
            throw new TransferNotAllowedException(Student.class, "student", oldStudent.getId(), newStudent.getId());
        }
    }

    private void assertSameCourseAndTask(List<Criteria> satisfiedCriteria, Task task, Student student) {
        Set<Long> courseIds = new HashSet<>(
                satisfiedCriteria.stream()
                        .map(c -> c.getTask().getCourse().getId())
                        .toList()
        );
        courseIds.add(task.getCourse().getId());
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

    private void setSatisfiedCriteria(Submission submission, List<Criteria> satisfiedCriteria) {
        submission.setSatisfiedCriteria(satisfiedCriteria);
        satisfiedCriteria.forEach(c -> c.getSubmissionList().add(submission));
    }

    private List<Criteria> pickCriteria(Map<Long, Criteria> criteriaMap, List<Long> ids) {
        return ids.stream()
                .map(criteriaMap::get)
                .collect(Collectors.toList());
    }

    @Autowired
    public void setTaskService(TaskService taskService) {
        this.taskService = taskService;
    }

    @Autowired
    public void setCriteriaService(CriteriaService criteriaService) {
        this.criteriaService = criteriaService;
    }
}
