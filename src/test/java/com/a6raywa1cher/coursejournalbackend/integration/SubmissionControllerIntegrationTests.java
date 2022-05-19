package com.a6raywa1cher.coursejournalbackend.integration;

import com.a6raywa1cher.coursejournalbackend.RequestContext;
import com.a6raywa1cher.coursejournalbackend.TestUtils;
import com.a6raywa1cher.coursejournalbackend.dto.CriteriaDto;
import com.a6raywa1cher.coursejournalbackend.dto.SubmissionDto;
import com.a6raywa1cher.coursejournalbackend.dto.TaskDto;
import com.a6raywa1cher.coursejournalbackend.dto.exc.VariousParentEntitiesException;
import com.a6raywa1cher.coursejournalbackend.integration.models.SubmissionInfo;
import com.a6raywa1cher.coursejournalbackend.service.SubmissionService;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultMatcher;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
public class SubmissionControllerIntegrationTests extends AbstractIntegrationTests {
    @Autowired
    SubmissionService submissionService;

    RequestContext<Long> createGetSubmissionByIdContextWithCourse(long courseId) {
        ZonedDateTime submittedAt = ZonedDateTime.now().minusDays(1);
        double additionalScore = faker.number().randomDouble(2, 0, 5);
        long taskId = ef.createTask(ef.bag().withCourseId(courseId));
        long studentId = ef.createStudent(ef.bag().withCourseId(courseId));
        List<Long> satisfiedCriteria = createManyCriteria(taskId, faker.number().numberBetween(1, 3));

        long id = submissionService.create(SubmissionDto.builder()
                .submittedAt(submittedAt)
                .additionalScore(additionalScore)
                .task(taskId)
                .student(studentId)
                .satisfiedCriteria(satisfiedCriteria)
                .build()).getId();

        Function<String, ResultMatcher[]> matchers = prefix -> new ResultMatcher[]{
                jsonPath(prefix + ".id").value(id),
                jsonPath(prefix + ".task").value(taskId),
                jsonPath(prefix + ".student").value(studentId),
                jsonPath(prefix + ".submittedAt").value(new TestUtils.DateMatcher(submittedAt)),
                jsonPath(prefix + ".satisfiedCriteria", contains(satisfiedCriteria.stream().map(Math::toIntExact).toArray())),
                jsonPath(prefix + ".mainScore").isNumber(),
                jsonPath(prefix + ".additionalScore").value(additionalScore)
        };

        return RequestContext.<Long>builder()
                .request(id)
                .matchersSupplier(matchers)
                .data(Map.of("courseId", Long.toString(courseId)))
                .build();
    }

    RequestContext<Long> createGetSubmissionByIdContext(long userId) {
        long courseId = ef.createCourse(userId);
        return createGetSubmissionByIdContextWithCourse(courseId);
    }

    @Test
    void getSubmissionById__self__valid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                var ctx = createGetSubmissionByIdContext(getSelfEmployeeIdAsLong());

                long id = ctx.getRequest();
                ResultMatcher[] matchers = ctx.getMatchers();

