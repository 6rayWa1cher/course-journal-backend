package com.a6raywa1cher.coursejournalbackend.integration;

import com.a6raywa1cher.coursejournalbackend.RequestContext;
import com.a6raywa1cher.coursejournalbackend.dto.SubmissionDto;
import com.a6raywa1cher.coursejournalbackend.service.CourseService;
import com.a6raywa1cher.coursejournalbackend.service.SubmissionService;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.ResultMatcher;

import java.util.function.Function;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
public class SubmissionControllerIntegrationTests extends AbstractIntegrationTests {
    @Autowired
    CourseService courseService;

    @Autowired
    SubmissionService submissionService;

    @Test
    void getSubmissionById__self__valid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                String firstName = faker.name().firstName();
                String lastName = faker.name().lastName();
                long courseId = ef.createCourse(getIdAsLong());

                long id = studentService.create(SubmissionDto.builder()
                        .firstName(firstName)
                        .lastName(lastName)
                        .course(courseId)
                        .build()).getId();

                ResultMatcher[] matchers = {
                        jsonPath("$.id").value(id),
                        jsonPath("$.course").value(courseId),
                        jsonPath("$.firstName").value(firstName),
                        jsonPath("$.lastName").value(lastName),
                };

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
                String firstName = faker.name().firstName();
                String lastName = faker.name().lastName();
                long courseId = ef.createCourse();

                long id = studentService.create(SubmissionDto.builder()
                        .firstName(firstName)
                        .lastName(lastName)
                        .course(courseId)
                        .build()).getId();

                ResultMatcher[] matchers = {
                        jsonPath("$.id").value(id),
                        jsonPath("$.course").value(courseId),
                        jsonPath("$.firstName").value(firstName),
                        jsonPath("$.lastName").value(lastName),
                };

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
                String firstName = faker.name().firstName();
                String lastName = faker.name().lastName();
                long courseId = ef.createCourse();

                long id = studentService.create(SubmissionDto.builder()
                        .firstName(firstName)
                        .lastName(lastName)
                        .course(courseId)
                        .build()).getId();

