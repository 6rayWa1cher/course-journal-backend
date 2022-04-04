package com.a6raywa1cher.coursejournalbackend;

import com.a6raywa1cher.coursejournalbackend.dto.*;
import com.a6raywa1cher.coursejournalbackend.model.AttendanceType;
import com.a6raywa1cher.coursejournalbackend.model.UserRole;
import com.a6raywa1cher.coursejournalbackend.service.*;
import com.github.javafaker.Faker;
import lombok.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestComponent;

import java.time.ZonedDateTime;

@TestComponent
public class EntityFactory {
    private final TaskService taskService;

    private final CourseService courseService;

    private final UserService userService;

    private final CriteriaService criteriaService;

    private final Faker faker;

    private final MapStructTestMapper mapper;

    private final StudentService studentService;

    private final AttendanceService attendanceService;

    @Autowired
    public EntityFactory(TaskService taskService, CourseService courseService, UserService userService, CriteriaService criteriaService, Faker faker, MapStructTestMapper mapper, StudentService studentService, AttendanceService attendanceService) {
        this.taskService = taskService;
        this.courseService = courseService;
        this.userService = userService;
        this.criteriaService = criteriaService;
        this.faker = faker;
        this.mapper = mapper;
        this.studentService = studentService;
        this.attendanceService = attendanceService;
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
        TaskDto dto = TaskDto.builder()
                .title(faker.lorem().sentence())
                .course(bag.getCourseId())
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

    public long createAttendance() { return createAttendance(bag()); }

    public long createAttendance(Long attendanceId) { return createAttendance(bag().withAttendanceId(attendanceId)); }

    public long createAttendance(EntityFactoryBag bag) {
        AttendanceDto dto = AttendanceDto.builder()
                .attendedAt(ZonedDateTime.now())
                .attendanceType(TestUtils.randomAttendanceType())
                .course(bag.getCourseId())
                .student(bag.getStudentId())
                .build();


        AttendanceDto dtoFromBag = bag().getDto(AttendanceDto.class);
        if (dtoFromBag != null) {
            mapper.merge(dtoFromBag, dto);
        }

        return attendanceService.create(dto).getId();
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

        private Long attendanceId;

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

        public Long getAttendanceId() {
            if (attendanceId == null) attendanceId = ef.createAttendance(this);
            return attendanceId;
        }

        public <T> T getDto(Class<T> clazz) {
            if (dto == null) return null;
            return clazz.isAssignableFrom(dto.getClass()) ? clazz.cast(dto) : null;
        }
    }
}
