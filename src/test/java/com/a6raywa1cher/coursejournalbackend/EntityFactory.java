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

    private final CourseTokenService courseTokenService;

    private final EmployeeService employeeService;

    private final CriteriaService criteriaService;

    private final SubmissionService submissionService;

    private final Faker faker;

    private final MapStructTestMapper mapper;

    private final StudentService studentService;

    private final AttendanceService attendanceService;

    private final GroupService groupService;

    private final FacultyService facultyService;

    private final AuthUserService authUserService;

    @Autowired
    public EntityFactory(TaskService taskService, CourseService courseService, CourseTokenService courseTokenService, EmployeeService employeeService,
                         CriteriaService criteriaService, SubmissionService submissionService, Faker faker,
                         MapStructTestMapper mapper, StudentService studentService, AttendanceService attendanceService, GroupService groupService, FacultyService facultyService, AuthUserService authUserService) {
        this.taskService = taskService;
        this.courseService = courseService;
        this.courseTokenService = courseTokenService;
        this.employeeService = employeeService;
        this.criteriaService = criteriaService;
        this.submissionService = submissionService;
        this.faker = faker;
        this.mapper = mapper;
        this.studentService = studentService;
        this.attendanceService = attendanceService;
        this.groupService = groupService;
        this.facultyService = facultyService;
        this.authUserService = authUserService;
    }

    public long createAuthUser() {
        return createAuthUser(bag());
    }

    public long createAuthUser(EntityFactoryBag bag) {
        CreateEditAuthUserDto dto = CreateEditAuthUserDto.builder()
                .username(faker.name().username())
                .userRole(bag.getUserRole())
                .userInfo(bag.getUserInfoId())
                .build();

        CreateEditAuthUserDto dtoFromBag = bag.getDto(CreateEditAuthUserDto.class);
        if (dtoFromBag != null) {
            mapper.merge(dtoFromBag, dto);
        }

        return authUserService.create(dto).getId();
    }

    public Long createUserInfoId() {
        return createUserInfoId(bag());
    }

    public Long createUserInfoId(UserRole userRole) {
        return createUserInfoId(bag().withUserRole(userRole));
    }

    public Long createUserInfoId(EntityFactoryBag bag) {
        return switch (bag.getUserRole()) {
            case ADMIN -> null;
            case TEACHER -> bag.getEmployeeId();
            case HEADMAN -> bag.getStudentId();
        };
    }

    public long createEmployee() {
        return createEmployee(bag());
    }

    public long createEmployee(EntityFactoryBag bag) {
        EmployeeDto dto = EmployeeDto.builder()
                .firstName(faker.name().firstName())
                .lastName(faker.name().lastName())
                .build();

        EmployeeDto dtoFromBag = bag.getDto(EmployeeDto.class);
        if (dtoFromBag != null) {
            mapper.merge(dtoFromBag, dto);
        }

        return employeeService.create(dto).getId();
    }

    public long createCourse() {
        return createCourse(bag());
    }

    public long createCourse(Long userId) {
        return createCourse(bag().withEmployeeId(userId));
    }

    public long createCourse(EntityFactoryBag bag) {
        CourseDto dto = CourseDto.builder()
                .name(faker.lorem().sentence())
                .owner(bag.getEmployeeId())
                .build();

        CourseDto dtoFromBag = bag.getDto(CourseDto.class);
        if (dtoFromBag != null) {
            mapper.merge(dtoFromBag, dto);
        }

        return courseService.create(dto).getId();
    }

    public long createCourseToken() {
        return createCourseToken(bag());
    }

    public long createCourseToken(Long userId) {
        return createCourseToken(bag().withEmployeeId(userId));
    }

    public long createCourseToken(EntityFactoryBag bag) {
        CourseTokenDto dto = CourseTokenDto.builder()
                .course(bag.getCourseId())
                .build();

        CourseTokenDto dtoFromBag = bag.getDto(CourseTokenDto.class);
        if (dtoFromBag != null) {
            mapper.merge(dtoFromBag, dto);
        }

        return courseTokenService.create(dto).getId();
    }

    public long createTask() {
        return createTask(bag());
    }

    public long createTask(Long userId) {
        return createTask(bag().withEmployeeId(userId));
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
        return createStudent(bag().withEmployeeId(userId));
    }

    public long createStudent(EntityFactoryBag bag) {
        StudentDto dto = StudentDto.builder()
                .firstName(faker.name().firstName())
                .lastName(faker.name().lastName())
                .course(bag.getCourseId())
                .group(bag.getGroupId())
                .build();

        StudentDto dtoFromBag = bag.getDto(StudentDto.class);
        if (dtoFromBag != null) {
            mapper.merge(dtoFromBag, dto);
        }

        return studentService.create(dto).getId();
    }

    public long createAttendance() { return createAttendance(bag()); }

    public long createAttendance(Long userId) {
        return createAttendance(bag().withEmployeeId(userId));
    }

    public long createAttendance(EntityFactoryBag bag) {
        AttendanceDto dto = AttendanceDto.builder()
                .attendedClass(faker.number().numberBetween(1, 6))
                .attendanceType(TestUtils.randomAttendanceType())
                .course(bag.getCourseId())
                .student(bag.getStudentId())
                .build();


        AttendanceDto dtoFromBag = bag.getDto(AttendanceDto.class);
        if (dtoFromBag != null) {
            mapper.merge(dtoFromBag, dto);
        }

        return attendanceService.create(dto).getId();
    }

    public long createCriteria() {
        return createCriteria(bag());
    }

    public long createCriteria(Long userId) {
        return createCriteria(bag().withEmployeeId(userId));
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
        return createSubmission(bag().withEmployeeId(userId));
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

    public long createGroup() {
        return createGroup(bag());
    }

    public long createGroup(Long userId) {
        return createGroup(bag().withEmployeeId(userId));
    }

    public long createGroup(EntityFactoryBag bag) {
        GroupDto dto = GroupDto.builder()
                .name(faker.lorem().sentence(1))
                .faculty(bag.getFacultyId())
                .course(bag.getCourseId())
                .build();

        GroupDto dtoFromBag = bag.getDto(GroupDto.class);
        if (dtoFromBag != null) {
            mapper.merge(dtoFromBag, dto);
        }

        return groupService.create(dto).getId();
    }

    public long createFaculty() {
        return createFaculty(bag());
    }

    public long createFaculty(Long userId) {
        return createFaculty(bag().withEmployeeId(userId));
    }

    public long createFaculty(EntityFactoryBag bag) {
        FacultyDto dto = FacultyDto.builder()
                .name(faker.lorem().sentence(2))
                .build();

        FacultyDto dtoFromBag = bag.getDto(FacultyDto.class);
        if (dtoFromBag != null) {
            mapper.merge(dtoFromBag, dto);
        }

        return facultyService.create(dto).getId();
    }

    public EntityFactoryBag bag() {
        return new EntityFactoryBag(this);
    }

    private ZonedDateTime getFakedZonedDateTime(ZonedDateTime from, ZonedDateTime to) {
        Date fromDate = Date.from(from.toInstant());
        Date toDate = Date.from(to.toInstant());
        Date randomDate = faker.date().between(fromDate, toDate);
        return ZonedDateTime.ofInstant(randomDate.toInstant(), ZoneId.systemDefault());
    }

    @Data
    @With
    @Builder(access = AccessLevel.PRIVATE)
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class EntityFactoryBag {
        private final EntityFactory ef;

        private Long authUserId;

        private Long userInfoId;

        private UserRole userRole;

        private Long employeeId;

        private Long courseId;

        private Long courseTokenId;

        private Long taskId;

        private Long studentId;

        private Long attendanceId;

        private Long criteriaId;

        private Long submissionId;

        private Long facultyId;

        private Long groupId;

        private Object dto;

        public Long getAuthUserId() {
            if (authUserId == null) authUserId = ef.createAuthUser(this);
            return authUserId;
        }

        public UserRole getUserRole() {
            if (userRole == null) userRole = TestUtils.randomUserRole();
            return userRole;
        }

        public Long getUserInfoId() {
            if (userInfoId == null) userInfoId = ef.createUserInfoId(this);
            return userInfoId;
        }

        public Long getEmployeeId() {
            if (employeeId == null) employeeId = ef.createEmployee(this);
            return employeeId;
        }

        public Long getCourseId() {
            if (courseId == null) courseId = ef.createCourse(this);
            return courseId;
        }

        public Long getCourseTokenId() {
            if (courseTokenId == null) courseTokenId = ef.createCourseToken(this);
            return courseTokenId;
        }

        public Long getTaskId() {
            if (taskId == null) taskId = ef.createTask(this);
            return taskId;
        }

        public Long getStudentId() {
            if (studentId == null) studentId = ef.createStudent(this);
            return studentId;
        }

        public Long getAttendanceId() {
            if (attendanceId == null) attendanceId = ef.createAttendance(this);
            return attendanceId;
        }

        public Long getCriteriaId() {
            if (criteriaId == null) criteriaId = ef.createCriteria(this);
            return criteriaId;
        }

        public Long getSubmissionId() {
            if (submissionId == null) submissionId = ef.createSubmission(this);
            return submissionId;
        }

        public Long getFacultyId() {
            if (facultyId == null) facultyId = ef.createFaculty(this);
            return facultyId;
        }

        public Long getGroupId() {
            if (groupId == null) groupId = ef.createGroup(this);
            return groupId;
        }

        public <T> T getDto(Class<T> clazz) {
            if (dto == null) return null;
            return clazz.isAssignableFrom(dto.getClass()) ? clazz.cast(dto) : null;
        }
    }
}
