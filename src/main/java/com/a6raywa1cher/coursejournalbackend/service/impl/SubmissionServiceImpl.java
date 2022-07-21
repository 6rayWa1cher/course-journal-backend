package com.a6raywa1cher.coursejournalbackend.service.impl;

import com.a6raywa1cher.coursejournalbackend.dto.CriteriaDto;
import com.a6raywa1cher.coursejournalbackend.dto.SubmissionDto;
import com.a6raywa1cher.coursejournalbackend.dto.TaskDto;
import com.a6raywa1cher.coursejournalbackend.dto.exc.*;
import com.a6raywa1cher.coursejournalbackend.dto.mapper.MapStructMapper;
import com.a6raywa1cher.coursejournalbackend.model.*;
import com.a6raywa1cher.coursejournalbackend.model.repo.SubmissionRepository;
import com.a6raywa1cher.coursejournalbackend.service.*;
import com.a6raywa1cher.coursejournalbackend.utils.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
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
    public void recalculateMainScoreForTask(long taskId) {
        Task task = getTaskById(taskId);
        List<Submission> submissions = repository.getAllByTask(task, Sort.unsorted());
        List<CriteriaDto> criteria = criteriaService.getByTaskId(taskId, Sort.unsorted());
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
        assertSameCourseAndTask(satisfiedCriteria, task);
        mapper.put(dto, submission);

        submission.setTask(task);
        submission.setStudent(student);
        setSatisfiedCriteria(submission, satisfiedCriteria);
        submission.setMainScore(scoringService.getMainScore(
                mapper.map(submission),
                mapper.map(task),
                criteriaService.getByTaskId(task.getId(), Sort.unsorted())
        ));
        submission.setCreatedAt(LocalDateTime.now());
        submission.setLastModifiedAt(LocalDateTime.now());
        return mapper.map(repository.save(submission));
    }

    private SetForStudentAndCourseContext createSetForStudentAndCourseContext(Course course) {
        long courseId = course.getId();

        LocalDateTime now = LocalDateTime.now();
        List<Criteria> allCriteria = criteriaService.findRawByCourseId(courseId);
        Map<Long, Criteria> idToCriteria = allCriteria
                .stream()
                .collect(Collectors.toMap(Criteria::getId, c -> c));
        Map<Long, List<CriteriaDto>> taskToCriteriaList = allCriteria
                .stream()
                .map(mapper::map)
                .map(dto -> Pair.of(dto.getTask(), dto))
                .collect(Collectors.groupingBy(Pair::getFirst, Collectors.mapping(Pair::getSecond, Collectors.toList())));
        Map<Long, Task> idToTask = taskService.findRawByCourseId(courseId)
                .stream()
                .collect(Collectors.toMap(Task::getId, t -> t));
        Map<Long, TaskDto> idToTaskDto = idToTask.entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> mapper.map(e.getValue())));

        return new SetForStudentAndCourseContext(idToCriteria, taskToCriteriaList, idToTask, idToTaskDto, now);
    }

    private SetForStudentAndCourseResult $setForStudentAndCourse(
            Student student,
            List<Submission> studentSubmissions,
            List<SubmissionDto> studentSubmissionDtoList,
            SetForStudentAndCourseContext ctx
    ) {
        Map<Long, Criteria> idToCriteria = ctx.idToCriteria();
        Map<Long, List<CriteriaDto>> taskToCriteriaList = ctx.taskToCriteriaList();
        Map<Long, Task> idToTask = ctx.idToTask();
        Map<Long, TaskDto> idToTaskDto = ctx.idToTaskDto();
        LocalDateTime now = ctx.now();

        Map<Long, Submission> taskToSubmission = studentSubmissions
                .stream()
                .collect(Collectors.toMap(s -> s.getTask().getId(), s -> s));
        List<Submission> toSave = new ArrayList<>();
        List<Submission> toDelete = new ArrayList<>(studentSubmissions);

        for (SubmissionDto req : studentSubmissionDtoList) {
            Long taskId = req.getTask();
            Task task = idToTask.get(taskId);
            Submission db = taskToSubmission.getOrDefault(taskId, new Submission());
            boolean existsInDb = db.getId() != null;

            mapper.put(req, db);

            if (!existsInDb) {
                db.setTask(task);
                db.setStudent(student);
                db.setCreatedAt(now);
            }
            List<Criteria> satisfiedCriteria = pickCriteria(idToCriteria, req.getSatisfiedCriteria());
            assertSameCourseAndTask(satisfiedCriteria, task);
            setSatisfiedCriteria(db, satisfiedCriteria);
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
        return new SetForStudentAndCourseResult(toSave, toDelete);
    }

    @Override
    public List<SubmissionDto> setForStudentAndCourse(long studentId, long courseId, List<SubmissionDto> submissionDtoList) {
        Student student = getStudentById(studentId);
        Course course = getCourseById(courseId);
        assertStudentInCourse(student, course);

        List<Submission> allSubmissions = repository.getAllByStudentAndCourse(student, course, Sort.unsorted());
        SetForStudentAndCourseContext ctx = createSetForStudentAndCourseContext(course);

        SetForStudentAndCourseResult result = $setForStudentAndCourse(
                student, allSubmissions, submissionDtoList, ctx
        );

        List<Submission> toSave = result.toSave();
        List<Submission> toDelete = result.toDelete();

        repository.deleteAll(toDelete);
        return repository.saveAll(toSave).stream()
                .map(mapper::map)
                .toList();
    }

    @Override
    public List<SubmissionDto> setForCourse(long courseId, List<SubmissionDto> submissionDtoList) {
        Course course = getCourseById(courseId);

        SetForStudentAndCourseContext ctx = createSetForStudentAndCourseContext(course);

        List<Student> allStudents = studentService.getRawByCourseId(courseId);
        Map<Long, Student> idToStudent = allStudents.stream()
                .collect(Collectors.toMap(Student::getId, s -> s));

        Function<Long, Student> handleStudentAbsence = (k) -> {
            throw new StudentDoesntBelongToCourseException(k, course.getId());
        };

        Map<Student, List<SubmissionDto>> studentToRequestMap = submissionDtoList.stream()
                .collect(Collectors.groupingBy(s -> idToStudent.computeIfAbsent(s.getStudent(), handleStudentAbsence)));

        List<Submission> allSubmissions = repository.getAllByCourse(course, Sort.unsorted());
        Map<Student, List<Submission>> studentToDbSubmissionsMap = allSubmissions.stream()
                .collect(Collectors.groupingBy(s -> idToStudent.computeIfAbsent(s.getStudent().getId(), handleStudentAbsence)));

        List<Submission> toSave = new ArrayList<>();
        List<Submission> toDelete = new ArrayList<>();

        for (Student student : allStudents) {
            List<Submission> studentSubmissions = studentToDbSubmissionsMap.getOrDefault(student, Collections.emptyList());
            List<SubmissionDto> requestSubmissions = studentToRequestMap.getOrDefault(student, Collections.emptyList());

            SetForStudentAndCourseResult result = $setForStudentAndCourse(
                    student,
                    studentSubmissions,
                    requestSubmissions,
                    ctx
            );
            toSave.addAll(result.toSave());
            toDelete.addAll(result.toDelete());
        }

        repository.deleteAll(toDelete);
        return repository.saveAll(toSave).stream()
                .map(mapper::map)
                .toList();
    }

    private record SetForStudentAndCourseResult(List<Submission> toSave, List<Submission> toDelete) {
    }

    private record SetForStudentAndCourseContext(
            Map<Long, Criteria> idToCriteria,
            Map<Long, List<CriteriaDto>> taskToCriteriaList,
            Map<Long, Task> idToTask,
            Map<Long, TaskDto> idToTaskDto,
            LocalDateTime now) {
    }


    @Override
    public SubmissionDto update(long id, SubmissionDto dto) {
        Submission submission = getSubmissionById(id);
        Task task = getTaskById(dto.getTask());
        Student student = getStudentById(dto.getStudent());
        List<Criteria> satisfiedCriteria = getCriteriaListByIds(dto.getSatisfiedCriteria());

        assertNoTaskChange(submission.getTask(), task);
        assertNoStudentChange(submission.getStudent(), student);
        assertSameCourseAndTask(satisfiedCriteria, task);
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
        assertSameCourseAndTask(satisfiedCriteria, task);
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

    private void assertStudentInCourse(Student student, Course course) {
        if (!course.getStudents().contains(student)) {
            throw new StudentDoesntBelongToCourseException(student.getId(), course.getId());
        }
    }

    private void assertSameCourseAndTask(List<Criteria> satisfiedCriteria, Task task) {
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
        List<Criteria> originalList = submission.getSatisfiedCriteria();
        if (originalList.equals(satisfiedCriteria)) return;
        for (Criteria originalStudent : originalList) {
            if (!satisfiedCriteria.contains(originalStudent)) {
                originalStudent.getSubmissionList().remove(submission);
            }
        }
        for (Criteria newStudent : satisfiedCriteria) {
            if (!originalList.contains(newStudent)) {
                newStudent.getSubmissionList().add(submission);
            }
        }
        submission.setSatisfiedCriteria(satisfiedCriteria);
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
