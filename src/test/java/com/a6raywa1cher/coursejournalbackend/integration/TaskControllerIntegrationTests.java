package com.a6raywa1cher.coursejournalbackend.integration;

import com.a6raywa1cher.coursejournalbackend.TestUtils;
import com.a6raywa1cher.coursejournalbackend.dto.TaskDto;
import com.a6raywa1cher.coursejournalbackend.service.CourseService;
import com.a6raywa1cher.coursejournalbackend.service.TaskService;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.ResultMatcher;

import java.time.ZonedDateTime;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
public class TaskControllerIntegrationTests extends AbstractIntegrationTests {
    @Autowired
    TaskService taskService;

    @Autowired
    CourseService courseService;

    @Test
    void getTasksByCourse__self__valid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                String sentence1 = faker.lorem().sentence();
                String sentence2 = faker.lorem().sentence();

                long courseId1 = ef.createCourse(getSelfEmployeeIdAsLong());
                long courseId2 = ef.createCourse(ef.createEmployee());

                taskService.create(TaskDto.builder()
                        .title(sentence1)
                        .course(courseId1)
                        .build());

                taskService.create(TaskDto.builder()
                        .title(sentence2)
                        .course(courseId1)
                        .build());

                taskService.create(TaskDto.builder()
                        .title(sentence1)
                        .course(courseId2)
                        .build());

