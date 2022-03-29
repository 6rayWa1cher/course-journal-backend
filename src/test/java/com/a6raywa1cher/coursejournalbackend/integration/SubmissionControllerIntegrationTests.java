package com.a6raywa1cher.coursejournalbackend.integration;

import com.a6raywa1cher.coursejournalbackend.RequestContext;
import com.a6raywa1cher.coursejournalbackend.TestUtils;
import com.a6raywa1cher.coursejournalbackend.dto.SubmissionDto;
import com.a6raywa1cher.coursejournalbackend.service.SubmissionService;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jayway.jsonpath.JsonPath;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultMatcher;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
public class SubmissionControllerIntegrationTests extends AbstractIntegrationTests {
    @Autowired
    SubmissionService submissionService;

    RequestContext<Long> createGetSubmissionByIdContext(long userId) {
        long courseId = ef.createCourse(userId);

        ZonedDateTime submittedAt = ZonedDateTime.now().minusDays(1);
        int additionalScore = faker.number().numberBetween(0, 5);
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
                .build();
    }

    @Test
    void getSubmissionById__self__valid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                var ctx = createGetSubmissionByIdContext(getIdAsLong());

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
                var ctx = createGetSubmissionByIdContext(ef.createUser());

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
                var ctx = createGetSubmissionByIdContext(ef.createUser());

                long id = ctx.getRequest();

                securePerform(get("/submissions/{id}", id))
                        .andExpect(status().isForbidden());
            }
        };
    }

    @Test
    void getSubmissionById__notAuthenticated__invalid() throws Exception {
        var ctx = createGetSubmissionByIdContext(ef.createUser());

        long id = ctx.getRequest();

        mvc.perform(get("/submissions/{id}", id))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getSubmissionById__notExists__invalid() {
        var ctx = createGetSubmissionByIdContext(ef.createUser());

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
        int additionalScore = faker.number().numberBetween(0, 5);
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
                long courseId1 = ef.createCourse(getIdAsLong());
                long courseId2 = ef.createCourse(getIdAsLong());
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
                long userId = ef.createUser();
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
    void getSubmissionsByCourse__notAuthenticated__invalid() throws Exception {
        long id = ef.createCourse();
        mvc.perform(get("/submissions/course/{id}", id)).andExpect(status().isUnauthorized());
    }

    // ================================================================================================================

    RequestContext<Long> createGetSubmissionsByTaskContext(long taskId, long studentId) {
        ZonedDateTime submittedAt = ZonedDateTime.now().minusDays(1);
        int additionalScore = faker.number().numberBetween(0, 5);
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
                long courseId = ef.createCourse(getIdAsLong());
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
    void getSubmissionsByTask__notAuthenticated__invalid() throws Exception {
        long id = ef.createTask();
        mvc.perform(get("/submissions/task/{id}", id)).andExpect(status().isUnauthorized());
    }

    // ================================================================================================================

    RequestContext<Long> createGetSubmissionsByStudentAndCourseContext(long taskId, long studentId) {
        ZonedDateTime submittedAt = ZonedDateTime.now().minusDays(1);
        int additionalScore = faker.number().numberBetween(0, 5);
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
                long courseId1 = ef.createCourse(getIdAsLong());
                long courseId2 = ef.createCourse(getIdAsLong());
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
        int additionalScore = faker.number().numberBetween(2, 5);

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
                long courseId = ef.createCourse(getIdAsLong());
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
    void createSubmission__mainScore__noEffect() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                long courseId = ef.createCourse(getIdAsLong());
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
                long courseId = ef.createCourse(getIdAsLong());
                long taskId = ef.createTask(ef.bag().withCourseId(courseId));
                long studentId = ef.createStudent(ef.bag().withCourseId(courseId));
                long submissionId = ef.createSubmission(
                        ef.bag().withTaskId(taskId).withStudentId(studentId)
                                .withDto(SubmissionDto.builder().additionalScore(5).build())
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
                                .withDto(SubmissionDto.builder().additionalScore(5).build())
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
                                .withDto(SubmissionDto.builder().additionalScore(5).build())
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
                                .withDto(SubmissionDto.builder().additionalScore(5).build())
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
                                .withDto(SubmissionDto.builder().additionalScore(5).build())
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
                                .withDto(SubmissionDto.builder().additionalScore(5).build())
                );

                int newAdditionalScore = 10;
                int mainScore = 100500;

                RequestContext<ObjectNode> ctx = getPutSubmissionRequest(taskId, studentId, newAdditionalScore);


                ObjectNode request = ctx.getRequest()
                        .put("mainScore", mainScore);

                var mainScoreMatcher = new BaseMatcher<Integer>() {
                    @Override
                    public boolean matches(Object actual) {
                        return ((int) actual) != 100500;
                    }

                    @Override
                    public void describeTo(Description description) {
                        description.appendValue(this);
                    }
                };

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
                                .withDto(SubmissionDto.builder().additionalScore(5).build())
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
                        .withDto(SubmissionDto.builder().additionalScore(5).build())
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
                jsonPath(prefix + ".satisfiedCriteria").isNotEmpty()
        };

        return new RequestContext<>(request, matchers);
    }

    @Test
    void patchSubmission__self__valid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                long courseId = ef.createCourse(getIdAsLong());
                long taskId = ef.createTask(ef.bag().withCourseId(courseId));
                long studentId = ef.createStudent(ef.bag().withCourseId(courseId));
                long submissionId = ef.createSubmission(
                        ef.bag().withTaskId(taskId).withStudentId(studentId)
                                .withDto(SubmissionDto.builder().additionalScore(5).build())
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
                                .withDto(SubmissionDto.builder().additionalScore(5).build())
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
                                .withDto(SubmissionDto.builder().additionalScore(5).build())
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
                                .withDto(SubmissionDto.builder().additionalScore(5).build())
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
                                .withDto(SubmissionDto.builder().additionalScore(5).build())
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
                                .withDto(SubmissionDto.builder().additionalScore(5).build())
                );

                int newAdditionalScore = 10;
                int mainScore = 100500;

                RequestContext<ObjectNode> ctx = getPatchSubmissionRequest(taskId, studentId, newAdditionalScore);


                ObjectNode request = ctx.getRequest()
                        .put("mainScore", mainScore);

                var mainScoreMatcher = new BaseMatcher<Integer>() {
                    @Override
                    public boolean matches(Object actual) {
                        return ((int) actual) != 100500;
                    }

                    @Override
                    public void describeTo(Description description) {
                        description.appendValue(this);
                    }
                };

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
                                .withDto(SubmissionDto.builder().additionalScore(5).build())
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
                        .withDto(SubmissionDto.builder().additionalScore(5).build())
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
                long submissionId = ef.createSubmission(getIdAsLong());

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
    void deleteSubmission__notFound__invalid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                long submissionId = ef.createSubmission(getIdAsLong());

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

    private List<Long> createManyCriteria(long taskId, int count) {
        return Stream.generate(() -> ef.createCriteria(ef.bag().withTaskId(taskId)))
                .limit(count)
                .toList();
    }
}