                securePerform(get("/submissions/{id}", id))
                        .andExpect(status().isForbidden());
            }
        };
    }

    @Test
    void getSubmissionById__notAuthenticated__invalid() throws Exception {
        String firstName = faker.name().firstName();
        String lastName = faker.name().lastName();
        long courseId = ef.createCourse();

        long id = studentService.create(SubmissionDto.builder()
                .firstName(firstName)
                .lastName(lastName)
                .course(courseId)
                .build()).getId();

        mvc.perform(get("/submissions/{id}", id))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getSubmissionById__notExists__invalid() {
        String firstName = faker.name().firstName();
        String lastName = faker.name().lastName();
        long courseId = ef.createCourse();

        long id = studentService.create(SubmissionDto.builder()
                .firstName(firstName)
                .lastName(lastName)
                .course(courseId)
                .build()).getId();

        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                securePerform(get("/submissions/{id}", id + 1000))
                        .andExpect(status().isNotFound());
            }
        };
    }

    // ================================================================================================================

    @Test
    void getSubmissionByCourse__self__valid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                long courseId1 = ef.createCourse(getIdAsLong());
                long courseId2 = ef.createCourse(getIdAsLong());

                String firstName1 = "A" + faker.name().firstName();
                String firstName2 = "B" + faker.name().firstName();

                studentService.create(SubmissionDto.builder()
                        .firstName(firstName2)
                        .lastName(faker.name().lastName())
                        .course(courseId1)
                        .build());

                studentService.create(SubmissionDto.builder()
                        .firstName(firstName1)
                        .lastName(faker.name().lastName())
                        .course(courseId1)
                        .build());

                studentService.create(SubmissionDto.builder()
                        .firstName(firstName1)
                        .lastName(faker.name().lastName())
                        .course(courseId2)
                        .build());

                securePerform(get("/submissions/course/{id}", courseId1)
                        .queryParam("sort", "firstName,asc"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.totalElements").value(2))
                        .andExpect(jsonPath("$.content[0].firstName").value(firstName1))
                        .andExpect(jsonPath("$.content[1].firstName").value(firstName2));
            }
        };
    }

    @Test
    void getSubmissionByCourse__otherAsAdmin__valid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                long courseId1 = ef.createCourse();
                long courseId2 = ef.createCourse();

                String firstName1 = "A" + faker.name().firstName();
                String firstName2 = "B" + faker.name().firstName();

                studentService.create(SubmissionDto.builder()
                        .firstName(firstName2)
                        .lastName(faker.name().lastName())
                        .course(courseId1)
                        .build());

                studentService.create(SubmissionDto.builder()
                        .firstName(firstName1)
                        .lastName(faker.name().lastName())
                        .course(courseId1)
                        .build());

                studentService.create(SubmissionDto.builder()
                        .firstName(firstName1)
                        .lastName(faker.name().lastName())
                        .course(courseId2)
                        .build());

                securePerform(get("/submissions/course/{id}", courseId1)
                        .queryParam("sort", "firstName,asc"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.totalElements").value(2))
                        .andExpect(jsonPath("$.content[0].firstName").value(firstName1))
                        .andExpect(jsonPath("$.content[1].firstName").value(firstName2));
            }
        };
    }

    @Test
    void getSubmissionByCourse__otherAsTeacher__invalid() {
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
    void getSubmissionByCourse__notAuthenticated__invalid() throws Exception {
        long id = ef.createCourse();
        mvc.perform(get("/submissions/course/{id}", id)).andExpect(status().isUnauthorized());
    }

    // ================================================================================================================

    @Test
    void getSubmissionByStudentAndCourse__self__valid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                long courseId1 = ef.createCourse(getIdAsLong());
                long courseId2 = ef.createCourse(getIdAsLong());

                String firstName1 = "A" + faker.name().firstName();
                String firstName2 = "B" + faker.name().firstName();

                studentService.create(SubmissionDto.builder()
                        .firstName(firstName2)
                        .lastName(faker.name().lastName())
                        .course(courseId1)
                        .build());

                studentService.create(SubmissionDto.builder()
                        .firstName(firstName1)
                        .lastName(faker.name().lastName())
                        .course(courseId1)
                        .build());

                studentService.create(SubmissionDto.builder()
                        .firstName(firstName1)
                        .lastName(faker.name().lastName())
                        .course(courseId2)
                        .build());

                securePerform(get("/submissions/course/{id}", courseId1)
                        .queryParam("sort", "firstName,asc"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.totalElements").value(2))
                        .andExpect(jsonPath("$.content[0].firstName").value(firstName1))
                        .andExpect(jsonPath("$.content[1].firstName").value(firstName2));
            }
        };
    }

    @Test
    void getSubmissionByStudentAndCourse__otherAsAdmin__valid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                long courseId1 = ef.createCourse();
                long courseId2 = ef.createCourse();

                String firstName1 = "A" + faker.name().firstName();
                String firstName2 = "B" + faker.name().firstName();

                studentService.create(SubmissionDto.builder()
                        .firstName(firstName2)
                        .lastName(faker.name().lastName())
                        .course(courseId1)
                        .build());

                studentService.create(SubmissionDto.builder()
                        .firstName(firstName1)
                        .lastName(faker.name().lastName())
                        .course(courseId1)
                        .build());

                studentService.create(SubmissionDto.builder()
                        .firstName(firstName1)
                        .lastName(faker.name().lastName())
                        .course(courseId2)
                        .build());

                securePerform(get("/submissions/course/{id}", courseId1)
                        .queryParam("sort", "firstName,asc"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.totalElements").value(2))
                        .andExpect(jsonPath("$.content[0].firstName").value(firstName1))
                        .andExpect(jsonPath("$.content[1].firstName").value(firstName2));
            }
        };
    }

    @Test
    void getSubmissionByStudentAndCourse__otherAsTeacher__invalid() {
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
    void getSubmissionByStudentAndCourse__notAuthenticated__invalid() throws Exception {
        long id = ef.createCourse();
        mvc.perform(get("/submissions/course/{id}", id)).andExpect(status().isUnauthorized());
    }

    // ================================================================================================================

    RequestContext<ObjectNode> getCreateSubmissionRequest(long courseId) {
        String firstName = faker.name().firstName();
        String lastName = faker.name().lastName();
        String middleName = faker.name().firstName();

        ObjectNode request = objectMapper.createObjectNode()
                .put("firstName", firstName)
                .put("middleName", middleName)
                .put("lastName", lastName)
                .put("course", courseId);

        Function<String, ResultMatcher[]> matchers = prefix -> new ResultMatcher[]{
                jsonPath("$.id").isNumber(),
                jsonPath("$.firstName").value(firstName),
                jsonPath("$.middleName").value(middleName),
                jsonPath("$.lastName").value(lastName),
                jsonPath("$.course").value(courseId)
        };

        return new RequestContext<>(request, matchers);
    }

    @Test
    void createSubmission__self__valid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                long courseId = ef.createCourse(getIdAsLong());
                var ctx = getCreateSubmissionRequest(courseId);

                ObjectNode request = ctx.getRequest();

                securePerform(post("/submissions/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isCreated())
                        .andExpectAll(ctx.getMatchers());

                securePerform(get("/submissions/course/{id}", courseId))
                        .andExpect(status().isOk())
                        .andExpectAll(content().string(containsString(request.get("firstName").asText())));
            }
        };
    }

    @Test
    void createSubmission__otherAsAdmin__valid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                long courseId = ef.createCourse();
                var ctx = getCreateSubmissionRequest(courseId);

                ObjectNode request = ctx.getRequest();

                securePerform(post("/submissions/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isCreated())
                        .andExpectAll(ctx.getMatchers());

                securePerform(get("/submissions/course/{id}", courseId))
                        .andExpect(status().isOk())
                        .andExpectAll(content().string(containsString(request.get("firstName").asText())));
            }
        };
    }

    @Test
    void createSubmission__otherAsTeacher__invalid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                long courseId = ef.createCourse();
                var ctx = getCreateSubmissionRequest(courseId);

                ObjectNode request = ctx.getRequest();

                securePerform(post("/submissions/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isForbidden());
            }
        };
    }

    @Test
    void createSubmission__notAuthenticated__invalid() throws Exception {
        long courseId = ef.createCourse();
        var ctx = getCreateSubmissionRequest(courseId);

        ObjectNode request = ctx.getRequest();

        mvc.perform(post("/submissions/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                .andExpect(status().isUnauthorized());
    }

    // ================================================================================================================

    RequestContext<ObjectNode> getBatchCreateSubmissionRequest(long courseId) {
        String firstName1 = faker.name().firstName();
        String lastName1 = "A" + faker.name().lastName();
        String middleName1 = faker.name().firstName();

        String firstName2 = faker.name().firstName();
        String lastName2 = "B" + faker.name().lastName();
        String middleName2 = faker.name().firstName();

        ObjectNode request = objectMapper.createObjectNode()
                .put("course", courseId);

        request.putArray("students")
                .add(objectMapper.createObjectNode()
                        .put("firstName", firstName1)
                        .put("middleName", middleName1)
                        .put("lastName", lastName1))
                .add(objectMapper.createObjectNode()
                        .put("firstName", firstName2)
                        .put("middleName", middleName2)
                        .put("lastName", lastName2));

        return new RequestContext<>(request,
                prefix -> new ResultMatcher[]{
                        jsonPath(prefix + "[0].firstName").value(firstName1),
                        jsonPath(prefix + "[0].middleName").value(middleName1),
                        jsonPath(prefix + "[0].lastName").value(lastName1),
                        jsonPath(prefix + "[1].firstName").value(firstName2),
                        jsonPath(prefix + "[1].middleName").value(middleName2),
                        jsonPath(prefix + "[1].lastName").value(lastName2)
                });
    }

    @Test
    void batchCreateSubmission__self__valid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                long courseId = ef.createCourse(getIdAsLong());
                RequestContext<ObjectNode> ctx = getBatchCreateSubmissionRequest(courseId);

                ObjectNode request = ctx.getRequest();

                securePerform(post("/submissions/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isCreated())
                        .andExpect(jsonPath("$", hasSize(2)))
                        .andExpectAll(ctx.getMatchers());

                securePerform(get("/submissions/course/{id}", courseId)
                        .queryParam("sort", "lastName,asc"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.totalElements").value(2))
                        .andExpectAll(ctx.getMatchers("$.content"));
            }
        };
    }

    @Test
    void batchCreateSubmission__otherAsAdmin__valid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                long courseId = ef.createCourse();
                RequestContext<ObjectNode> ctx = getBatchCreateSubmissionRequest(courseId);

                ObjectNode request = ctx.getRequest();

                securePerform(post("/submissions/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isCreated())
                        .andExpect(jsonPath("$", hasSize(2)))
                        .andExpectAll(ctx.getMatchers());

                securePerform(get("/submissions/course/{id}", courseId)
                        .queryParam("sort", "lastName,asc"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.totalElements").value(2))
                        .andExpectAll(ctx.getMatchers("$.content"));
            }
        };
    }

    @Test
    void batchCreateSubmission__otherAsTeacher__invalid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                long courseId = ef.createCourse();
                RequestContext<ObjectNode> ctx = getBatchCreateSubmissionRequest(courseId);

                ObjectNode request = ctx.getRequest();

                securePerform(post("/submissions/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isForbidden());
            }
        };
    }

    @Test
    void batchCreateSubmission__notAuthenticated__invalid() throws Exception {
        long courseId = ef.createCourse();
        RequestContext<ObjectNode> ctx = getBatchCreateSubmissionRequest(courseId);

        ObjectNode request = ctx.getRequest();

        mvc.perform(post("/submissions/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                .andExpect(status().isUnauthorized());
    }

    // ================================================================================================================

    RequestContext<ObjectNode> getPutSubmissionRequest(long courseId) {
        String firstName = faker.name().firstName();
        String lastName = faker.name().lastName();
        String middleName = faker.name().firstName();

        ObjectNode request = objectMapper.createObjectNode()
                .put("firstName", firstName)
                .put("middleName", middleName)
                .put("lastName", lastName)
                .put("course", courseId);

        Function<String, ResultMatcher[]> matchers = prefix -> new ResultMatcher[]{
                jsonPath("$.id").isNumber(),
                jsonPath("$.firstName").value(firstName),
                jsonPath("$.middleName").value(middleName),
                jsonPath("$.lastName").value(lastName),
                jsonPath("$.course").value(courseId)
        };

        return new RequestContext<>(request, matchers);
    }

    @Test
    void putSubmission__self__valid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                long courseId = ef.createCourse(getIdAsLong());
                long studentId = ef.createSubmission(ef.bag().withCourseId(courseId));

                RequestContext<ObjectNode> ctx = getPutSubmissionRequest(courseId);
                ObjectNode request = ctx.getRequest();

                securePerform(put("/submissions/{id}", studentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isOk())
                        .andExpectAll(ctx.getMatchers());

                securePerform(get("/submissions/{id}", studentId))
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
                long studentId = ef.createSubmission(ef.bag().withCourseId(courseId));

                RequestContext<ObjectNode> ctx = getPutSubmissionRequest(courseId);
                ObjectNode request = ctx.getRequest();

                securePerform(put("/submissions/{id}", studentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isOk())
                        .andExpectAll(ctx.getMatchers());

                securePerform(get("/submissions/{id}", studentId))
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
                long studentId = ef.createSubmission(ef.bag().withCourseId(courseId));

                RequestContext<ObjectNode> ctx = getPutSubmissionRequest(courseId);
                ObjectNode request = ctx.getRequest();

                securePerform(put("/submissions/{id}", studentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isForbidden());
            }
        };
    }

    @Test
    void putSubmission__courseChange__invalid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                long courseId1 = ef.createCourse();
                long courseId2 = ef.createCourse();
                long studentId = ef.createSubmission(ef.bag().withCourseId(courseId1));

                RequestContext<ObjectNode> ctx = getPutSubmissionRequest(courseId2);
                ObjectNode request = ctx.getRequest();

                securePerform(put("/submissions/{id}", studentId)
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
                long studentId = ef.createSubmission(ef.bag().withCourseId(courseId));

                RequestContext<ObjectNode> ctx = getPutSubmissionRequest(courseId);
                ObjectNode request = ctx.getRequest();

                securePerform(put("/submissions/{id}", studentId + 1000)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isNotFound());
            }
        };
    }

    @Test
    void putSubmission__notAuthenticated__invalid() throws Exception {
        long courseId = ef.createCourse();
        long studentId = ef.createSubmission(ef.bag().withCourseId(courseId));

        RequestContext<ObjectNode> ctx = getPutSubmissionRequest(courseId);
        ObjectNode request = ctx.getRequest();

        mvc.perform(put("/submissions/{id}", studentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                .andExpect(status().isUnauthorized());
    }

    // ================================================================================================================

    RequestContext<ObjectNode> getPatchSubmissionRequest(long courseId, String middleName, String lastName) {
        String firstName = faker.name().firstName();

        ObjectNode request = objectMapper.createObjectNode()
                .put("firstName", firstName)
                .put("course", courseId);

        Function<String, ResultMatcher[]> matchers = prefix -> new ResultMatcher[]{
                jsonPath("$.id").isNumber(),
                jsonPath("$.firstName").value(firstName),
                jsonPath("$.middleName").value(middleName),
                jsonPath("$.lastName").value(lastName),
                jsonPath("$.course").value(courseId)
        };

        return new RequestContext<>(request, matchers);
    }

    @Test
    void patchSubmission__self__valid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                long courseId = ef.createCourse(getIdAsLong());

                String lastName = faker.name().lastName();
                String middleName = faker.name().firstName();

                long studentId = ef.createSubmission(ef.bag()
                        .withCourseId(courseId)
                        .withDto(SubmissionDto.builder()
                                .lastName(lastName)
                                .middleName(middleName)
                                .build()
                        ));

                RequestContext<ObjectNode> ctx = getPatchSubmissionRequest(courseId, middleName, lastName);
                ObjectNode request = ctx.getRequest();

                securePerform(patch("/submissions/{id}", studentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isOk())
                        .andExpectAll(ctx.getMatchers());

                securePerform(get("/submissions/{id}", studentId))
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

                String lastName = faker.name().lastName();
                String middleName = faker.name().firstName();

                long studentId = ef.createSubmission(ef.bag()
                        .withCourseId(courseId)
                        .withDto(SubmissionDto.builder()
                                .lastName(lastName)
                                .middleName(middleName)
                                .build()
                        ));

                RequestContext<ObjectNode> ctx = getPatchSubmissionRequest(courseId, middleName, lastName);
                ObjectNode request = ctx.getRequest();

                securePerform(patch("/submissions/{id}", studentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isOk())
                        .andExpectAll(ctx.getMatchers());

                securePerform(get("/submissions/{id}", studentId))
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

                String lastName = faker.name().lastName();
                String middleName = faker.name().firstName();

                long studentId = ef.createSubmission(ef.bag()
                        .withCourseId(courseId)
                        .withDto(SubmissionDto.builder()
                                .lastName(lastName)
                                .middleName(middleName)
                                .build()
                        ));

                RequestContext<ObjectNode> ctx = getPatchSubmissionRequest(courseId, middleName, lastName);
                ObjectNode request = ctx.getRequest();

                securePerform(patch("/submissions/{id}", studentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isForbidden());
            }
        };
    }

    @Test
    void patchSubmission__courseChange__invalid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                long courseId1 = ef.createCourse();
                long courseId2 = ef.createCourse();

                String lastName = faker.name().lastName();
                String middleName = faker.name().firstName();

                long studentId = ef.createSubmission(ef.bag()
                        .withCourseId(courseId1)
                        .withDto(SubmissionDto.builder()
                                .lastName(lastName)
                                .middleName(middleName)
                                .build()
                        ));

                RequestContext<ObjectNode> ctx = getPatchSubmissionRequest(courseId2, middleName, lastName);
                ObjectNode request = ctx.getRequest();

                securePerform(patch("/submissions/{id}", studentId)
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

                String lastName = faker.name().lastName();
                String middleName = faker.name().firstName();

                long studentId = ef.createSubmission(ef.bag()
                        .withCourseId(courseId)
                        .withDto(SubmissionDto.builder()
                                .lastName(lastName)
                                .middleName(middleName)
                                .build()
                        ));

                RequestContext<ObjectNode> ctx = getPatchSubmissionRequest(courseId, middleName, lastName);
                ObjectNode request = ctx.getRequest();

                securePerform(patch("/submissions/{id}", studentId + 1000)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isNotFound());
            }
        };
    }

    @Test
    void patchSubmission__notAuthenticated__invalid() throws Exception {
        long courseId = ef.createCourse();

        String lastName = faker.name().lastName();
        String middleName = faker.name().firstName();

        long studentId = ef.createSubmission(ef.bag()
                .withCourseId(courseId)
                .withDto(SubmissionDto.builder()
                        .lastName(lastName)
                        .middleName(middleName)
                        .build()
                ));

        RequestContext<ObjectNode> ctx = getPatchSubmissionRequest(courseId, middleName, lastName);
        ObjectNode request = ctx.getRequest();

        mvc.perform(patch("/submissions/{id}", studentId)
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
                long studentId = ef.createSubmission(getIdAsLong());

                securePerform(delete("/submissions/{id}", studentId))
                        .andExpect(status().isOk());

                securePerform(get("/submissions/{id}", studentId))
                        .andExpect(status().isNotFound());
            }
        };
    }

    @Test
    void deleteSubmission__otherAsAdmin__valid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                long studentId = ef.createSubmission();

                securePerform(delete("/submissions/{id}", studentId))
                        .andExpect(status().isOk());

                securePerform(get("/submissions/{id}", studentId))
                        .andExpect(status().isNotFound());
            }
        };
    }

    @Test
    void deleteSubmission__otherAsTeacher__invalid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                long studentId = ef.createSubmission();

                securePerform(delete("/submissions/{id}", studentId))
                        .andExpect(status().isForbidden());
            }
        };
    }

    @Test
    void deleteSubmission__notFound__invalid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                long studentId = ef.createSubmission(getIdAsLong());

                securePerform(delete("/submissions/{id}", studentId + 1000))
                        .andExpect(status().isNotFound());
            }
        };
    }

    @Test
    void deleteSubmission__notAuthenticated__invalid() throws Exception {
        long studentId = ef.createSubmission();

        mvc.perform(delete("/submissions/{id}", studentId))
                .andExpect(status().isUnauthorized());
    }
}
