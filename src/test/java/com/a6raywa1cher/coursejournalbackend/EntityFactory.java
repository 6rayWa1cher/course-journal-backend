package com.a6raywa1cher.coursejournalbackend;

import com.a6raywa1cher.coursejournalbackend.dto.CourseDto;
import com.a6raywa1cher.coursejournalbackend.dto.CreateEditUserDto;
import com.a6raywa1cher.coursejournalbackend.dto.TaskDto;
import com.a6raywa1cher.coursejournalbackend.model.UserRole;
import com.a6raywa1cher.coursejournalbackend.service.CourseService;
import com.a6raywa1cher.coursejournalbackend.service.CriteriaService;
import com.a6raywa1cher.coursejournalbackend.service.TaskService;
import com.a6raywa1cher.coursejournalbackend.service.UserService;
import com.github.javafaker.Faker;
import lombok.*;
import org.springframework.boot.test.context.TestComponent;

@TestComponent
public class EntityFactory {
    private final TaskService taskService;

    private final CourseService courseService;

    private final UserService userService;

    private final CriteriaService criteriaService;

    private final Faker faker;

    public EntityFactory(TaskService taskService, CourseService courseService, UserService userService, CriteriaService criteriaService, Faker faker) {
        this.taskService = taskService;
        this.courseService = courseService;
        this.userService = userService;
        this.criteriaService = criteriaService;
        this.faker = faker;
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
        return courseService.create(CourseDto.builder()
                .name(faker.lorem().sentence())
                .owner(bag.getUserId())
                .build()).getId();
    }

    public long createTask() {
        return createTask(bag());
    }

    public long createTask(Long userId) {
        return createTask(bag().withUserId(userId));
    }

    public long createTask(EntityFactoryBag bag) {
        return taskService.create(TaskDto.builder()
                .title(faker.lorem().sentence())
                .course(bag.getCourseId())
                .build()).getId();
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
    }
}
