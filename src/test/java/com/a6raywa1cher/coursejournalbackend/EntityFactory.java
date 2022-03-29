package com.a6raywa1cher.coursejournalbackend;

import com.a6raywa1cher.coursejournalbackend.dto.*;
import com.a6raywa1cher.coursejournalbackend.model.UserRole;
import com.a6raywa1cher.coursejournalbackend.service.*;
import com.github.javafaker.Faker;
import lombok.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestComponent;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.stream.Stream;

@TestComponent
public class EntityFactory {
    private final TaskService taskService;

    private final CourseService courseService;

    private final UserService userService;

    private final CriteriaService criteriaService;

    private final SubmissionService submissionService;

    private final Faker faker;

    private final MapStructTestMapper mapper;

    private final StudentService studentService;

    @Autowired
    public EntityFactory(TaskService taskService, CourseService courseService, UserService userService,
                         CriteriaService criteriaService, SubmissionService submissionService, Faker faker,
                         MapStructTestMapper mapper, StudentService studentService) {
        this.taskService = taskService;
        this.courseService = courseService;
        this.userService = userService;
        this.criteriaService = criteriaService;
        this.submissionService = submissionService;
        this.faker = faker;
        this.mapper = mapper;
        this.studentService = studentService;
    }

    public long createUser() {
        return userService.createUser(CreateEditUserDto.builder()
                .username(faker.name().username())
                .userRole(UserRole.TEACHER)
                .build()).getId();
    }

    public long createCourse() {
        return createCourse(bag());
    }

    public long createCourse(Long userId) {
        return createCourse(bag().withUserId(userId));
    }

    public long createCourse(EntityFactoryBag bag) {
        CourseDto dto = CourseDto.builder()
                .name(faker.lorem().sentence())
                .owner(bag.getUserId())
                .build();

        CourseDto dtoFromBag = bag.getDto(CourseDto.class);
        if (dtoFromBag != null) {
            mapper.merge(dtoFromBag, dto);
        }

        return courseService.create(dto).getId();
    }

    public long createTask() {
        return createTask(bag());
    }

    public long createTask(Long userId) {
        return createTask(bag().withUserId(userId));
    }

    public long createTask(EntityFactoryBag bag) {
        ZonedDateTime softDeadlineAt = ZonedDateTime.now();
        ZonedDateTime hardDeadlineAt = ZonedDateTime.now().plusDays(faker.number().numberBetween(1, 10));
        TaskDto dto = TaskDto.builder()
                .title(faker.lorem().sentence())
                .course(bag.getCourseId())
                .maxScore(faker.number().numberBetween(5, 10))
                .maxPenaltyPercent(faker.number().numberBetween(0, 95))
                .softDeadlineAt(softDeadlineAt)
                .hardDeadlineAt(hardDeadlineAt)
                .deadlinesEnabled(true)
                .build();

        TaskDto dtoFromBag = bag.getDto(TaskDto.class);
        if (dtoFromBag != null) {
            mapper.merge(dtoFromBag, dto);
        }

        return taskService.create(dto).getId();
    }

    public long createStudent() {
        return createStudent(bag());
    }

    public long createStudent(Long userId) {
        return createStudent(bag().withUserId(userId));
    }

    public long createStudent(EntityFactoryBag bag) {
        StudentDto dto = StudentDto.builder()
                .firstName(faker.name().firstName())
                .lastName(faker.name().lastName())
                .course(bag.getCourseId())
                .build();

        StudentDto dtoFromBag = bag.getDto(StudentDto.class);
        if (dtoFromBag != null) {
            mapper.merge(dtoFromBag, dto);
        }

        return studentService.create(dto).getId();
    }

    public long createCriteria() {
        return createCriteria(bag());
    }

    public long createCriteria(Long userId) {
        return createCriteria(bag().withUserId(userId));
    }

    public long createCriteria(EntityFactoryBag bag) {
        CriteriaDto dto = CriteriaDto.builder()
                .name(faker.lorem().sentence())
                .criteriaPercent(faker.number().numberBetween(20, 80))
                .task(bag.getTaskId())
                .build();

        CriteriaDto dtoFromBag = bag.getDto(CriteriaDto.class);
        if (dtoFromBag != null) {
            mapper.merge(dtoFromBag, dto);
        }

        return criteriaService.create(dto).getId();
    }

    public long createSubmission() {
        return createSubmission(bag());
    }

    public long createSubmission(Long userId) {
        return createSubmission(bag().withUserId(userId));
    }

    public long createSubmission(EntityFactoryBag bag) {
        SubmissionDto dto = SubmissionDto.builder()
                .submittedAt(getFakedZonedDateTime(ZonedDateTime.now().minusDays(15), ZonedDateTime.now().plusDays(15)))
                .additionalScore(faker.number().numberBetween(0, 5))
                .task(bag.getTaskId())
                .student(bag.getStudentId())
                .satisfiedCriteria(
                        Stream.generate(() -> createCriteria(bag))
                                .limit(faker.number().numberBetween(1, 5))
                                .toList())
                .build();

        SubmissionDto dtoFromBag = bag.getDto(SubmissionDto.class);
        if (dtoFromBag != null) {
            mapper.merge(dtoFromBag, dto);
        }

        return submissionService.create(dto).getId();
    }

    public EntityFactoryBag bag() {
        return new EntityFactoryBag(this);
    }

    @Data
    @With
    @Builder(access = AccessLevel.PRIVATE)
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class EntityFactoryBag {
        private final EntityFactory ef;

        private Long userId;

        private Long courseId;

        private Long taskId;

        private Long studentId;

        private Long criteriaId;

        private Long submissionId;

        private Object dto;

        public Long getUserId() {
            if (userId == null) userId = ef.createUser();
            return userId;
        }

        public Long getCourseId() {
            if (courseId == null) courseId = ef.createCourse(this);
            return courseId;
        }

        public Long getTaskId() {
            if (taskId == null) taskId = ef.createTask(this);
            return taskId;
        }

        public Long getStudentId() {
            if (studentId == null) studentId = ef.createStudent(this);
            return studentId;
        }

        public Long getCriteriaId() {
            if (criteriaId == null) criteriaId = ef.createCriteria(this);
            return criteriaId;
        }

        public Long getSubmissionId() {
            if (submissionId == null) submissionId = ef.createSubmission(this);
            return submissionId;
        }

        public <T> T getDto(Class<T> clazz) {
            if (dto == null) return null;
            return clazz.isAssignableFrom(dto.getClass()) ? clazz.cast(dto) : null;
        }
    }

    private ZonedDateTime getFakedZonedDateTime(ZonedDateTime from, ZonedDateTime to) {
        Date fromDate = Date.from(from.toInstant());
        Date toDate = Date.from(to.toInstant());
        Date randomDate = faker.date().between(fromDate, toDate);
        return ZonedDateTime.ofInstant(randomDate.toInstant(), ZoneId.systemDefault());
    }
}