                securePerform(get("/tasks/course/{id}", courseId1).queryParam("sort", "id,desc"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.totalElements").value(2))
                        .andExpect(jsonPath("$.content[0].title").value(sentence2))
                        .andExpect(jsonPath("$.content[1].title").value(sentence1));
            }
        };
    }

    @Test
    void getTasksByCourse__otherAsAdmin__valid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                String sentence1 = faker.lorem().sentence();
                String sentence2 = faker.lorem().sentence();

                long courseId = ef.createCourse(ef.createEmployee());

                taskService.create(TaskDto.builder()
                        .title(sentence1)
                        .course(courseId)
                        .build());

                taskService.create(TaskDto.builder()
                        .title(sentence2)
                        .course(courseId)
                        .build());

                securePerform(get("/tasks/course/{id}", courseId).queryParam("sort", "id,desc"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.totalElements").value(2))
                        .andExpect(jsonPath("$.content[0].title").value(sentence2))
                        .andExpect(jsonPath("$.content[1].title").value(sentence1));
            }
        };
    }

    @Test
    void getTasksByCourse__otherAsTeacher__invalid() {
        long id = ef.createCourse(ef.createEmployee());

        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                securePerform(get("/tasks/course/{id}", id).queryParam("sort", "id,desc"))
                        .andExpect(status().isForbidden());
            }
        };
    }

    @Test
    void getTasksByCourse__withCourseToken__valid() {
        new WithCourseToken() {
            @Override
            void run() throws Exception {
                String sentence1 = faker.lorem().sentence();
                String sentence2 = faker.lorem().sentence();

                long courseId1 = getCourseId();
                long courseId2 = ef.createCourse(getOwnerId());

                taskService.create(TaskDto.builder()
                        .title(sentence1)
                        .course(courseId1)
                        .build());

                taskService.create(TaskDto.builder()
                        .title(sentence2)
                        .course(courseId1)
                        .build());

                taskService.create(TaskDto.builder()
                        .title(sentence1)
                        .course(courseId2)
                        .build());

                securePerform(get("/tasks/course/{id}", courseId1).queryParam("sort", "id,desc"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.totalElements").value(2))
                        .andExpect(jsonPath("$.content[0].title").value(sentence2))
                        .andExpect(jsonPath("$.content[1].title").value(sentence1));
            }
        };
    }

    @Test
    void getTasksByCourse__notAuthenticated__invalid() throws Exception {
        long id = ef.createCourse(ef.createEmployee());
        mvc.perform(get("/tasks/course/{id}", id)).andExpect(status().isUnauthorized());
    }

    // ================================================================================================================

    @Test
    void getTasksByCourseNotPaged__self__valid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                String sentence1 = faker.lorem().sentence();
                String sentence2 = faker.lorem().sentence();

                long courseId1 = ef.createCourse(getSelfEmployeeIdAsLong());
                long courseId2 = ef.createCourse(ef.createEmployee());

                taskService.create(TaskDto.builder()
                        .title(sentence1)
                        .taskNumber(2)
                        .course(courseId1)
                        .build());

                taskService.create(TaskDto.builder()
                        .title(sentence2)
                        .taskNumber(1)
                        .course(courseId1)
                        .build());

                taskService.create(TaskDto.builder()
                        .title(sentence1)
                        .course(courseId2)
                        .build());

                securePerform(get("/tasks/course/{id}/all", courseId1))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$[0].title").value(sentence2))
                        .andExpect(jsonPath("$[1].title").value(sentence1));
            }
        };
    }

    @Test
    void getTasksByCourseNotPaged__otherAsAdmin__valid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                String sentence1 = faker.lorem().sentence();
                String sentence2 = faker.lorem().sentence();

                long courseId1 = ef.createCourse(ef.createEmployee());
                long courseId2 = ef.createCourse(ef.createEmployee());

                taskService.create(TaskDto.builder()
                        .title(sentence1)
                        .taskNumber(2)
                        .course(courseId1)
                        .build());

                taskService.create(TaskDto.builder()
                        .title(sentence2)
                        .taskNumber(1)
                        .course(courseId1)
                        .build());

                taskService.create(TaskDto.builder()
                        .title(sentence1)
                        .course(courseId2)
                        .build());

                securePerform(get("/tasks/course/{id}/all", courseId1))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$[0].title").value(sentence2))
                        .andExpect(jsonPath("$[1].title").value(sentence1));
            }
        };
    }

    @Test
    void getTasksByCourseNotPaged__otherAsTeacher__invalid() {
        long id = ef.createCourse(ef.createEmployee());

        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                securePerform(get("/tasks/course/{id}/all", id).queryParam("sort", "id,desc"))
                        .andExpect(status().isForbidden());
            }
        };
    }

    @Test
    void getTasksByCourseNotPaged__withCourseToken__valid() {
        new WithCourseToken() {
            @Override
            void run() throws Exception {
                String sentence1 = faker.lorem().sentence();
                String sentence2 = faker.lorem().sentence();

                long courseId1 = getCourseId();
                long courseId2 = ef.createCourse(getOwnerId());

                taskService.create(TaskDto.builder()
                        .title(sentence1)
                        .taskNumber(2)
                        .course(courseId1)
                        .build());

                taskService.create(TaskDto.builder()
                        .title(sentence2)
                        .taskNumber(1)
                        .course(courseId1)
                        .build());

                taskService.create(TaskDto.builder()
                        .title(sentence1)
                        .course(courseId2)
                        .build());

                securePerform(get("/tasks/course/{id}/all", courseId1))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$[0].title").value(sentence2))
                        .andExpect(jsonPath("$[1].title").value(sentence1));
            }
        };
    }

    @Test
    void getTasksByCourseNotPaged__notAuthenticated__invalid() throws Exception {
        long id = ef.createCourse(ef.createEmployee());
        mvc.perform(get("/tasks/course/{id}/all", id)).andExpect(status().isUnauthorized());
    }

    // ================================================================================================================

    @Test
    void getTaskById__self__valid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                String title = faker.lorem().sentence();
                long id = taskService.create(TaskDto.builder()
                        .title(title)
                        .taskNumber(2)
                        .course(ef.createCourse(getSelfEmployeeIdAsLong()))
                        .build()).getId();

                securePerform(get("/tasks/{id}", id))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.title").value(title));
            }
        };
    }

    @Test
    void getTaskById__otherAsAdmin__valid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                String title = faker.lorem().sentence();
                long id = taskService.create(TaskDto.builder()
                        .title(title)
                        .taskNumber(2)
                        .course(ef.createCourse(ef.createEmployee()))
                        .build()).getId();

                securePerform(get("/tasks/{id}", id))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.title").value(title));
            }
        };
    }

    @Test
    void getTaskById__otherAsTeacher__invalid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                String title = faker.lorem().sentence();
                long id = taskService.create(TaskDto.builder()
                        .title(title)
                        .taskNumber(2)
                        .course(ef.createCourse(ef.createEmployee()))
                        .build()).getId();

                securePerform(get("/tasks/{id}", id))
                        .andExpect(status().isForbidden());
            }
        };
    }

    @Test
    void getTaskById__withCourseToken__valid() {
        new WithCourseToken() {
            @Override
            void run() throws Exception {
                String title = faker.lorem().sentence();
                long id = taskService.create(TaskDto.builder()
                        .title(title)
                        .taskNumber(2)
                        .course(getCourseId())
                        .build()).getId();

                securePerform(get("/tasks/{id}", id))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.title").value(title));
            }
        };
    }

    @Test
    void getTaskById__notAuthenticated__invalid() throws Exception {
        String title = faker.lorem().sentence();
        long id = taskService.create(TaskDto.builder()
                .title(title)
                .taskNumber(2)
                .course(ef.createCourse(ef.createEmployee()))
                .build()).getId();

        mvc.perform(get("/tasks/{id}", id))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getTaskById__notExists__invalid() {
        String title = faker.lorem().sentence();
        long id = taskService.create(TaskDto.builder()
                .title(title)
                .taskNumber(2)
                .course(ef.createCourse(ef.createEmployee()))
                .build()).getId();

        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                securePerform(get("/tasks/{id}", id + 1000))
                        .andExpect(status().isNotFound());
            }
        };
    }

    // ================================================================================================================

    @Test
    void createTask__self__valid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                String title = faker.lorem().sentence();
                long courseId = ef.createCourse(getSelfEmployeeIdAsLong());

                securePerform(post("/tasks/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.createObjectNode()
                                .put("title", title)
                                .put("course", courseId)
                                .toString()))
                        .andExpect(status().isCreated())
                        .andExpectAll(
                                jsonPath("$.id").isNumber(),
                                jsonPath("$.title").value(title),
                                jsonPath("$.course").value(courseId)
                        );

                securePerform(get("/tasks/course/{id}", courseId))
                        .andExpect(status().isOk())
                        .andExpect(content().string(containsString(title)));
            }
        };
    }

    @Test
    void createTask__otherAsAdmin__valid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                String title = faker.lorem().sentence();
                long courseId = ef.createCourse(ef.createEmployee());

                securePerform(post("/tasks/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.createObjectNode()
                                .put("title", title)
                                .put("course", courseId)
                                .toString()))
                        .andExpect(status().isCreated())
                        .andExpectAll(
                                jsonPath("$.id").isNumber(),
                                jsonPath("$.title").value(title),
                                jsonPath("$.course").value(courseId)
                        );

                securePerform(get("/tasks/course/{id}", courseId))
                        .andExpect(status().isOk())
                        .andExpect(content().string(containsString(title)));
            }
        };
    }

    @Test
    void createTask__otherAsTeacher__invalid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                String title = faker.lorem().sentence();
                long courseId = ef.createCourse(ef.createEmployee());

                securePerform(post("/tasks/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.createObjectNode()
                                .put("title", title)
                                .put("course", courseId)
                                .toString()))
                        .andExpect(status().isForbidden());
            }
        };
    }

    @Test
    void createTask__withCourseToken__invalid() {
        new WithCourseToken() {
            @Override
            void run() throws Exception {
                String title = faker.lorem().sentence();
                long courseId = getCourseId();

                securePerform(post("/tasks/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.createObjectNode()
                                .put("title", title)
                                .put("course", courseId)
                                .toString()))
                        .andExpect(status().isForbidden());
            }
        };
    }

    @Test
    void createTask__conflictingTaskNumber__invalid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                String title1 = faker.lorem().sentence();
                String title2 = faker.lorem().sentence();

                long courseId = ef.createCourse(getSelfEmployeeIdAsLong());

                taskService.create(TaskDto.builder()
                        .title(title1)
                        .taskNumber(1)
                        .course(courseId)
                        .build());

                securePerform(post("/tasks/", courseId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.createObjectNode()
                                .put("title", title2)
                                .put("taskNumber", 1)
                                .put("course", courseId)
                                .toString()))
                        .andExpect(status().isConflict());
            }
        };
    }

    @Test
    void createTask__deadlinesMixedNullsAndEnabled__invalid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                String title1 = faker.lorem().sentence();
                String title2 = faker.lorem().sentence();

                long courseId = ef.createCourse(getSelfEmployeeIdAsLong());

                taskService.create(TaskDto.builder()
                        .title(title1)
                        .taskNumber(1)
                        .course(courseId)
                        .build());

                securePerform(post("/tasks/", courseId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.createObjectNode()
                                .put("title", title2)
                                .put("taskNumber", 2)
                                .put("course", courseId)
                                .put("hardDeadlineAt", ZonedDateTime.now().plusDays(5).toString())
                                .put("deadlinesEnabled", true)
                                .toString()))
                        .andExpect(status().isBadRequest());
            }
        };
    }

    @Test
    void createTask__deadlinesNotMixedNullsAndEnabled__valid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                String title1 = faker.lorem().sentence();
                String title2 = faker.lorem().sentence();

                long courseId = ef.createCourse(getSelfEmployeeIdAsLong());

                taskService.create(TaskDto.builder()
                        .title(title1)
                        .taskNumber(1)
                        .course(courseId)
                        .build());

                securePerform(post("/tasks/", courseId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.createObjectNode()
                                .put("title", title2)
                                .put("taskNumber", 1)
                                .put("course", courseId)
                                .put("hardDeadlineAt", ZonedDateTime.now().plusDays(5).toString())
                                .put("softDeadlineAt", ZonedDateTime.now().plusDays(2).toString())
                                .put("deadlinesEnabled", true)
                                .toString()))
                        .andExpect(status().isConflict());
            }
        };
    }

    @Test
    void createTask__deadlinesMixedNullsAndDisabled__valid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                String title = faker.lorem().sentence();
                long courseId = ef.createCourse(getSelfEmployeeIdAsLong());
                ZonedDateTime softDeadlineAt = ZonedDateTime.now().plusDays(2);

                securePerform(post("/tasks/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.createObjectNode()
                                .put("title", title)
                                .put("course", courseId)
                                .put("softDeadlineAt", softDeadlineAt.toString())
                                .put("deadlinesEnabled", false)
                                .toString()))
                        .andExpect(status().isCreated())
                        .andExpectAll(
                                jsonPath("$.id").isNumber(),
                                jsonPath("$.title").value(title),
                                jsonPath("$.course").value(courseId),
                                jsonPath("$.deadlinesEnabled").value(false),
                                jsonPath("$.softDeadlineAt").value(new TestUtils.DateMatcher(softDeadlineAt))

                        );

                securePerform(get("/tasks/course/{id}", courseId))
                        .andExpect(status().isOk())
                        .andExpect(content().string(containsString(title)));
            }
        };
    }

    @Test
    void createTask__notAuthenticated__invalid() throws Exception {
        mvc.perform(post("/tasks/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.createObjectNode()
                                .put("title", faker.lorem().sentence())
                                .put("course", ef.createCourse(ef.createEmployee()))
                                .toString()))
                .andExpect(status().isUnauthorized());
    }

    // ================================================================================================================

    @Test
    void reorderTasks__self__valid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                String sentence1 = faker.lorem().sentence();
                String sentence2 = faker.lorem().sentence();

                long courseId = ef.createCourse(getSelfEmployeeIdAsLong());

                long task1 = taskService.create(TaskDto.builder()
                        .title(sentence1)
                        .course(courseId)
                        .build()).getId();

                long task2 = taskService.create(TaskDto.builder()
                        .title(sentence2)
                        .course(courseId)
                        .build()).getId();

                ObjectNode request = objectMapper.createObjectNode();
                request.putArray("order")
                        .add(objectMapper.createObjectNode()
                                .put("id", task1)
                                .put("number", 2))
                        .add(objectMapper.createObjectNode()
                                .put("id", task2)
                                .put("number", 1));

                securePerform(post("/tasks/course/{id}/reorder", courseId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isOk());

                securePerform(get("/tasks/{id}", task2))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.taskNumber").value(1));

                securePerform(get("/tasks/{id}", task1))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.taskNumber").value(2));
            }
        };
    }

    @Test
    void reorderTasks__tasksFromDifferentCourses__invalid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                String sentence1 = faker.lorem().sentence();
                String sentence2 = faker.lorem().sentence();

                long courseId1 = ef.createCourse(getSelfEmployeeIdAsLong());
                long courseId2 = ef.createCourse(getSelfEmployeeIdAsLong());

                long task1 = taskService.create(TaskDto.builder()
                        .title(sentence1)
                        .course(courseId1)
                        .build()).getId();

                long task2 = taskService.create(TaskDto.builder()
                        .title(sentence2)
                        .course(courseId2)
                        .build()).getId();


                ObjectNode request = objectMapper.createObjectNode();
                request.putArray("order")
                        .add(objectMapper.createObjectNode()
                                .put("id", task1)
                                .put("number", 2))
                        .add(objectMapper.createObjectNode()
                                .put("id", task2)
                                .put("number", 1));

                securePerform(post("/tasks/course/{id}/reorder", courseId1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isBadRequest());
            }
        };
    }

    @Test
    void reorderTasks__otherAsAdmin__valid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                String sentence1 = faker.lorem().sentence();
                String sentence2 = faker.lorem().sentence();

                long courseId = ef.createCourse(ef.createEmployee());

                long task1 = taskService.create(TaskDto.builder()
                        .title(sentence1)
                        .course(courseId)
                        .build()).getId();

                long task2 = taskService.create(TaskDto.builder()
                        .title(sentence2)
                        .course(courseId)
                        .build()).getId();

                ObjectNode request = objectMapper.createObjectNode();
                request.putArray("order")
                        .add(objectMapper.createObjectNode()
                                .put("id", task1)
                                .put("number", 2))
                        .add(objectMapper.createObjectNode()
                                .put("id", task2)
                                .put("number", 1));

                securePerform(post("/tasks/course/{id}/reorder", courseId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isOk());

                securePerform(get("/tasks/{id}", task2))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.taskNumber").value(1));

                securePerform(get("/tasks/{id}", task1))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.taskNumber").value(2));
            }
        };
    }

    @Test
    void reorderTasks__otherAsTeacher__invalid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                String sentence1 = faker.lorem().sentence();
                String sentence2 = faker.lorem().sentence();

                long courseId = ef.createCourse(ef.createEmployee());

                long task1 = taskService.create(TaskDto.builder()
                        .title(sentence1)
                        .course(courseId)
                        .build()).getId();

                long task2 = taskService.create(TaskDto.builder()
                        .title(sentence2)
                        .course(courseId)
                        .build()).getId();

                ObjectNode request = objectMapper.createObjectNode();
                request.putArray("order")
                        .add(objectMapper.createObjectNode()
                                .put("id", task1)
                                .put("number", 2))
                        .add(objectMapper.createObjectNode()
                                .put("id", task2)
                                .put("number", 1));

                securePerform(post("/tasks/course/{id}/reorder", courseId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isForbidden());
            }
        };
    }

    @Test
    void reorderTasks__withCourseToken__invalid() {
        new WithCourseToken() {
            @Override
            void run() throws Exception {
                String sentence1 = faker.lorem().sentence();
                String sentence2 = faker.lorem().sentence();

                long courseId = getCourseId();

                long task1 = taskService.create(TaskDto.builder()
                        .title(sentence1)
                        .course(courseId)
                        .build()).getId();

                long task2 = taskService.create(TaskDto.builder()
                        .title(sentence2)
                        .course(courseId)
                        .build()).getId();

                ObjectNode request = objectMapper.createObjectNode();
                request.putArray("order")
                        .add(objectMapper.createObjectNode()
                                .put("id", task1)
                                .put("number", 2))
                        .add(objectMapper.createObjectNode()
                                .put("id", task2)
                                .put("number", 1));

                securePerform(post("/tasks/course/{id}/reorder", courseId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isForbidden());
            }
        };
    }

    @Test
    void reorderTasks__conflictingTaskNumber__withinList__invalid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                String sentence1 = faker.lorem().sentence();
                String sentence2 = faker.lorem().sentence();

                long courseId = ef.createCourse(ef.createEmployee());

                long task1 = taskService.create(TaskDto.builder()
                        .title(sentence1)
                        .course(courseId)
                        .build()).getId();

                long task2 = taskService.create(TaskDto.builder()
                        .title(sentence2)
                        .course(courseId)
                        .build()).getId();

                ObjectNode request = objectMapper.createObjectNode();
                request.putArray("order")
                        .add(objectMapper.createObjectNode()
                                .put("id", task1)
                                .put("number", 1))
                        .add(objectMapper.createObjectNode()
                                .put("id", task2)
                                .put("number", 1));

                securePerform(post("/tasks/course/{id}/reorder", courseId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isBadRequest());
            }
        };
    }

    @Test
    void reorderTasks__conflictingTaskNumber__withinCourse__invalid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                String sentence1 = faker.lorem().sentence();
                String sentence2 = faker.lorem().sentence();

                long courseId = ef.createCourse(ef.createEmployee());
                int taskNumber = 2;

                long task1 = taskService.create(TaskDto.builder()
                        .title(sentence1)
                        .taskNumber(1)
                        .course(courseId)
                        .build()).getId();

                taskService.create(TaskDto.builder()
                        .title(sentence2)
                        .taskNumber(taskNumber)
                        .course(courseId)
                        .build());

                ObjectNode request = objectMapper.createObjectNode();
                request.putArray("order")
                        .add(objectMapper.createObjectNode()
                                .put("id", task1)
                                .put("number", taskNumber));

                securePerform(post("/tasks/course/{id}/reorder", courseId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isConflict());
            }
        };
    }

    @Test
    void reorderTasks__missingTasks__invalid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                String sentence1 = faker.lorem().sentence();
                String sentence2 = faker.lorem().sentence();

                long courseId1 = ef.createCourse(getSelfEmployeeIdAsLong());
                long courseId2 = ef.createCourse(getSelfEmployeeIdAsLong());

                long task1 = taskService.create(TaskDto.builder()
                        .title(sentence1)
                        .course(courseId1)
                        .build()).getId();

                long task2 = taskService.create(TaskDto.builder()
                        .title(sentence2)
                        .course(courseId2)
                        .build()).getId();

                ObjectNode request = objectMapper.createObjectNode();
                request.putArray("order")
                        .add(objectMapper.createObjectNode()
                                .put("id", task1)
                                .put("number", 2))
                        .add(objectMapper.createObjectNode()
                                .put("id", task2)
                                .put("number", 1))
                        .add(objectMapper.createObjectNode()
                                .put("id", task2 + 1000)
                                .put("number", 3));

                securePerform(post("/tasks/course/{id}/reorder", courseId1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isNotFound());
            }
        };
    }

    @Test
    void reorderTasks__notAuthenticated__invalid() throws Exception {
        String sentence1 = faker.lorem().sentence();
        String sentence2 = faker.lorem().sentence();

        long courseId = ef.createCourse(ef.createEmployee());

        long task1 = taskService.create(TaskDto.builder()
                .title(sentence1)
                .taskNumber(1)
                .course(courseId)
                .build()).getId();

        taskService.create(TaskDto.builder()
                .title(sentence2)
                .taskNumber(2)
                .course(courseId)
                .build());

        ObjectNode request = objectMapper.createObjectNode();
        request.putArray("order")
                .add(objectMapper.createObjectNode()
                        .put("id", task1)
                        .put("number", 2));

        mvc.perform(post("/tasks/course/{id}/reorder", courseId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                .andExpect(status().isUnauthorized());
    }

    // ================================================================================================================

    @Test
    void putTask__self__valid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                long courseId = ef.createCourse(getSelfEmployeeIdAsLong());
                int taskNumber = 1;
                String title = faker.lorem().sentence();
                String description = faker.lorem().paragraph();
                int maxScore = 150;
                int maxPenaltyPercent = 60;
                boolean announced = true;
                boolean deadlinesEnabled = true;
                ZonedDateTime announcementAt = ZonedDateTime.now().minusSeconds(5);
                ZonedDateTime softDeadlineAt = ZonedDateTime.now().plusDays(15);
                ZonedDateTime hardDeadlineAt = ZonedDateTime.now().plusDays(30);

                String prevTitle = faker.lorem().sentence();
                String prevDescription = faker.lorem().paragraph();

                long taskId = taskService.create(TaskDto.builder()
                        .title(prevTitle)
                        .description(prevDescription)
                        .course(courseId)
                        .build()).getId();

                ResultMatcher[] resultMatchers = {
                        jsonPath("$.id").value(taskId),
                        jsonPath("$.course").value(courseId),
                        jsonPath("$.taskNumber").value(taskNumber),
                        jsonPath("$.title").value(title),
                        jsonPath("$.description").value(description),
                        jsonPath("$.maxScore").value(maxScore),
                        jsonPath("$.maxPenaltyPercent").value(maxPenaltyPercent),
                        jsonPath("$.announced").value(announced),
                        jsonPath("$.deadlinesEnabled").value(deadlinesEnabled),
                        jsonPath("$.announcementAt", new TestUtils.DateMatcher(announcementAt)),
                        jsonPath("$.softDeadlineAt", new TestUtils.DateMatcher(softDeadlineAt)),
                        jsonPath("$.hardDeadlineAt", new TestUtils.DateMatcher(hardDeadlineAt)),
                };

                securePerform(put("/tasks/{id}", taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.createObjectNode()
                                .put("course", courseId)
                                .put("taskNumber", taskNumber)
                                .put("title", title)
                                .put("description", description)
                                .put("maxScore", maxScore)
                                .put("maxPenaltyPercent", maxPenaltyPercent)
                                .put("announced", announced)
                                .put("deadlinesEnabled", deadlinesEnabled)
                                .put("announcementAt", announcementAt.toString())
                                .put("softDeadlineAt", softDeadlineAt.toString())
                                .put("hardDeadlineAt", hardDeadlineAt.toString())
                                .toString()))
                        .andExpect(status().isOk())
                        .andExpectAll(resultMatchers);


                securePerform(get("/tasks/{id}", taskId))
                        .andExpect(status().isOk())
                        .andExpectAll(resultMatchers);
            }
        };
    }

    @Test
    void putTask__otherAsAdmin__valid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                long courseId = ef.createCourse(ef.createEmployee());
                int taskNumber = 1;
                String title = faker.lorem().sentence();
                String description = faker.lorem().paragraph();
                int maxScore = 150;
                int maxPenaltyPercent = 60;
                boolean announced = true;
                boolean deadlinesEnabled = true;
                ZonedDateTime announcementAt = ZonedDateTime.now().minusSeconds(5);
                ZonedDateTime softDeadlineAt = ZonedDateTime.now().plusDays(15);
                ZonedDateTime hardDeadlineAt = ZonedDateTime.now().plusDays(30);

                String prevTitle = faker.lorem().sentence();
                String prevDescription = faker.lorem().paragraph();

                long taskId = taskService.create(TaskDto.builder()
                        .title(prevTitle)
                        .description(prevDescription)
                        .course(courseId)
                        .build()).getId();

                ResultMatcher[] resultMatchers = {
                        jsonPath("$.id").value(taskId),
                        jsonPath("$.course").value(courseId),
                        jsonPath("$.taskNumber").value(taskNumber),
                        jsonPath("$.title").value(title),
                        jsonPath("$.description").value(description),
                        jsonPath("$.maxScore").value(maxScore),
                        jsonPath("$.maxPenaltyPercent").value(maxPenaltyPercent),
                        jsonPath("$.announced").value(announced),
                        jsonPath("$.deadlinesEnabled").value(deadlinesEnabled),
                        jsonPath("$.announcementAt", new TestUtils.DateMatcher(announcementAt)),
                        jsonPath("$.softDeadlineAt", new TestUtils.DateMatcher(softDeadlineAt)),
                        jsonPath("$.hardDeadlineAt", new TestUtils.DateMatcher(hardDeadlineAt)),
                };

                securePerform(put("/tasks/{id}", taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.createObjectNode()
                                .put("course", courseId)
                                .put("taskNumber", taskNumber)
                                .put("title", title)
                                .put("description", description)
                                .put("maxScore", maxScore)
                                .put("maxPenaltyPercent", maxPenaltyPercent)
                                .put("announced", announced)
                                .put("deadlinesEnabled", deadlinesEnabled)
                                .put("announcementAt", announcementAt.toString())
                                .put("softDeadlineAt", softDeadlineAt.toString())
                                .put("hardDeadlineAt", hardDeadlineAt.toString())
                                .toString()))
                        .andExpect(status().isOk())
                        .andExpectAll(resultMatchers);


                securePerform(get("/tasks/{id}", taskId))
                        .andExpect(status().isOk())
                        .andExpectAll(resultMatchers);
            }
        };
    }

    @Test
    void putTask__otherAsTeacher__invalid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                long courseId = ef.createCourse(ef.createEmployee());
                int taskNumber = 1;
                String title = faker.lorem().sentence();
                String description = faker.lorem().paragraph();
                int maxScore = 150;
                int maxPenaltyPercent = 60;
                boolean announced = true;
                boolean deadlinesEnabled = true;
                ZonedDateTime announcementAt = ZonedDateTime.now().minusSeconds(5);
                ZonedDateTime softDeadlineAt = ZonedDateTime.now().plusDays(15);
                ZonedDateTime hardDeadlineAt = ZonedDateTime.now().plusDays(30);

                String prevTitle = faker.lorem().sentence();
                String prevDescription = faker.lorem().paragraph();

                long taskId = taskService.create(TaskDto.builder()
                        .title(prevTitle)
                        .description(prevDescription)
                        .course(courseId)
                        .build()).getId();

                securePerform(put("/tasks/{id}", taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.createObjectNode()
                                .put("course", courseId)
                                .put("taskNumber", taskNumber)
                                .put("title", title)
                                .put("description", description)
                                .put("maxScore", maxScore)
                                .put("maxPenaltyPercent", maxPenaltyPercent)
                                .put("announced", announced)
                                .put("deadlinesEnabled", deadlinesEnabled)
                                .put("announcementAt", announcementAt.toString())
                                .put("softDeadlineAt", softDeadlineAt.toString())
                                .put("hardDeadlineAt", hardDeadlineAt.toString())
                                .toString()))
                        .andExpect(status().isForbidden());
            }
        };
    }

    @Test
    void putTask__withCourseToken__invalid() {
        new WithCourseToken() {
            @Override
            void run() throws Exception {
                long courseId = getCourseId();
                int taskNumber = 1;
                String title = faker.lorem().sentence();
                String description = faker.lorem().paragraph();
                int maxScore = 150;
                int maxPenaltyPercent = 60;
                boolean announced = true;
                boolean deadlinesEnabled = true;
                ZonedDateTime announcementAt = ZonedDateTime.now().minusSeconds(5);
                ZonedDateTime softDeadlineAt = ZonedDateTime.now().plusDays(15);
                ZonedDateTime hardDeadlineAt = ZonedDateTime.now().plusDays(30);

                String prevTitle = faker.lorem().sentence();
                String prevDescription = faker.lorem().paragraph();

                long taskId = taskService.create(TaskDto.builder()
                        .title(prevTitle)
                        .description(prevDescription)
                        .course(courseId)
                        .build()).getId();

                securePerform(put("/tasks/{id}", taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.createObjectNode()
                                .put("course", courseId)
                                .put("taskNumber", taskNumber)
                                .put("title", title)
                                .put("description", description)
                                .put("maxScore", maxScore)
                                .put("maxPenaltyPercent", maxPenaltyPercent)
                                .put("announced", announced)
                                .put("deadlinesEnabled", deadlinesEnabled)
                                .put("announcementAt", announcementAt.toString())
                                .put("softDeadlineAt", softDeadlineAt.toString())
                                .put("hardDeadlineAt", hardDeadlineAt.toString())
                                .toString()))
                        .andExpect(status().isForbidden());
            }
        };
    }

    @Test
    void putTask__courseChange__invalid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                long courseId = ef.createCourse(ef.createEmployee());
                int taskNumber = 1;
                String title = faker.lorem().sentence();
                String description = faker.lorem().paragraph();
                int maxScore = 150;
                int maxPenaltyPercent = 60;
                boolean announced = true;
                boolean deadlinesEnabled = true;
                ZonedDateTime announcementAt = ZonedDateTime.now().minusSeconds(5);
                ZonedDateTime softDeadlineAt = ZonedDateTime.now().plusDays(15);
                ZonedDateTime hardDeadlineAt = ZonedDateTime.now().plusDays(30);

                long prevCourseId = ef.createCourse(ef.createEmployee());
                String prevTitle = faker.lorem().sentence();
                String prevDescription = faker.lorem().paragraph();

                long taskId = taskService.create(TaskDto.builder()
                        .title(prevTitle)
                        .description(prevDescription)
                        .course(prevCourseId)
                        .build()).getId();

                securePerform(put("/tasks/{id}", taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.createObjectNode()
                                .put("course", courseId)
                                .put("taskNumber", taskNumber)
                                .put("title", title)
                                .put("description", description)
                                .put("maxScore", maxScore)
                                .put("maxPenaltyPercent", maxPenaltyPercent)
                                .put("announced", announced)
                                .put("deadlinesEnabled", deadlinesEnabled)
                                .put("announcementAt", announcementAt.toString())
                                .put("softDeadlineAt", softDeadlineAt.toString())
                                .put("hardDeadlineAt", hardDeadlineAt.toString())
                                .toString()))
                        .andExpect(status().isBadRequest());
            }
        };
    }

    @Test
    void putTask__conflictingTaskNumber__invalid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                long courseId = ef.createCourse(getSelfEmployeeIdAsLong());
                int taskNumber = 123;
                String title = faker.lorem().sentence();
                String description = faker.lorem().paragraph();
                int maxScore = 150;
                int maxPenaltyPercent = 60;
                boolean announced = true;
                boolean deadlinesEnabled = true;
                ZonedDateTime announcementAt = ZonedDateTime.now().minusSeconds(5);
                ZonedDateTime softDeadlineAt = ZonedDateTime.now().plusDays(15);
                ZonedDateTime hardDeadlineAt = ZonedDateTime.now().plusDays(30);

                String prevTitle = faker.lorem().sentence();
                String prevDescription = faker.lorem().paragraph();

                Long taskId = transactionTemplate.execute((status) -> {
                    long id = taskService.create(TaskDto.builder()
                            .title(prevTitle)
                            .description(prevDescription)
                            .course(courseId)
                            .build()).getId();

                    taskService.create(TaskDto.builder()
                            .title(faker.lorem().sentence())
                            .course(courseId)
                            .taskNumber(taskNumber)
                            .build());
                    status.flush();
                    return id;
                });

                securePerform(put("/tasks/{id}", taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.createObjectNode()
                                .put("course", courseId)
                                .put("taskNumber", taskNumber)
                                .put("title", title)
                                .put("description", description)
                                .put("maxScore", maxScore)
                                .put("maxPenaltyPercent", maxPenaltyPercent)
                                .put("announced", announced)
                                .put("deadlinesEnabled", deadlinesEnabled)
                                .put("announcementAt", announcementAt.toString())
                                .put("softDeadlineAt", softDeadlineAt.toString())
                                .put("hardDeadlineAt", hardDeadlineAt.toString())
                                .toString()))
                        .andExpect(status().isConflict());
            }
        };
    }

    @Test
    void putTask__notExists__invalid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                long courseId = ef.createCourse(ef.createEmployee());
                int taskNumber = 1;
                String title = faker.lorem().sentence();
                String description = faker.lorem().paragraph();
                int maxScore = 150;
                int maxPenaltyPercent = 60;
                boolean announced = true;
                boolean deadlinesEnabled = true;
                ZonedDateTime announcementAt = ZonedDateTime.now().minusSeconds(5);
                ZonedDateTime softDeadlineAt = ZonedDateTime.now().plusDays(15);
                ZonedDateTime hardDeadlineAt = ZonedDateTime.now().plusDays(30);

                String prevTitle = faker.lorem().sentence();
                String prevDescription = faker.lorem().paragraph();

                long taskId = taskService.create(TaskDto.builder()
                        .title(prevTitle)
                        .description(prevDescription)
                        .course(courseId)
                        .build()).getId();

                securePerform(put("/tasks/{id}", taskId + 1000)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.createObjectNode()
                                .put("course", courseId)
                                .put("taskNumber", taskNumber)
                                .put("title", title)
                                .put("description", description)
                                .put("maxScore", maxScore)
                                .put("maxPenaltyPercent", maxPenaltyPercent)
                                .put("announced", announced)
                                .put("deadlinesEnabled", deadlinesEnabled)
                                .put("announcementAt", announcementAt.toString())
                                .put("softDeadlineAt", softDeadlineAt.toString())
                                .put("hardDeadlineAt", hardDeadlineAt.toString())
                                .toString()))
                        .andExpect(status().isNotFound());
            }
        };
    }

    @Test
    void putTask__deadlinesMixedNullsAndEnabled__invalid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                long courseId = ef.createCourse(getSelfEmployeeIdAsLong());
                int taskNumber = 1;
                String title = faker.lorem().sentence();
                String description = faker.lorem().paragraph();
                int maxScore = 150;
                int maxPenaltyPercent = 60;
                boolean announced = true;
                boolean deadlinesEnabled = true;
                ZonedDateTime announcementAt = ZonedDateTime.now().minusSeconds(5);
                ZonedDateTime softDeadlineAt = ZonedDateTime.now().plusDays(15);

                String prevTitle = faker.lorem().sentence();
                String prevDescription = faker.lorem().paragraph();

                long taskId = taskService.create(TaskDto.builder()
                        .title(prevTitle)
                        .description(prevDescription)
                        .course(courseId)
                        .build()).getId();

                securePerform(put("/tasks/{id}", taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.createObjectNode()
                                .put("course", courseId)
                                .put("taskNumber", taskNumber)
                                .put("title", title)
                                .put("description", description)
                                .put("maxScore", maxScore)
                                .put("maxPenaltyPercent", maxPenaltyPercent)
                                .put("announced", announced)
                                .put("deadlinesEnabled", deadlinesEnabled)
                                .put("announcementAt", announcementAt.toString())
                                .put("softDeadlineAt", softDeadlineAt.toString())
                                .putNull("hardDeadlineAt")
                                .toString()))
                        .andExpect(status().isBadRequest());
            }
        };
    }

    @Test
    void putTask__deadlinesMixedNullsAndDisabled__valid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                long courseId = ef.createCourse(getSelfEmployeeIdAsLong());
                int taskNumber = 1;
                String title = faker.lorem().sentence();
                String description = faker.lorem().paragraph();
                int maxScore = 150;
                int maxPenaltyPercent = 60;
                boolean announced = true;
                boolean deadlinesEnabled = false;
                ZonedDateTime announcementAt = ZonedDateTime.now().minusSeconds(5);
                ZonedDateTime softDeadlineAt = ZonedDateTime.now().plusDays(15);

                String prevTitle = faker.lorem().sentence();
                String prevDescription = faker.lorem().paragraph();

                long taskId = taskService.create(TaskDto.builder()
                        .title(prevTitle)
                        .description(prevDescription)
                        .course(courseId)
                        .build()).getId();

                ResultMatcher[] resultMatchers = {
                        jsonPath("$.id").value(taskId),
                        jsonPath("$.course").value(courseId),
                        jsonPath("$.taskNumber").value(taskNumber),
                        jsonPath("$.title").value(title),
                        jsonPath("$.description").value(description),
                        jsonPath("$.maxScore").value(maxScore),
                        jsonPath("$.maxPenaltyPercent").value(maxPenaltyPercent),
                        jsonPath("$.announced").value(announced),
                        jsonPath("$.deadlinesEnabled").value(deadlinesEnabled),
                        jsonPath("$.announcementAt", new TestUtils.DateMatcher(announcementAt)),
                        jsonPath("$.softDeadlineAt", new TestUtils.DateMatcher(softDeadlineAt)),
                };

                securePerform(put("/tasks/{id}", taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.createObjectNode()
                                .put("course", courseId)
                                .put("taskNumber", taskNumber)
                                .put("title", title)
                                .put("description", description)
                                .put("maxScore", maxScore)
                                .put("maxPenaltyPercent", maxPenaltyPercent)
                                .put("announced", announced)
                                .put("deadlinesEnabled", deadlinesEnabled)
                                .put("announcementAt", announcementAt.toString())
                                .put("softDeadlineAt", softDeadlineAt.toString())
                                .putNull("hardDeadlineAt")
                                .toString()))
                        .andExpect(status().isOk())
                        .andExpectAll(resultMatchers);


                securePerform(get("/tasks/{id}", taskId))
                        .andExpect(status().isOk())
                        .andExpectAll(resultMatchers);
            }
        };
    }

    @Test
    void putTask__notAuthenticated__invalid() throws Exception {
        long courseId = ef.createCourse(ef.createEmployee());
        int taskNumber = 123;
        String title = faker.lorem().sentence();
        String description = faker.lorem().paragraph();
        int maxScore = 150;
        int maxPenaltyPercent = 60;
        boolean announced = true;
        ZonedDateTime announcementAt = ZonedDateTime.now().minusSeconds(5);
        ZonedDateTime softDeadlineAt = ZonedDateTime.now().plusDays(15);
        ZonedDateTime hardDeadlineAt = ZonedDateTime.now().plusDays(30);

        String prevTitle = faker.lorem().sentence();
        String prevDescription = faker.lorem().paragraph();

        long taskId = taskService.create(TaskDto.builder()
                .title(prevTitle)
                .description(prevDescription)
                .course(courseId)
                .build()).getId();

        mvc.perform(put("/tasks/{id}", taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.createObjectNode()
                                .put("course", courseId)
                                .put("taskNumber", taskNumber)
                                .put("title", title)
                                .put("description", description)
                                .put("maxScore", maxScore)
                                .put("maxPenaltyPercent", maxPenaltyPercent)
                                .put("announced", announced)
                                .put("announcementAt", announcementAt.toString())
                                .put("softDeadlineAt", softDeadlineAt.toString())
                                .put("hardDeadlineAt", hardDeadlineAt.toString())
                                .toString()))
                .andExpect(status().isUnauthorized());
    }

    // ================================================================================================================

    @Test
    void patchTask__self__valid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                long courseId = ef.createCourse(getSelfEmployeeIdAsLong());
                int taskNumber = 1;
                String description = faker.lorem().paragraph();
                int maxScore = 150;
                int maxPenaltyPercent = 60;
                boolean announced = true;
                ZonedDateTime announcementAt = ZonedDateTime.now().minusSeconds(5);
                ZonedDateTime softDeadlineAt = ZonedDateTime.now().plusDays(15);
                ZonedDateTime hardDeadlineAt = ZonedDateTime.now().plusDays(30);

                String title = faker.lorem().sentence();
                String prevDescription = faker.lorem().paragraph();

                long taskId = taskService.create(TaskDto.builder()
                        .title(title)
                        .description(prevDescription)
                        .course(courseId)
                        .build()).getId();

                ResultMatcher[] resultMatchers = {
                        jsonPath("$.id").value(taskId),
                        jsonPath("$.course").value(courseId),
                        jsonPath("$.taskNumber").value(taskNumber),
                        jsonPath("$.title").value(title),
                        jsonPath("$.description").value(description),
                        jsonPath("$.maxScore").value(maxScore),
                        jsonPath("$.maxPenaltyPercent").value(maxPenaltyPercent),
                        jsonPath("$.announced").value(announced),
                        jsonPath("$.announcementAt", new TestUtils.DateMatcher(announcementAt)),
                        jsonPath("$.softDeadlineAt", new TestUtils.DateMatcher(softDeadlineAt)),
                        jsonPath("$.hardDeadlineAt", new TestUtils.DateMatcher(hardDeadlineAt)),
                };

                securePerform(patch("/tasks/{id}", taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.createObjectNode()
                                .put("taskNumber", taskNumber)
                                .put("description", description)
                                .put("maxScore", maxScore)
                                .put("maxPenaltyPercent", maxPenaltyPercent)
                                .put("announced", announced)
                                .put("announcementAt", announcementAt.toString())
                                .put("softDeadlineAt", softDeadlineAt.toString())
                                .put("hardDeadlineAt", hardDeadlineAt.toString())
                                .toString()))
                        .andExpect(status().isOk())
                        .andExpectAll(resultMatchers);


                securePerform(get("/tasks/{id}", taskId))
                        .andExpect(status().isOk())
                        .andExpectAll(resultMatchers);
            }
        };
    }

    @Test
    void patchTask__otherAsAdmin__valid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                long courseId = ef.createCourse(ef.createEmployee());
                int taskNumber = 1;
                String description = faker.lorem().paragraph();
                int maxScore = 150;
                int maxPenaltyPercent = 60;
                boolean announced = true;
                ZonedDateTime announcementAt = ZonedDateTime.now().minusSeconds(5);
                ZonedDateTime softDeadlineAt = ZonedDateTime.now().plusDays(15);
                ZonedDateTime hardDeadlineAt = ZonedDateTime.now().plusDays(30);

                String title = faker.lorem().sentence();
                String prevDescription = faker.lorem().paragraph();

                long taskId = taskService.create(TaskDto.builder()
                        .title(title)
                        .description(prevDescription)
                        .course(courseId)
                        .build()).getId();

                ResultMatcher[] resultMatchers = {
                        jsonPath("$.id").value(taskId),
                        jsonPath("$.course").value(courseId),
                        jsonPath("$.taskNumber").value(taskNumber),
                        jsonPath("$.title").value(title),
                        jsonPath("$.description").value(description),
                        jsonPath("$.maxScore").value(maxScore),
                        jsonPath("$.maxPenaltyPercent").value(maxPenaltyPercent),
                        jsonPath("$.announced").value(announced),
                        jsonPath("$.announcementAt", new TestUtils.DateMatcher(announcementAt)),
                        jsonPath("$.softDeadlineAt", new TestUtils.DateMatcher(softDeadlineAt)),
                        jsonPath("$.hardDeadlineAt", new TestUtils.DateMatcher(hardDeadlineAt)),
                };

                securePerform(patch("/tasks/{id}", taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.createObjectNode()
                                .put("taskNumber", taskNumber)
                                .put("description", description)
                                .put("maxScore", maxScore)
                                .put("maxPenaltyPercent", maxPenaltyPercent)
                                .put("announced", announced)
                                .put("announcementAt", announcementAt.toString())
                                .put("softDeadlineAt", softDeadlineAt.toString())
                                .put("hardDeadlineAt", hardDeadlineAt.toString())
                                .toString()))
                        .andExpect(status().isOk())
                        .andExpectAll(resultMatchers);


                securePerform(get("/tasks/{id}", taskId))
                        .andExpect(status().isOk())
                        .andExpectAll(resultMatchers);
            }
        };
    }

    @Test
    void patchTask__otherAsTeacher__invalid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                long courseId = ef.createCourse(ef.createEmployee());
                ZonedDateTime announcementAt = ZonedDateTime.now().minusSeconds(5);

                String title = faker.lorem().sentence();
                String description = faker.lorem().paragraph();

                long taskId = taskService.create(TaskDto.builder()
                        .title(title)
                        .description(description)
                        .course(courseId)
                        .build()).getId();

                securePerform(patch("/tasks/{id}", taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.createObjectNode()
                                .put("announcementAt", announcementAt.toString())
                                .toString()))
                        .andExpect(status().isForbidden());
            }
        };
    }

    @Test
    void patchTask__withCourseToken__invalid() {
        new WithCourseToken() {
            @Override
            void run() throws Exception {
                long courseId = getCourseId();
                ZonedDateTime announcementAt = ZonedDateTime.now().minusSeconds(5);

                String title = faker.lorem().sentence();
                String description = faker.lorem().paragraph();

                long taskId = taskService.create(TaskDto.builder()
                        .title(title)
                        .description(description)
                        .course(courseId)
                        .build()).getId();

                securePerform(patch("/tasks/{id}", taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.createObjectNode()
                                .put("announcementAt", announcementAt.toString())
                                .toString()))
                        .andExpect(status().isForbidden());
            }
        };
    }

    @Test
    void patchTask__courseChange__invalid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                long employeeId = ef.createEmployee();
                long courseId = ef.createCourse(employeeId);

                long prevCourseId = ef.createCourse(employeeId);
                String title = faker.lorem().sentence();
                String description = faker.lorem().paragraph();

                long taskId = taskService.create(TaskDto.builder()
                        .title(title)
                        .description(description)
                        .course(prevCourseId)
                        .build()).getId();

                securePerform(patch("/tasks/{id}", taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.createObjectNode()
                                .put("course", courseId)
                                .toString()))
                        .andExpect(status().isBadRequest());
            }
        };
    }

    @Test
    void patchTask__conflictingTaskNumber__invalid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                long courseId = ef.createCourse(ef.createEmployee());
                int taskNumber = 123;

                long taskId = taskService.create(TaskDto.builder()
                        .title(faker.lorem().sentence())
                        .course(courseId)
                        .taskNumber(1)
                        .build()).getId();

                taskService.create(TaskDto.builder()
                        .title(faker.lorem().sentence())
                        .course(courseId)
                        .taskNumber(taskNumber)
                        .build());

                securePerform(patch("/tasks/{id}", taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.createObjectNode()
                                .put("course", courseId)
                                .put("taskNumber", taskNumber)
                                .toString()))
                        .andExpect(status().isConflict());
            }
        };
    }

    @Test
    void patchTask__notExists__invalid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                long employeeId = ef.createEmployee();
                long courseId = ef.createCourse(employeeId);

                long prevCourseId = ef.createCourse(employeeId);
                String title = faker.lorem().sentence();
                String description = faker.lorem().paragraph();

                long taskId = taskService.create(TaskDto.builder()
                        .title(title)
                        .description(description)
                        .course(prevCourseId)
                        .build()).getId();

                securePerform(patch("/tasks/{id}", taskId + 1000)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.createObjectNode()
                                .put("course", courseId)
                                .toString()))
                        .andExpect(status().isNotFound());
            }
        };
    }

    @Test
    void patchTask__deadlinesMixedNullsAndEnabled__invalid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                long courseId = ef.createCourse(getSelfEmployeeIdAsLong());
                ZonedDateTime announcementAt = ZonedDateTime.now().minusSeconds(5);

                String title = faker.lorem().sentence();
                String description = faker.lorem().paragraph();

                long taskId = taskService.create(TaskDto.builder()
                        .title(title)
                        .description(description)
                        .course(courseId)
                        .softDeadlineAt(ZonedDateTime.now().plusDays(5))
                        .build()).getId();

                securePerform(patch("/tasks/{id}", taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.createObjectNode()
                                .put("announcementAt", announcementAt.toString())
                                .put("deadlinesEnabled", true)
                                .toString()))
                        .andExpect(status().isBadRequest());
            }
        };
    }

    @Test
    void patchTask__deadlinesMixedNullsAndDisabled__valid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                long courseId = ef.createCourse(getSelfEmployeeIdAsLong());
                int taskNumber = 1;
                String title = faker.lorem().sentence();
                String description = faker.lorem().paragraph();
                int maxScore = 150;
                int maxPenaltyPercent = 60;
                boolean announced = true;
                boolean deadlinesEnabled = false;
                ZonedDateTime announcementAt = ZonedDateTime.now().minusSeconds(5);
                ZonedDateTime softDeadlineAt = ZonedDateTime.now().plusDays(15);

                String prevTitle = faker.lorem().sentence();
                String prevDescription = faker.lorem().paragraph();

                long taskId = taskService.create(TaskDto.builder()
                        .title(prevTitle)
                        .description(prevDescription)
                        .course(courseId)
                        .build()).getId();

                ResultMatcher[] resultMatchers = {
                        jsonPath("$.id").value(taskId),
                        jsonPath("$.course").value(courseId),
                        jsonPath("$.taskNumber").value(taskNumber),
                        jsonPath("$.title").value(title),
                        jsonPath("$.description").value(description),
                        jsonPath("$.maxScore").value(maxScore),
                        jsonPath("$.maxPenaltyPercent").value(maxPenaltyPercent),
                        jsonPath("$.announced").value(announced),
                        jsonPath("$.deadlinesEnabled").value(deadlinesEnabled),
                        jsonPath("$.announcementAt", new TestUtils.DateMatcher(announcementAt)),
                        jsonPath("$.softDeadlineAt", new TestUtils.DateMatcher(softDeadlineAt)),
                };

                securePerform(put("/tasks/{id}", taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.createObjectNode()
                                .put("course", courseId)
                                .put("taskNumber", taskNumber)
                                .put("title", title)
                                .put("description", description)
                                .put("maxScore", maxScore)
                                .put("maxPenaltyPercent", maxPenaltyPercent)
                                .put("announced", announced)
                                .put("deadlinesEnabled", deadlinesEnabled)
                                .put("announcementAt", announcementAt.toString())
                                .put("softDeadlineAt", softDeadlineAt.toString())
                                .putNull("hardDeadlineAt")
                                .toString()))
                        .andExpect(status().isOk())
                        .andExpectAll(resultMatchers);


                securePerform(get("/tasks/{id}", taskId))
                        .andExpect(status().isOk())
                        .andExpectAll(resultMatchers);
            }
        };
    }

    @Test
    void patchTask__notAuthenticated__invalid() throws Exception {
        long courseId = ef.createCourse(ef.createEmployee());
        int taskNumber = 123;

        long taskId = taskService.create(TaskDto.builder()
                .title(faker.lorem().sentence())
                .course(courseId)
                .taskNumber(1)
                .build()).getId();

        mvc.perform(
                        patch("/tasks/{id}", taskId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.createObjectNode()
                                        .put("taskNumber", taskNumber)
                                        .toString()
                                )
                )
                .andExpect(status().isUnauthorized());
    }

    // ================================================================================================================

    @Test
    void deleteTask__self__valid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                String title = faker.lorem().sentence();
                long courseId = ef.createCourse(getSelfEmployeeIdAsLong());

                long taskId = taskService.create(TaskDto.builder()
                        .title(title)
                        .course(courseId)
                        .build()).getId();

                securePerform(delete("/tasks/{id}", taskId))
                        .andExpect(status().isOk());

                securePerform(get("/tasks/{id}", taskId))
                        .andExpect(status().isNotFound());
            }
        };
    }

    @Test
    void deleteTask__otherAsAdmin__valid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                String title = faker.lorem().sentence();
                long courseId = ef.createCourse(ef.createEmployee());

                long taskId = taskService.create(TaskDto.builder()
                        .title(title)
                        .course(courseId)
                        .build()).getId();

                securePerform(delete("/tasks/{id}", taskId))
                        .andExpect(status().isOk());

                securePerform(get("/tasks/{id}", taskId))
                        .andExpect(status().isNotFound());
            }
        };
    }

    @Test
    void deleteTask__otherAsTeacher__invalid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                String title = faker.lorem().sentence();
                long courseId = ef.createCourse(ef.createEmployee());

                long taskId = taskService.create(TaskDto.builder()
                        .title(title)
                        .course(courseId)
                        .build()).getId();

                securePerform(delete("/tasks/{id}", taskId))
                        .andExpect(status().isForbidden());
            }
        };
    }

    @Test
    void deleteTask__withCourseToken__invalid() {
        new WithCourseToken() {
            @Override
            void run() throws Exception {
                String title = faker.lorem().sentence();
                long courseId = getCourseId();

                long taskId = taskService.create(TaskDto.builder()
                        .title(title)
                        .course(courseId)
                        .build()).getId();

                securePerform(delete("/tasks/{id}", taskId))
                        .andExpect(status().isForbidden());
            }
        };
    }

    @Test
    void deleteTask__notFound__invalid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                String title = faker.lorem().sentence();
                long courseId = ef.createCourse(getSelfEmployeeIdAsLong());

                long taskId = taskService.create(TaskDto.builder()
                        .title(title)
                        .course(courseId)
                        .build()).getId();

                securePerform(delete("/tasks/{id}", taskId + 1000))
                        .andExpect(status().isNotFound());
            }
        };
    }

    @Test
    void deleteTask__notAuthenticated__invalid() throws Exception {
        String title = faker.lorem().sentence();
        long courseId = ef.createCourse(ef.createEmployee());

        long taskId = taskService.create(TaskDto.builder()
                .title(title)
                .course(courseId)
                .build()).getId();

        mvc.perform(delete("/tasks/{id}", taskId))
                .andExpect(status().isUnauthorized());
    }
}