                securePerform(get("/submissions/{id}", id))
                        .andExpect(status().isOk())
                        .andExpectAll(matchers);
            }
        };
    }

    @Test
    void getSubmissionById__otherAsAdmin__valid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                var ctx = createGetSubmissionByIdContext(ef.createEmployee());

                long id = ctx.getRequest();
                ResultMatcher[] matchers = ctx.getMatchers();

                securePerform(get("/submissions/{id}", id))
                        .andExpect(status().isOk())
                        .andExpectAll(matchers);
            }
        };
    }

    @Test
    void getSubmissionById__otherAsTeacher__invalid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                var ctx = createGetSubmissionByIdContext(ef.createEmployee());

                long id = ctx.getRequest();

                securePerform(get("/submissions/{id}", id))
                        .andExpect(status().isForbidden());
            }
        };
    }

    @Test
    void getSubmissionById__withCourseToken__valid() {
        new WithCourseToken() {
            @Override
            void run() throws Exception {
                var ctx = createGetSubmissionByIdContextWithCourse(getCourseId());
                long id = ctx.getRequest();
                ResultMatcher[] matchers = ctx.getMatchers();

                securePerform(get("/submissions/{id}", id))
                        .andExpect(status().isOk())
                        .andExpectAll(matchers);
            }
        };
    }

    @Test
    void getSubmissionById__notAuthenticated__invalid() throws Exception {
        var ctx = createGetSubmissionByIdContext(ef.createEmployee());

        long id = ctx.getRequest();

        mvc.perform(get("/submissions/{id}", id))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getSubmissionById__notExists__invalid() {
        var ctx = createGetSubmissionByIdContext(ef.createEmployee());

        long id = ctx.getRequest();

        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                securePerform(get("/submissions/{id}", id + 1000))
                        .andExpect(status().isNotFound());
            }
        };
    }

    // ================================================================================================================

    RequestContext<Long> createGetSubmissionsByCourseContext(long courseId, long studentId) {
        ZonedDateTime submittedAt = ZonedDateTime.now().minusDays(1);
        double additionalScore = faker.number().randomDouble(2, 0, 5);
        long taskId = ef.createTask(ef.bag().withCourseId(courseId));
        List<Long> satisfiedCriteria = createManyCriteria(taskId, faker.number().numberBetween(1, 3));

        long id = submissionService.create(SubmissionDto.builder()
                .submittedAt(submittedAt)
                .additionalScore(additionalScore)
                .task(taskId)
                .student(studentId)
                .satisfiedCriteria(satisfiedCriteria)
                .build()).getId();

        Function<String, ResultMatcher[]> matchers = prefix -> new ResultMatcher[]{
                jsonPath(prefix + ".id").value(id),
                jsonPath(prefix + ".task").value(taskId),
                jsonPath(prefix + ".student").value(studentId),
                jsonPath(prefix + ".submittedAt").value(new TestUtils.DateMatcher(submittedAt)),
                jsonPath(prefix + ".satisfiedCriteria", contains(satisfiedCriteria.stream().map(Math::toIntExact).toArray())),
                jsonPath(prefix + ".mainScore").isNumber(),
                jsonPath(prefix + ".additionalScore").value(additionalScore)
        };

        return RequestContext.<Long>builder()
                .request(id)
                .matchersSupplier(matchers)
                .build();
    }

    @Test
    void getSubmissionsByCourse__self__valid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                long courseId1 = ef.createCourse(getSelfEmployeeIdAsLong());
                long courseId2 = ef.createCourse(getSelfEmployeeIdAsLong());
                long studentId1 = ef.createStudent(ef.bag().withCourseId(courseId1));
                long studentId2 = ef.createStudent(ef.bag().withCourseId(courseId1));
                long studentId3 = ef.createStudent(ef.bag().withCourseId(courseId2));

                var ctx1 = createGetSubmissionsByCourseContext(courseId1, studentId1);
                var ctx2 = createGetSubmissionsByCourseContext(courseId1, studentId2);
                createGetSubmissionsByCourseContext(courseId2, studentId3);

                securePerform(get("/submissions/course/{id}", courseId1)
                        .queryParam("sort", "student,asc"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$", hasSize(2)))
                        .andExpectAll(ctx1.getMatchers("$[0]"))
                        .andExpectAll(ctx2.getMatchers("$[1]"));
            }
        };
    }

    @Test
    void getSubmissionsByCourse__otherAsAdmin__valid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                long userId = ef.createEmployee();
                long courseId1 = ef.createCourse(userId);
                long courseId2 = ef.createCourse(userId);
                long studentId1 = ef.createStudent(ef.bag().withCourseId(courseId1));
                long studentId2 = ef.createStudent(ef.bag().withCourseId(courseId1));
                long studentId3 = ef.createStudent(ef.bag().withCourseId(courseId2));

                var ctx1 = createGetSubmissionsByCourseContext(courseId1, studentId1);
                var ctx2 = createGetSubmissionsByCourseContext(courseId1, studentId2);
                createGetSubmissionsByCourseContext(courseId2, studentId3);

                securePerform(get("/submissions/course/{id}", courseId1)
                        .queryParam("sort", "student,asc"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$", hasSize(2)))
                        .andExpectAll(ctx1.getMatchers("$[0]"))
                        .andExpectAll(ctx2.getMatchers("$[1]"));
            }
        };
    }

    @Test
    void getSubmissionsByCourse__otherAsTeacher__invalid() {
        long id = ef.createCourse();

        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                securePerform(get("/submissions/course/{id}", id))
                        .andExpect(status().isForbidden());
            }
        };
    }

    @Test
    void getSubmissionsByCourse__withCourseToken__valid() {
        new WithCourseToken() {
            @Override
            void run() throws Exception {
                long courseId1 = getCourseId();
                long courseId2 = ef.createCourse(getOwnerId());
                long studentId1 = ef.createStudent(ef.bag().withCourseId(courseId1));
                long studentId2 = ef.createStudent(ef.bag().withCourseId(courseId1));
                long studentId3 = ef.createStudent(ef.bag().withCourseId(courseId2));

                var ctx1 = createGetSubmissionsByCourseContext(courseId1, studentId1);
                var ctx2 = createGetSubmissionsByCourseContext(courseId1, studentId2);
                createGetSubmissionsByCourseContext(courseId2, studentId3);

                securePerform(get("/submissions/course/{id}", courseId1)
                        .queryParam("sort", "student,asc"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$", hasSize(2)))
                        .andExpectAll(ctx1.getMatchers("$[0]"))
                        .andExpectAll(ctx2.getMatchers("$[1]"));
            }
        };
    }

    @Test
    void getSubmissionsByCourse__notAuthenticated__invalid() throws Exception {
        long id = ef.createCourse();
        mvc.perform(get("/submissions/course/{id}", id)).andExpect(status().isUnauthorized());
    }

    // ================================================================================================================

    RequestContext<Long> createGetSubmissionsByTaskContext(long taskId, long studentId) {
        ZonedDateTime submittedAt = ZonedDateTime.now().minusDays(1);
        double additionalScore = faker.number().randomDouble(2, 0, 5);
        List<Long> satisfiedCriteria = createManyCriteria(taskId, faker.number().numberBetween(1, 3));

        long id = submissionService.create(SubmissionDto.builder()
                .submittedAt(submittedAt)
                .additionalScore(additionalScore)
                .task(taskId)
                .student(studentId)
                .satisfiedCriteria(satisfiedCriteria)
                .build()).getId();

        Function<String, ResultMatcher[]> matchers = prefix -> new ResultMatcher[]{
                jsonPath(prefix + ".id").value(id),
                jsonPath(prefix + ".task").value(taskId),
                jsonPath(prefix + ".student").value(studentId),
                jsonPath(prefix + ".submittedAt").value(new TestUtils.DateMatcher(submittedAt)),
                jsonPath(prefix + ".satisfiedCriteria", contains(satisfiedCriteria.stream().map(Math::toIntExact).toArray())),
                jsonPath(prefix + ".mainScore").isNumber(),
                jsonPath(prefix + ".additionalScore").value(additionalScore)
        };

        return RequestContext.<Long>builder()
                .request(id)
                .matchersSupplier(matchers)
                .build();
    }

    @Test
    void getSubmissionsByTask__self__valid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                long courseId = ef.createCourse(getSelfEmployeeIdAsLong());
                long taskId1 = ef.createTask(ef.bag().withCourseId(courseId));
                long taskId2 = ef.createTask(ef.bag().withCourseId(courseId));
                long studentId1 = ef.createStudent(ef.bag().withCourseId(courseId));
                long studentId2 = ef.createStudent(ef.bag().withCourseId(courseId));
                long studentId3 = ef.createStudent(ef.bag().withCourseId(courseId));

                var ctx1 = createGetSubmissionsByTaskContext(taskId1, studentId1);
                var ctx2 = createGetSubmissionsByTaskContext(taskId1, studentId2);
                createGetSubmissionsByTaskContext(taskId2, studentId3);

                securePerform(get("/submissions/task/{id}", taskId1)
                        .queryParam("sort", "student,asc"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$", hasSize(2)))
                        .andExpectAll(ctx1.getMatchers("$[0]"))
                        .andExpectAll(ctx2.getMatchers("$[1]"));
            }
        };
    }

    @Test
    void getSubmissionsByTask__otherAsAdmin__valid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                long courseId = ef.createCourse();
                long taskId1 = ef.createTask(ef.bag().withCourseId(courseId));
                long taskId2 = ef.createTask(ef.bag().withCourseId(courseId));
                long studentId1 = ef.createStudent(ef.bag().withCourseId(courseId));
                long studentId2 = ef.createStudent(ef.bag().withCourseId(courseId));
                long studentId3 = ef.createStudent(ef.bag().withCourseId(courseId));

                var ctx1 = createGetSubmissionsByTaskContext(taskId1, studentId1);
                var ctx2 = createGetSubmissionsByTaskContext(taskId1, studentId2);
                createGetSubmissionsByTaskContext(taskId2, studentId3);

                securePerform(get("/submissions/task/{id}", taskId1)
                        .queryParam("sort", "student,asc"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$", hasSize(2)))
                        .andExpectAll(ctx1.getMatchers("$[0]"))
                        .andExpectAll(ctx2.getMatchers("$[1]"));
            }
        };
    }

    @Test
    void getSubmissionsByTask__otherAsTeacher__invalid() {
        long id = ef.createTask();

        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                securePerform(get("/submissions/task/{id}", id))
                        .andExpect(status().isForbidden());
            }
        };
    }

    @Test
    void getSubmissionsByTask__withCourseToken__valid() {
        new WithCourseToken() {
            @Override
            void run() throws Exception {
                long courseId = getCourseId();
                long taskId1 = ef.createTask(ef.bag().withCourseId(courseId));
                long taskId2 = ef.createTask(ef.bag().withCourseId(courseId));
                long studentId1 = ef.createStudent(ef.bag().withCourseId(courseId));
                long studentId2 = ef.createStudent(ef.bag().withCourseId(courseId));
                long studentId3 = ef.createStudent(ef.bag().withCourseId(courseId));

                var ctx1 = createGetSubmissionsByTaskContext(taskId1, studentId1);
                var ctx2 = createGetSubmissionsByTaskContext(taskId1, studentId2);
                createGetSubmissionsByTaskContext(taskId2, studentId3);

                securePerform(get("/submissions/task/{id}", taskId1)
                        .queryParam("sort", "student,asc"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$", hasSize(2)))
                        .andExpectAll(ctx1.getMatchers("$[0]"))
                        .andExpectAll(ctx2.getMatchers("$[1]"));
            }
        };
    }

    @Test
    void getSubmissionsByTask__notAuthenticated__invalid() throws Exception {
        long id = ef.createTask();
        mvc.perform(get("/submissions/task/{id}", id)).andExpect(status().isUnauthorized());
    }

    // ================================================================================================================

    RequestContext<Long> createGetSubmissionsByStudentAndCourseContext(long taskId, long studentId) {
        ZonedDateTime submittedAt = ZonedDateTime.now().minusDays(1);
        double additionalScore = faker.number().randomDouble(2, 0, 5);
        List<Long> satisfiedCriteria = createManyCriteria(taskId, faker.number().numberBetween(1, 3));

        long id = submissionService.create(SubmissionDto.builder()
                .submittedAt(submittedAt)
                .additionalScore(additionalScore)
                .task(taskId)
                .student(studentId)
                .satisfiedCriteria(satisfiedCriteria)
                .build()).getId();

        Function<String, ResultMatcher[]> matchers = prefix -> new ResultMatcher[]{
                jsonPath(prefix + ".id").value(id),
                jsonPath(prefix + ".task").value(taskId),
                jsonPath(prefix + ".student").value(studentId),
                jsonPath(prefix + ".submittedAt").value(new TestUtils.DateMatcher(submittedAt)),
                jsonPath(prefix + ".satisfiedCriteria", contains(satisfiedCriteria.stream().map(Math::toIntExact).toArray())),
                jsonPath(prefix + ".mainScore").isNumber(),
                jsonPath(prefix + ".additionalScore").value(additionalScore)
        };

        return RequestContext.<Long>builder()
                .request(id)
                .matchersSupplier(matchers)
                .build();
    }

    @Test
    void getSubmissionsByStudentAndCourse__self__valid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                long courseId1 = ef.createCourse(getSelfEmployeeIdAsLong());
                long courseId2 = ef.createCourse(getSelfEmployeeIdAsLong());
                long task1 = ef.createTask(ef.bag().withCourseId(courseId1));
                long task2 = ef.createTask(ef.bag().withCourseId(courseId1));
                long task3 = ef.createTask(ef.bag().withCourseId(courseId2));
                long studentId1 = ef.createStudent(ef.bag().withCourseId(courseId1));
                long studentId2 = ef.createStudent(ef.bag().withCourseId(courseId1));
                long studentId3 = ef.createStudent(ef.bag().withCourseId(courseId2));

                var ctx1 = createGetSubmissionsByStudentAndCourseContext(task1, studentId1);
                var ctx2 = createGetSubmissionsByStudentAndCourseContext(task2, studentId1);
                createGetSubmissionsByStudentAndCourseContext(task2, studentId2);
                createGetSubmissionsByStudentAndCourseContext(task3, studentId3);

                securePerform(get("/submissions/course/{id}/student/{id2}", courseId1, studentId1)
                        .queryParam("sort", "task,asc"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$", hasSize(2)))
                        .andExpectAll(ctx1.getMatchers("$[0]"))
                        .andExpectAll(ctx2.getMatchers("$[1]"));
            }
        };
    }

    @Test
    void getSubmissionsByStudentAndCourse__otherAsAdmin__valid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                long courseId1 = ef.createCourse();
                long courseId2 = ef.createCourse();
                long task1 = ef.createTask(ef.bag().withCourseId(courseId1));
                long task2 = ef.createTask(ef.bag().withCourseId(courseId1));
                long task3 = ef.createTask(ef.bag().withCourseId(courseId2));
                long studentId1 = ef.createStudent(ef.bag().withCourseId(courseId1));
                long studentId2 = ef.createStudent(ef.bag().withCourseId(courseId1));
                long studentId3 = ef.createStudent(ef.bag().withCourseId(courseId2));

                var ctx1 = createGetSubmissionsByStudentAndCourseContext(task1, studentId1);
                var ctx2 = createGetSubmissionsByStudentAndCourseContext(task2, studentId1);
                createGetSubmissionsByStudentAndCourseContext(task2, studentId2);
                createGetSubmissionsByStudentAndCourseContext(task3, studentId3);

                securePerform(get("/submissions/course/{id}/student/{id2}", courseId1, studentId1)
                        .queryParam("sort", "task,asc"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$", hasSize(2)))
                        .andExpectAll(ctx1.getMatchers("$[0]"))
                        .andExpectAll(ctx2.getMatchers("$[1]"));
            }
        };
    }

    @Test
    void getSubmissionsByStudentAndCourse__otherAsTeacher__invalid() {
        long courseId = ef.createCourse();
        long taskId = ef.createTask(ef.bag().withCourseId(courseId));
        long studentId = ef.createStudent(ef.bag().withCourseId(courseId));
        ef.createSubmission(ef.bag().withTaskId(taskId).withStudentId(studentId));

        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                securePerform(get("/submissions/course/{id}/student/{id2}", courseId, studentId))
                        .andExpect(status().isForbidden());
            }
        };
    }

    @Test
    void getSubmissionsByStudentAndCourse__withCourseToken__valid() {
        new WithCourseToken() {
            @Override
            void run() throws Exception {
                long courseId1 = getCourseId();
                long courseId2 = ef.createCourse(getOwnerId());
                long task1 = ef.createTask(ef.bag().withCourseId(courseId1));
                long task2 = ef.createTask(ef.bag().withCourseId(courseId1));
                long task3 = ef.createTask(ef.bag().withCourseId(courseId2));
                long studentId1 = ef.createStudent(ef.bag().withCourseId(courseId1));
                long studentId2 = ef.createStudent(ef.bag().withCourseId(courseId1));
                long studentId3 = ef.createStudent(ef.bag().withCourseId(courseId2));

                var ctx1 = createGetSubmissionsByStudentAndCourseContext(task1, studentId1);
                var ctx2 = createGetSubmissionsByStudentAndCourseContext(task2, studentId1);
                createGetSubmissionsByStudentAndCourseContext(task2, studentId2);
                createGetSubmissionsByStudentAndCourseContext(task3, studentId3);

                securePerform(get("/submissions/course/{id}/student/{id2}", courseId1, studentId1)
                        .queryParam("sort", "task,asc"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$", hasSize(2)))
                        .andExpectAll(ctx1.getMatchers("$[0]"))
                        .andExpectAll(ctx2.getMatchers("$[1]"));
            }
        };
    }

    @Test
    void getSubmissionsByStudentAndCourse__notAuthenticated__invalid() throws Exception {
        long courseId = ef.createCourse();
        long taskId = ef.createTask(ef.bag().withCourseId(courseId));
        long studentId = ef.createStudent(ef.bag().withCourseId(courseId));
        ef.createSubmission(ef.bag().withTaskId(taskId).withStudentId(studentId));
        mvc.perform(get("/submissions/course/{id}/student/{id2}", courseId, studentId))
                .andExpect(status().isUnauthorized());
    }

    // ================================================================================================================

    RequestContext<ObjectNode> getCreateSubmissionRequest(long taskId, long studentId) {
        ZonedDateTime submittedAt = ZonedDateTime.now().minusDays(2);
        List<Long> satisfiedCriteria = createManyCriteria(taskId, faker.number().numberBetween(1, 3));
        double additionalScore = faker.number().randomDouble(2, 2, 5);

        ObjectNode request = objectMapper.createObjectNode()
                .put("task", taskId)
                .put("student", studentId)
                .put("submittedAt", submittedAt.toString())
                .put("additionalScore", additionalScore);
        ArrayNode array = request.putArray("satisfiedCriteria");
        satisfiedCriteria.forEach(array::add);

        Function<String, ResultMatcher[]> matchers = prefix -> new ResultMatcher[]{
                jsonPath(prefix + ".id").isNumber(),
                jsonPath(prefix + ".task").value(taskId),
                jsonPath(prefix + ".student").value(studentId),
                jsonPath(prefix + ".submittedAt").value(new TestUtils.DateMatcher(submittedAt)),
                jsonPath(prefix + ".mainScore").value(new TestUtils.GreaterThanMatcher(0)),
                jsonPath(prefix + ".additionalScore").value(additionalScore),
                jsonPath(prefix + ".satisfiedCriteria", contains(satisfiedCriteria.stream().map(Math::toIntExact).toArray())),
        };

        return new RequestContext<>(request, matchers);
    }

    @Test
    void createSubmission__self__valid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                long courseId = ef.createCourse(getSelfEmployeeIdAsLong());
                long taskId = ef.createTask(ef.bag().withCourseId(courseId));
                long studentId = ef.createStudent(ef.bag().withCourseId(courseId));
                var ctx = getCreateSubmissionRequest(taskId, studentId);

                ObjectNode request = ctx.getRequest();

                MvcResult mvcResult = securePerform(post("/submissions/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isCreated())
                        .andExpectAll(ctx.getMatchers())
                        .andReturn();

                int id = JsonPath.read(mvcResult.getResponse().getContentAsString(), "$.id");

                securePerform(get("/submissions/{id}", id))
                        .andExpect(status().isOk())
                        .andExpectAll(ctx.getMatchers());
            }
        };
    }

    @Test
    void createSubmission__otherAsAdmin__valid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                long courseId = ef.createCourse();
                long taskId = ef.createTask(ef.bag().withCourseId(courseId));
                long studentId = ef.createStudent(ef.bag().withCourseId(courseId));
                var ctx = getCreateSubmissionRequest(taskId, studentId);

                ObjectNode request = ctx.getRequest();

                MvcResult mvcResult = securePerform(post("/submissions/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isCreated())
                        .andExpectAll(ctx.getMatchers())
                        .andReturn();

                int id = JsonPath.read(mvcResult.getResponse().getContentAsString(), "$.id");

                securePerform(get("/submissions/{id}", id))
                        .andExpect(status().isOk())
                        .andExpectAll(ctx.getMatchers());
            }
        };
    }

    @Test
    void createSubmission__otherAsTeacher__invalid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                long courseId = ef.createCourse();
                long taskId = ef.createTask(ef.bag().withCourseId(courseId));
                long studentId = ef.createStudent(ef.bag().withCourseId(courseId));
                var ctx = getCreateSubmissionRequest(taskId, studentId);

                ObjectNode request = ctx.getRequest();

                securePerform(post("/submissions/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isForbidden());
            }
        };
    }

    @Test
    void createSubmission__withCourseToken__invalid() {
        new WithCourseToken() {
            @Override
            void run() throws Exception {
                long courseId = getCourseId();
                long taskId = ef.createTask(ef.bag().withCourseId(courseId));
                long studentId = ef.createStudent(ef.bag().withCourseId(courseId));
                var ctx = getCreateSubmissionRequest(taskId, studentId);

                ObjectNode request = ctx.getRequest();

                securePerform(post("/submissions/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isForbidden());
            }
        };
    }

    @Test
    void createSubmission__mainScore__noEffect() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                long courseId = ef.createCourse(getSelfEmployeeIdAsLong());
                long taskId = ef.createTask(ef.bag().withCourseId(courseId));
                long studentId = ef.createStudent(ef.bag().withCourseId(courseId));
                var ctx = getCreateSubmissionRequest(taskId, studentId);
                int mainScore = 100500;


                ObjectNode request = ctx.getRequest()
                        .put("mainScore", mainScore);

                var mainScoreMatcher = new TestUtils.NotEqualsMatcher(100500);

                MvcResult mvcResult = securePerform(post("/submissions/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isCreated())
                        .andExpectAll(ctx.getMatchers())
                        .andExpect(jsonPath("$.mainScore").value(mainScoreMatcher))
                        .andReturn();

                int id = JsonPath.read(mvcResult.getResponse().getContentAsString(), "$.id");

                securePerform(get("/submissions/{id}", id))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.mainScore").value(mainScoreMatcher))
                        .andExpectAll(ctx.getMatchers());
            }
        };
    }

    @Test
    void createSubmission__criteriaFromDifferentTasks__invalid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                long courseId = ef.createCourse();
                long taskId = ef.createTask(ef.bag().withCourseId(courseId));
                long studentId = ef.createStudent(ef.bag().withCourseId(courseId));
                var ctx = getCreateSubmissionRequest(taskId, studentId);

                long additionalCriteriaId = ef.createCriteria(ef.bag().withCourseId(courseId));

                ObjectNode request = ctx.getRequest();
                ArrayNode array = (ArrayNode) request.get("satisfiedCriteria");
                array.add(additionalCriteriaId);

                securePerform(post("/submissions/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isBadRequest());
            }
        };
    }

    @Test
    void createSubmission__taskAndStudentNotUnique__invalid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                long courseId = ef.createCourse();
                long taskId = ef.createTask(ef.bag().withCourseId(courseId));
                long studentId = ef.createStudent(ef.bag().withCourseId(courseId));
                var ctx = getCreateSubmissionRequest(taskId, studentId);

                ef.createSubmission(ef.bag().withTaskId(taskId).withStudentId(studentId));

                ObjectNode request = ctx.getRequest();

                securePerform(post("/submissions/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isConflict());
            }
        };
    }

    @Test
    void createSubmission__notAuthenticated__invalid() throws Exception {
        long courseId = ef.createCourse();
        long taskId = ef.createTask(ef.bag().withCourseId(courseId));
        long studentId = ef.createStudent(ef.bag().withCourseId(courseId));
        var ctx = getCreateSubmissionRequest(taskId, studentId);

        ObjectNode request = ctx.getRequest();

        mvc.perform(post("/submissions/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                .andExpect(status().isUnauthorized());
    }

    // ================================================================================================================

    RequestContext<ObjectNode> getPutSubmissionRequest(long taskId, long studentId, int additionalScore) {
        ZonedDateTime submittedAt = ZonedDateTime.now().minusDays(2);

        ObjectNode request = objectMapper.createObjectNode()
                .put("task", taskId)
                .put("student", studentId)
                .put("submittedAt", submittedAt.toString())
                .put("additionalScore", additionalScore);
        request.putArray("satisfiedCriteria");

        Function<String, ResultMatcher[]> matchers = prefix -> new ResultMatcher[]{
                jsonPath(prefix + ".id").isNumber(),
                jsonPath(prefix + ".task").value(taskId),
                jsonPath(prefix + ".student").value(studentId),
                jsonPath(prefix + ".submittedAt").value(new TestUtils.DateMatcher(submittedAt)),
                jsonPath(prefix + ".mainScore").value(new TestUtils.GreaterThanMatcher(0)),
                jsonPath(prefix + ".additionalScore").value(additionalScore),
                jsonPath(prefix + ".satisfiedCriteria").isEmpty()
        };

        return new RequestContext<>(request, matchers);
    }

    @Test
    void putSubmission__self__valid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                long courseId = ef.createCourse(getSelfEmployeeIdAsLong());
                long taskId = ef.createTask(ef.bag().withCourseId(courseId));
                long studentId = ef.createStudent(ef.bag().withCourseId(courseId));
                long submissionId = ef.createSubmission(
                        ef.bag().withTaskId(taskId).withStudentId(studentId)
                                .withDto(SubmissionDto.builder().additionalScore(5d).build())
                );

                int newAdditionalScore = 10;

                RequestContext<ObjectNode> ctx = getPutSubmissionRequest(taskId, studentId, newAdditionalScore);
                ObjectNode request = ctx.getRequest();

                securePerform(put("/submissions/{id}", submissionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isOk())
                        .andExpectAll(ctx.getMatchers());

                securePerform(get("/submissions/{id}", submissionId))
                        .andExpect(status().isOk())
                        .andExpectAll(ctx.getMatchers());
            }
        };
    }

    @Test
    void putSubmission__otherAsAdmin__valid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                long courseId = ef.createCourse();
                long taskId = ef.createTask(ef.bag().withCourseId(courseId));
                long studentId = ef.createStudent(ef.bag().withCourseId(courseId));
                long submissionId = ef.createSubmission(
                        ef.bag().withTaskId(taskId).withStudentId(studentId)
                                .withDto(SubmissionDto.builder().additionalScore(5d).build())
                );

                int newAdditionalScore = 10;

                RequestContext<ObjectNode> ctx = getPutSubmissionRequest(taskId, studentId, newAdditionalScore);
                ObjectNode request = ctx.getRequest();

                securePerform(put("/submissions/{id}", submissionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isOk())
                        .andExpectAll(ctx.getMatchers());

                securePerform(get("/submissions/{id}", submissionId))
                        .andExpect(status().isOk())
                        .andExpectAll(ctx.getMatchers());
            }
        };
    }

    @Test
    void putSubmission__otherAsTeacher__invalid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                long courseId = ef.createCourse();
                long taskId = ef.createTask(ef.bag().withCourseId(courseId));
                long studentId = ef.createStudent(ef.bag().withCourseId(courseId));
                long submissionId = ef.createSubmission(
                        ef.bag().withTaskId(taskId).withStudentId(studentId)
                                .withDto(SubmissionDto.builder().additionalScore(5d).build())
                );

                int newAdditionalScore = 10;

                RequestContext<ObjectNode> ctx = getPutSubmissionRequest(taskId, studentId, newAdditionalScore);
                ObjectNode request = ctx.getRequest();

                securePerform(put("/submissions/{id}", submissionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isForbidden());
            }
        };
    }

    @Test
    void putSubmission__withCourseToken__invalid() {
        new WithCourseToken() {
            @Override
            void run() throws Exception {
                long courseId = getCourseId();
                long taskId = ef.createTask(ef.bag().withCourseId(courseId));
                long studentId = ef.createStudent(ef.bag().withCourseId(courseId));
                long submissionId = ef.createSubmission(
                        ef.bag().withTaskId(taskId).withStudentId(studentId)
                                .withDto(SubmissionDto.builder().additionalScore(5d).build())
                );

                int newAdditionalScore = 10;

                RequestContext<ObjectNode> ctx = getPutSubmissionRequest(taskId, studentId, newAdditionalScore);
                ObjectNode request = ctx.getRequest();

                securePerform(put("/submissions/{id}", submissionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isForbidden());
            }
        };
    }

    @Test
    void putSubmission__taskChange__invalid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                long courseId = ef.createCourse();
                long taskId = ef.createTask(ef.bag().withCourseId(courseId));
                long studentId = ef.createStudent(ef.bag().withCourseId(courseId));
                long submissionId = ef.createSubmission(
                        ef.bag().withTaskId(taskId).withStudentId(studentId)
                                .withDto(SubmissionDto.builder().additionalScore(5d).build())
                );

                int newAdditionalScore = 10;
                long newTaskId = ef.createTask(ef.bag().withCourseId(courseId));

                RequestContext<ObjectNode> ctx = getPutSubmissionRequest(newTaskId, studentId, newAdditionalScore);
                ObjectNode request = ctx.getRequest();

                securePerform(put("/submissions/{id}", submissionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isBadRequest());
            }
        };
    }

    @Test
    void putSubmission__studentChange__invalid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                long courseId = ef.createCourse();
                long taskId = ef.createTask(ef.bag().withCourseId(courseId));
                long studentId = ef.createStudent(ef.bag().withCourseId(courseId));
                long submissionId = ef.createSubmission(
                        ef.bag().withTaskId(taskId).withStudentId(studentId)
                                .withDto(SubmissionDto.builder().additionalScore(5d).build())
                );

                int newAdditionalScore = 10;
                long newStudentId = ef.createStudent(ef.bag().withCourseId(courseId));

                RequestContext<ObjectNode> ctx = getPutSubmissionRequest(taskId, newStudentId, newAdditionalScore);
                ObjectNode request = ctx.getRequest();

                securePerform(put("/submissions/{id}", submissionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isBadRequest());
            }
        };
    }

    @Test
    void putSubmission__mainScoreChange__noEffect() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                long courseId = ef.createCourse();
                long taskId = ef.createTask(ef.bag().withCourseId(courseId));
                long studentId = ef.createStudent(ef.bag().withCourseId(courseId));
                long submissionId = ef.createSubmission(
                        ef.bag().withTaskId(taskId).withStudentId(studentId)
                                .withDto(SubmissionDto.builder().additionalScore(5d).build())
                );

                int newAdditionalScore = 10;
                int mainScore = 100500;

                RequestContext<ObjectNode> ctx = getPutSubmissionRequest(taskId, studentId, newAdditionalScore);


                ObjectNode request = ctx.getRequest()
                        .put("mainScore", mainScore);

                var mainScoreMatcher = new TestUtils.NotEqualsMatcher(100500d);

                securePerform(put("/submissions/{id}", submissionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.mainScore").value(mainScoreMatcher))
                        .andExpectAll(ctx.getMatchers());

                securePerform(get("/submissions/{id}", submissionId))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.mainScore").value(mainScoreMatcher))
                        .andExpectAll(ctx.getMatchers());
            }
        };
    }

    @Test
    void putSubmission__criteriaFromDifferentTasks__invalid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                long courseId = ef.createCourse();
                long taskId = ef.createTask(ef.bag().withCourseId(courseId));
                long studentId = ef.createStudent(ef.bag().withCourseId(courseId));
                long submissionId = ef.createSubmission(ef.bag().withTaskId(taskId).withStudentId(studentId));
                int additionalScore = 5;

                var ctx = getPutSubmissionRequest(taskId, studentId, additionalScore);

                long additionalCriteriaId = ef.createCriteria(ef.bag().withCourseId(courseId));

                ObjectNode request = ctx.getRequest();
                ArrayNode array = (ArrayNode) request.get("satisfiedCriteria");
                array.add(additionalCriteriaId);

                securePerform(put("/submissions/{id}", submissionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isBadRequest());
            }
        };
    }

    @Test
    void putSubmission__notExists__invalid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                long courseId = ef.createCourse();
                long taskId = ef.createTask(ef.bag().withCourseId(courseId));
                long studentId = ef.createStudent(ef.bag().withCourseId(courseId));
                long submissionId = ef.createSubmission(
                        ef.bag().withTaskId(taskId).withStudentId(studentId)
                                .withDto(SubmissionDto.builder().additionalScore(5d).build())
                );

                int newAdditionalScore = 10;

                RequestContext<ObjectNode> ctx = getPutSubmissionRequest(taskId, studentId, newAdditionalScore);
                ObjectNode request = ctx.getRequest();

                securePerform(put("/submissions/{id}", submissionId + 1000)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isNotFound());
            }
        };
    }

    @Test
    void putSubmission__notAuthenticated__invalid() throws Exception {
        long courseId = ef.createCourse();
        long taskId = ef.createTask(ef.bag().withCourseId(courseId));
        long studentId = ef.createStudent(ef.bag().withCourseId(courseId));
        long submissionId = ef.createSubmission(
                ef.bag().withTaskId(taskId).withStudentId(studentId)
                        .withDto(SubmissionDto.builder().additionalScore(5d).build())
        );

        int newAdditionalScore = 10;

        RequestContext<ObjectNode> ctx = getPutSubmissionRequest(taskId, studentId, newAdditionalScore);
        ObjectNode request = ctx.getRequest();

        mvc.perform(put("/submissions/{id}", submissionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                .andExpect(status().isUnauthorized());
    }

    // ================================================================================================================

    RequestContext<ObjectNode> getSetForStudentAndCourseRequest(
            List<SubmissionInfo> submissionInfos, long studentId
    ) {
        ObjectNode request = objectMapper.createObjectNode();
        ArrayNode array = request.putArray("submissions");
        for (SubmissionInfo info : submissionInfos) {
            array.add(objectMapper.valueToTree(info));
        }

        Function<String, ResultMatcher[]> matchers = prefix -> IntStream.range(0, submissionInfos.size())
                .boxed()
                .flatMap(i -> {
                    SubmissionInfo info = submissionInfos.get(i);
                    return Arrays.stream(
                            getSubmissionMatchers(prefix + "[%d]".formatted(i),
                                    info.task(), info.submittedAt(), info.additionalScore(),
                                    studentId, info.satisfiedCriteria())
                    );
                })
                .toArray(ResultMatcher[]::new);
        ;

        return new RequestContext<>(request, matchers);
    }

    RequestContext<ObjectNode> prepareSetForStudentAndCourseRequest(long courseId, long studentId) {
        long taskId1 = ef.createTask(ef.bag().withCourseId(courseId));
        long taskId2 = ef.createTask(ef.bag().withCourseId(courseId));
        long taskId3 = ef.createTask(ef.bag().withCourseId(courseId));

        long task1Criteria1 = ef.createCriteria(ef.bag().withTaskId(taskId1));
        long task1Criteria2 = ef.createCriteria(ef.bag().withTaskId(taskId1));
        long task2Criteria = ef.createCriteria(ef.bag().withTaskId(taskId2));
        long task3Criteria = ef.createCriteria(ef.bag().withTaskId(taskId3));

        ZonedDateTime now = ZonedDateTime.now();

        // submission to be edited
        ef.createSubmission(ef.bag().withTaskId(taskId1).withStudentId(studentId)
                .withDto(SubmissionDto.builder().satisfiedCriteria(List.of(task1Criteria1, task1Criteria2)).build()));

        // submission to be deleted
        ef.createSubmission(ef.bag().withTaskId(taskId2).withStudentId(studentId)
                .withDto(SubmissionDto.builder().satisfiedCriteria(List.of(task2Criteria)).build()));

        // and one submission to be created

        var ctx = getSetForStudentAndCourseRequest(
                List.of(
                        new SubmissionInfo(taskId1, List.of(task1Criteria1), now, faker.number().numberBetween(0, 5)),
                        new SubmissionInfo(taskId3, List.of(task3Criteria), now, faker.number().numberBetween(0, 5))
                ),
                studentId
        );
        return new RequestContext<>(ctx.getRequest(),
                prefix -> Stream.concat(
                        Stream.of(jsonPath(prefix, hasSize(2))),
                        Arrays.stream(ctx.getMatchers(prefix))
                ).toArray(ResultMatcher[]::new)
        );
    }

    @Test
    void setSubmissionsForStudentAndCourse__self__valid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                // GIVEN
                long courseId = ef.createCourse(getSelfEmployeeIdAsLong());
                long studentId = ef.createStudent();

                var ctx = prepareSetForStudentAndCourseRequest(courseId, studentId);
                ObjectNode request = ctx.getRequest();

                // WHEN
                securePerform(post("/submissions/course/{cid}/student/{sid}/set", courseId, studentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isOk())
                        .andExpectAll(ctx.getMatchers());

                // THEN
                securePerform(get("/submissions/course/{cid}/student/{sid}", courseId, studentId))
                        .andExpect(status().isOk())
                        .andExpectAll(ctx.getMatchers());
            }
        };
    }

    @Test
    void setSubmissionsForStudentAndCourse__otherAsAdmin__valid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                // GIVEN
                long courseId = ef.createCourse();
                long studentId = ef.createStudent();

                var ctx = prepareSetForStudentAndCourseRequest(courseId, studentId);
                ObjectNode request = ctx.getRequest();

                // WHEN
                securePerform(post("/submissions/course/{cid}/student/{sid}/set", courseId, studentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isOk())
                        .andExpectAll(ctx.getMatchers());

                // THEN
                securePerform(get("/submissions/course/{cid}/student/{sid}", courseId, studentId))
                        .andExpect(status().isOk())
                        .andExpectAll(ctx.getMatchers());
            }
        };
    }

    @Test
    void setSubmissionsForStudentAndCourse__otherAsTeacher__invalid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                // GIVEN
                long courseId = ef.createCourse();
                long studentId = ef.createStudent();

                var ctx = prepareSetForStudentAndCourseRequest(courseId, studentId);
                ObjectNode request = ctx.getRequest();

                // WHEN
                securePerform(post("/submissions/course/{cid}/student/{sid}/set", courseId, studentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        // THEN
                        .andExpect(status().isForbidden());
            }
        };
    }

    @Test
    void setSubmissionsForStudentAndCourse__withCourseToken__invalid() {
        new WithCourseToken() {
            @Override
            void run() throws Exception {
                // GIVEN
                long courseId = getCourseId();
                long studentId = ef.createStudent();

                var ctx = prepareSetForStudentAndCourseRequest(courseId, studentId);
                ObjectNode request = ctx.getRequest();

                // WHEN
                securePerform(post("/submissions/course/{cid}/student/{sid}/set", courseId, studentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        // THEN
                        .andExpect(status().isForbidden());
            }
        };
    }

    @Test
    void setSubmissionsForStudentAndCourse__criteriaFromDifferentTasks__invalid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                // GIVEN
                long courseId = ef.createCourse(getSelfEmployeeIdAsLong());
                long taskId = ef.createTask(ef.bag().withCourseId(courseId));
                long studentId = ef.createStudent();
                long otherCriteriaId = ef.createCriteria(ef.bag().withCourseId(courseId));

                ObjectNode request = objectMapper.createObjectNode();
                ObjectNode info = objectMapper.createObjectNode()
                        .put("task", taskId)
                        .put("submittedAt", ZonedDateTime.now().toString())
                        .put("additionalScore", 2);
                info.putArray("satisfiedCriteria").add(otherCriteriaId);
                request.putArray("submissions").add(info);

                // WHEN
                securePerform(post("/submissions/course/{cid}/student/{sid}/set", courseId, studentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        // THEN
                        .andExpect(status().isBadRequest())
                        .andExpect(result ->
                                assertThat(result.getResolvedException())
                                        .isInstanceOf(VariousParentEntitiesException.class)
                        );
            }
        };
    }

    @Test
    void setSubmissionsForStudentAndCourse__notAuthenticated__invalid() throws Exception {
        // GIVEN
        long courseId = ef.createCourse();
        long studentId = ef.createStudent();

        var ctx = prepareSetForStudentAndCourseRequest(courseId, studentId);
        ObjectNode request = ctx.getRequest();

        // WHEN
        mvc.perform(post("/submissions/course/{cid}/student/{sid}/set", courseId, studentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                // THEN
                .andExpect(status().isUnauthorized());
    }

    // ================================================================================================================

    RequestContext<ObjectNode> getPatchSubmissionRequest(long taskId, long studentId, int additionalScore) {
        ZonedDateTime submittedAt = ZonedDateTime.now().minusDays(2);

        ObjectNode request = objectMapper.createObjectNode()
                .put("task", taskId)
                .put("student", studentId)
                .put("submittedAt", submittedAt.toString())
                .put("additionalScore", additionalScore);

        Function<String, ResultMatcher[]> matchers = prefix -> new ResultMatcher[]{
                jsonPath(prefix + ".id").isNumber(),
                jsonPath(prefix + ".task").value(taskId),
                jsonPath(prefix + ".student").value(studentId),
                jsonPath(prefix + ".submittedAt").value(new TestUtils.DateMatcher(submittedAt)),
                jsonPath(prefix + ".mainScore").value(new TestUtils.GreaterThanMatcher(0)),
                jsonPath(prefix + ".additionalScore").value(additionalScore),
                jsonPath(prefix + ".satisfiedCriteria").exists()
        };

        return new RequestContext<>(request, matchers);
    }

    @Test
    void patchSubmission__self__valid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                long courseId = ef.createCourse(getSelfEmployeeIdAsLong());
                long taskId = ef.createTask(ef.bag().withCourseId(courseId));
                long studentId = ef.createStudent(ef.bag().withCourseId(courseId));
                long submissionId = ef.createSubmission(
                        ef.bag().withTaskId(taskId).withStudentId(studentId)
                                .withDto(SubmissionDto.builder().additionalScore(5d).build())
                );

                int newAdditionalScore = 10;

                RequestContext<ObjectNode> ctx = getPatchSubmissionRequest(taskId, studentId, newAdditionalScore);
                ObjectNode request = ctx.getRequest();

                securePerform(patch("/submissions/{id}", submissionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isOk())
                        .andExpectAll(ctx.getMatchers());

                securePerform(get("/submissions/{id}", submissionId))
                        .andExpect(status().isOk())
                        .andExpectAll(ctx.getMatchers());
            }
        };
    }

    @Test
    void patchSubmission__otherAsAdmin__valid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                long courseId = ef.createCourse();
                long taskId = ef.createTask(ef.bag().withCourseId(courseId));
                long studentId = ef.createStudent(ef.bag().withCourseId(courseId));
                long submissionId = ef.createSubmission(
                        ef.bag().withTaskId(taskId).withStudentId(studentId)
                                .withDto(SubmissionDto.builder().additionalScore(5d).build())
                );

                int newAdditionalScore = 10;

                RequestContext<ObjectNode> ctx = getPatchSubmissionRequest(taskId, studentId, newAdditionalScore);
                ObjectNode request = ctx.getRequest();

                securePerform(patch("/submissions/{id}", submissionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isOk())
                        .andExpectAll(ctx.getMatchers());

                securePerform(get("/submissions/{id}", submissionId))
                        .andExpect(status().isOk())
                        .andExpectAll(ctx.getMatchers());
            }
        };
    }

    @Test
    void patchSubmission__otherAsTeacher__invalid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                long courseId = ef.createCourse();
                long taskId = ef.createTask(ef.bag().withCourseId(courseId));
                long studentId = ef.createStudent(ef.bag().withCourseId(courseId));
                long submissionId = ef.createSubmission(
                        ef.bag().withTaskId(taskId).withStudentId(studentId)
                                .withDto(SubmissionDto.builder().additionalScore(5d).build())
                );

                int newAdditionalScore = 10;

                RequestContext<ObjectNode> ctx = getPatchSubmissionRequest(taskId, studentId, newAdditionalScore);
                ObjectNode request = ctx.getRequest();

                securePerform(patch("/submissions/{id}", submissionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isForbidden());
            }
        };
    }

    @Test
    void patchSubmission__withCourseToken__invalid() {
        new WithCourseToken() {
            @Override
            void run() throws Exception {
                long courseId = getCourseId();
                long taskId = ef.createTask(ef.bag().withCourseId(courseId));
                long studentId = ef.createStudent(ef.bag().withCourseId(courseId));
                long submissionId = ef.createSubmission(
                        ef.bag().withTaskId(taskId).withStudentId(studentId)
                                .withDto(SubmissionDto.builder().additionalScore(5d).build())
                );

                int newAdditionalScore = 10;

                RequestContext<ObjectNode> ctx = getPatchSubmissionRequest(taskId, studentId, newAdditionalScore);
                ObjectNode request = ctx.getRequest();

                securePerform(patch("/submissions/{id}", submissionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isForbidden());
            }
        };
    }

    @Test
    void patchSubmission__taskChange__invalid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                long courseId = ef.createCourse();
                long taskId = ef.createTask(ef.bag().withCourseId(courseId));
                long studentId = ef.createStudent(ef.bag().withCourseId(courseId));
                long submissionId = ef.createSubmission(
                        ef.bag().withTaskId(taskId).withStudentId(studentId)
                                .withDto(SubmissionDto.builder().additionalScore(5d).build())
                );

                int newAdditionalScore = 10;
                long newTaskId = ef.createTask(ef.bag().withCourseId(courseId));

                RequestContext<ObjectNode> ctx = getPatchSubmissionRequest(newTaskId, studentId, newAdditionalScore);
                ObjectNode request = ctx.getRequest();

                securePerform(patch("/submissions/{id}", submissionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isBadRequest());
            }
        };
    }

    @Test
    void patchSubmission__studentChange__invalid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                long courseId = ef.createCourse();
                long taskId = ef.createTask(ef.bag().withCourseId(courseId));
                long studentId = ef.createStudent(ef.bag().withCourseId(courseId));
                long submissionId = ef.createSubmission(
                        ef.bag().withTaskId(taskId).withStudentId(studentId)
                                .withDto(SubmissionDto.builder().additionalScore(5d).build())
                );

                int newAdditionalScore = 10;
                long newStudentId = ef.createStudent(ef.bag().withCourseId(courseId));

                RequestContext<ObjectNode> ctx = getPatchSubmissionRequest(taskId, newStudentId, newAdditionalScore);
                ObjectNode request = ctx.getRequest();

                securePerform(patch("/submissions/{id}", submissionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isBadRequest());
            }
        };
    }

    @Test
    void patchSubmission__mainScoreChange__noEffect() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                long courseId = ef.createCourse();
                long taskId = ef.createTask(ef.bag().withCourseId(courseId));
                long studentId = ef.createStudent(ef.bag().withCourseId(courseId));
                long submissionId = ef.createSubmission(
                        ef.bag().withTaskId(taskId).withStudentId(studentId)
                                .withDto(SubmissionDto.builder().additionalScore(5d).build())
                );

                int newAdditionalScore = 10;
                int mainScore = 100500;

                RequestContext<ObjectNode> ctx = getPatchSubmissionRequest(taskId, studentId, newAdditionalScore);


                ObjectNode request = ctx.getRequest()
                        .put("mainScore", mainScore);

                var mainScoreMatcher = new TestUtils.NotEqualsMatcher(100500d);

                securePerform(patch("/submissions/{id}", submissionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.mainScore").value(mainScoreMatcher))
                        .andExpectAll(ctx.getMatchers());

                securePerform(get("/submissions/{id}", submissionId))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.mainScore").value(mainScoreMatcher))
                        .andExpectAll(ctx.getMatchers());
            }
        };
    }

    @Test
    void patchSubmission__criteriaFromDifferentTasks__invalid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                long courseId = ef.createCourse();
                long taskId = ef.createTask(ef.bag().withCourseId(courseId));
                long studentId = ef.createStudent(ef.bag().withCourseId(courseId));
                long submissionId = ef.createSubmission(ef.bag().withTaskId(taskId).withStudentId(studentId));
                int additionalScore = 5;

                var ctx = getPatchSubmissionRequest(taskId, studentId, additionalScore);

                long additionalCriteriaId = ef.createCriteria(ef.bag().withCourseId(courseId));

                ObjectNode request = ctx.getRequest();
                ArrayNode array = request.putArray("satisfiedCriteria");
                array.add(additionalCriteriaId);

                securePerform(patch("/submissions/{id}", submissionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isBadRequest());
            }
        };
    }

    @Test
    void patchSubmission__notExists__invalid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                long courseId = ef.createCourse();
                long taskId = ef.createTask(ef.bag().withCourseId(courseId));
                long studentId = ef.createStudent(ef.bag().withCourseId(courseId));
                long submissionId = ef.createSubmission(
                        ef.bag().withTaskId(taskId).withStudentId(studentId)
                                .withDto(SubmissionDto.builder().additionalScore(5d).build())
                );

                int newAdditionalScore = 10;

                RequestContext<ObjectNode> ctx = getPatchSubmissionRequest(taskId, studentId, newAdditionalScore);
                ObjectNode request = ctx.getRequest();

                securePerform(patch("/submissions/{id}", submissionId + 1000)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isNotFound());
            }
        };
    }

    @Test
    void patchSubmission__notAuthenticated__invalid() throws Exception {
        long courseId = ef.createCourse();
        long taskId = ef.createTask(ef.bag().withCourseId(courseId));
        long studentId = ef.createStudent(ef.bag().withCourseId(courseId));
        long submissionId = ef.createSubmission(
                ef.bag().withTaskId(taskId).withStudentId(studentId)
                        .withDto(SubmissionDto.builder().additionalScore(5d).build())
        );

        int newAdditionalScore = 10;

        RequestContext<ObjectNode> ctx = getPatchSubmissionRequest(taskId, studentId, newAdditionalScore);
        ObjectNode request = ctx.getRequest();

        mvc.perform(patch("/submissions/{id}", submissionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                .andExpect(status().isUnauthorized());
    }

    // ================================================================================================================

    @Test
    void deleteSubmission__self__valid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                long submissionId = ef.createSubmission(getSelfEmployeeIdAsLong());

                securePerform(delete("/submissions/{id}", submissionId))
                        .andExpect(status().isOk());

                securePerform(get("/submissions/{id}", submissionId))
                        .andExpect(status().isNotFound());
            }
        };
    }

    @Test
    void deleteSubmission__otherAsAdmin__valid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                long submissionId = ef.createSubmission();

                securePerform(delete("/submissions/{id}", submissionId))
                        .andExpect(status().isOk());

                securePerform(get("/submissions/{id}", submissionId))
                        .andExpect(status().isNotFound());
            }
        };
    }

    @Test
    void deleteSubmission__otherAsTeacher__invalid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                long submissionId = ef.createSubmission();

                securePerform(delete("/submissions/{id}", submissionId))
                        .andExpect(status().isForbidden());
            }
        };
    }

    @Test
    void deleteSubmission__withCourseToken__invalid() {
        new WithCourseToken() {
            @Override
            void run() throws Exception {
                long submissionId = ef.createSubmission(ef.bag().withCourseId(getCourseId()));

                securePerform(delete("/submissions/{id}", submissionId))
                        .andExpect(status().isForbidden());
            }
        };
    }

    @Test
    void deleteSubmission__notFound__invalid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                long submissionId = ef.createSubmission(getSelfEmployeeIdAsLong());

                securePerform(delete("/submissions/{id}", submissionId + 1000))
                        .andExpect(status().isNotFound());
            }
        };
    }

    @Test
    void deleteSubmission__notAuthenticated__invalid() throws Exception {
        long submissionId = ef.createSubmission();

        mvc.perform(delete("/submissions/{id}", submissionId))
                .andExpect(status().isUnauthorized());
    }

    // ================================================================================================================

    @Test
    void createCriteriaUpdatesMainScore() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                // GIVEN
                long taskId = ef.createTask(ef.bag().withEmployeeId(getSelfEmployeeIdAsLong())
                        .withDto(TaskDto.builder().maxScore(100).deadlinesEnabled(false).build()));
                long criteriaId1 = ef.createCriteria(ef.bag().withTaskId(taskId)
                        .withDto(CriteriaDto.builder().criteriaPercent(50).build()));
                long submissionId = ef.createSubmission(ef.bag().withTaskId(taskId)
                        .withDto(SubmissionDto.builder().satisfiedCriteria(new ArrayList<>(List.of(criteriaId1))).build()));

                securePerform(get("/submissions/{id}", submissionId))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.mainScore").value(100));

                // WHEN
                securePerform(post("/criteria/", criteriaId1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.createObjectNode()
                                .put("name", faker.lorem().word())
                                .put("task", taskId)
                                .put("criteriaPercent", 50)
                                .toString()
                        ))
                        .andExpect(status().isCreated());

                // THEN
                securePerform(get("/submissions/{id}", submissionId))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.mainScore").value(50));
            }
        };
    }

    @Test
    void putCriteriaUpdatesMainScore() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                // GIVEN
                long taskId = ef.createTask(ef.bag().withEmployeeId(getSelfEmployeeIdAsLong())
                        .withDto(TaskDto.builder().maxScore(100).deadlinesEnabled(false).build()));
                long criteriaId1 = ef.createCriteria(ef.bag().withTaskId(taskId)
                        .withDto(CriteriaDto.builder().criteriaPercent(60).build()));
                long criteriaId2 = ef.createCriteria(ef.bag().withTaskId(taskId)
                        .withDto(CriteriaDto.builder().criteriaPercent(40).build()));
                long submissionId = ef.createSubmission(ef.bag().withTaskId(taskId)
                        .withDto(SubmissionDto.builder().satisfiedCriteria(new ArrayList<>(List.of(criteriaId1))).build()));

                securePerform(get("/submissions/{id}", submissionId))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.mainScore").value(60));

                // WHEN
                securePerform(put("/criteria/{id}", criteriaId1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.createObjectNode()
                                .put("name", faker.lorem().word())
                                .put("task", taskId)
                                .put("criteriaPercent", 80)
                                .toString()
                        ))
                        .andExpect(status().isOk());

                securePerform(put("/criteria/{id}", criteriaId2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.createObjectNode()
                                .put("name", faker.lorem().word())
                                .put("task", taskId)
                                .put("criteriaPercent", 20)
                                .toString()
                        ))
                        .andExpect(status().isOk());

                // THEN
                securePerform(get("/submissions/{id}", submissionId))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.mainScore").value(80));
            }
        };
    }

    @Test
    void patchCriteriaUpdatesMainScore() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                // GIVEN
                long taskId = ef.createTask(ef.bag().withEmployeeId(getSelfEmployeeIdAsLong())
                        .withDto(TaskDto.builder().maxScore(100).deadlinesEnabled(false).build()));
                long criteriaId1 = ef.createCriteria(ef.bag().withTaskId(taskId)
                        .withDto(CriteriaDto.builder().criteriaPercent(60).build()));
                long criteriaId2 = ef.createCriteria(ef.bag().withTaskId(taskId)
                        .withDto(CriteriaDto.builder().criteriaPercent(40).build()));
                long submissionId = ef.createSubmission(ef.bag().withTaskId(taskId)
                        .withDto(SubmissionDto.builder().satisfiedCriteria(new ArrayList<>(List.of(criteriaId1))).build()));

                securePerform(get("/submissions/{id}", submissionId))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.mainScore").value(60));

                // WHEN
                securePerform(patch("/criteria/{id}", criteriaId1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.createObjectNode()
                                .put("criteriaPercent", 80)
                                .toString()
                        ))
                        .andExpect(status().isOk());

                securePerform(patch("/criteria/{id}", criteriaId2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.createObjectNode()
                                .put("criteriaPercent", 20)
                                .toString()
                        ))
                        .andExpect(status().isOk());

                // THEN
                securePerform(get("/submissions/{id}", submissionId))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.mainScore").value(80));
            }
        };
    }

    @Test
    void putTaskUpdatesMainScore() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                // GIVEN
                long courseId = ef.createCourse(getSelfEmployeeIdAsLong());
                long taskId = ef.createTask(ef.bag().withCourseId(courseId)
                        .withDto(TaskDto.builder().maxScore(100).deadlinesEnabled(false).build()));
                long criteriaId1 = ef.createCriteria(ef.bag().withTaskId(taskId)
                        .withDto(CriteriaDto.builder().criteriaPercent(60).build()));
                ef.createCriteria(ef.bag().withTaskId(taskId)
                        .withDto(CriteriaDto.builder().criteriaPercent(40).build()));
                long submissionId = ef.createSubmission(ef.bag().withTaskId(taskId)
                        .withDto(SubmissionDto.builder()
                                .submittedAt(ZonedDateTime.now())
                                .satisfiedCriteria(new ArrayList<>(List.of(criteriaId1)))
                                .build()
                        )
                );

                securePerform(get("/submissions/{id}", submissionId))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.mainScore").value(60));

                // WHEN
                securePerform(put("/tasks/{id}", taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.createObjectNode()
                                .put("title", faker.lorem().sentence())
                                .put("taskNumber", 1)
                                .put("course", courseId)
                                .put("maxScore", 100)
                                .put("deadlinesEnabled", true)
                                .put("softDeadlineAt", ZonedDateTime.now().minusDays(3).toString())
                                .put("hardDeadlineAt", ZonedDateTime.now().minusDays(2).toString())
                                .put("maxPenaltyPercent", 50)
                                .toString()
                        ))
                        .andExpect(status().isOk());

                // THEN
                securePerform(get("/submissions/{id}", submissionId))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.mainScore").value(30));
            }
        };
    }

    @Test
    void patchTaskUpdatesMainScore() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                // GIVEN
                long taskId = ef.createTask(ef.bag().withEmployeeId(getSelfEmployeeIdAsLong())
                        .withDto(TaskDto.builder().maxScore(100).deadlinesEnabled(false).build()));
                long criteriaId1 = ef.createCriteria(ef.bag().withTaskId(taskId)
                        .withDto(CriteriaDto.builder().criteriaPercent(60).build()));
                ef.createCriteria(ef.bag().withTaskId(taskId)
                        .withDto(CriteriaDto.builder().criteriaPercent(40).build()));
                long submissionId = ef.createSubmission(ef.bag().withTaskId(taskId)
                        .withDto(SubmissionDto.builder()
                                .submittedAt(ZonedDateTime.now())
                                .satisfiedCriteria(new ArrayList<>(List.of(criteriaId1)))
                                .build()
                        )
                );

                securePerform(get("/submissions/{id}", submissionId))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.mainScore").value(60));

                // WHEN
                securePerform(patch("/tasks/{id}", taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.createObjectNode()
                                .put("deadlinesEnabled", true)
                                .put("softDeadlineAt", ZonedDateTime.now().minusDays(3).toString())
                                .put("hardDeadlineAt", ZonedDateTime.now().minusDays(2).toString())
                                .put("maxPenaltyPercent", 50)
                                .toString()
                        ))
                        .andExpect(status().isOk());

                // THEN
                securePerform(get("/submissions/{id}", submissionId))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.mainScore").value(30));
            }
        };
    }

    // ================================================================================================================

    private List<Long> createManyCriteria(long taskId, int count) {
        return Stream.generate(() -> ef.createCriteria(ef.bag().withTaskId(taskId)))
                .limit(count)
                .toList();
    }

    private ResultMatcher[] getSubmissionMatchers(
            String prefix, long taskId, ZonedDateTime submittedAt, Double additionalScore, long studentId,
            List<Long> satisfiedCriteria
    ) {
        return getSubmissionMatchers(prefix, null, taskId, submittedAt, additionalScore, studentId, satisfiedCriteria);
    }

    private ResultMatcher[] getSubmissionMatchers(
            String prefix, Long id, long taskId, ZonedDateTime submittedAt, Double additionalScore, long studentId,
            List<Long> satisfiedCriteria
    ) {
        return new ResultMatcher[]{
                id != null ?
                        jsonPath(prefix + ".id").value(id) :
                        jsonPath(prefix + ".id").isNumber(),
                jsonPath(prefix + ".task").value(taskId),
                jsonPath(prefix + ".student").value(studentId),
                jsonPath(prefix + ".submittedAt").value(new TestUtils.DateMatcher(submittedAt)),
                jsonPath(prefix + ".mainScore").value(new TestUtils.GreaterThanMatcher(0)),
                jsonPath(prefix + ".additionalScore").value(additionalScore),
                satisfiedCriteria != null ?
                        jsonPath(prefix + ".satisfiedCriteria", containsInAnyOrder(satisfiedCriteria.stream()
                                .map(Math::toIntExact)
                                .toArray(Integer[]::new))) :
                        jsonPath(prefix + ".satisfiedCriteria").exists(),
        };
    }
}
