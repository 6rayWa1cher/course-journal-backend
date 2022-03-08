package com.a6raywa1cher.coursejournalbackend.integration;

import com.a6raywa1cher.coursejournalbackend.RequestContext;
import com.a6raywa1cher.coursejournalbackend.dto.StudentDto;
import com.a6raywa1cher.coursejournalbackend.service.CourseService;
import com.a6raywa1cher.coursejournalbackend.service.StudentService;
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
public class StudentControllerIntegrationTests extends AbstractIntegrationTests {
    @Autowired
    CourseService courseService;

    @Autowired
    StudentService studentService;

    @Test
    void getStudentById__self__valid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                String firstName = faker.name().firstName();
                String lastName = faker.name().lastName();
                long courseId = ef.createCourse(getIdAsLong());

                long id = studentService.create(StudentDto.builder()
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

                securePerform(get("/students/{id}", id))
                        .andExpect(status().isOk())
                        .andExpectAll(matchers);
            }
        };
    }

    @Test
    void getStudentById__otherAsAdmin__valid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                String firstName = faker.name().firstName();
                String lastName = faker.name().lastName();
                long courseId = ef.createCourse();

                long id = studentService.create(StudentDto.builder()
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

                securePerform(get("/students/{id}", id))
                        .andExpect(status().isOk())
                        .andExpectAll(matchers);
            }
        };
    }

    @Test
    void getStudentById__otherAsTeacher__invalid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                String firstName = faker.name().firstName();
                String lastName = faker.name().lastName();
                long courseId = ef.createCourse();

                long id = studentService.create(StudentDto.builder()
                        .firstName(firstName)
                        .lastName(lastName)
                        .course(courseId)
                        .build()).getId();

                securePerform(get("/students/{id}", id))
                        .andExpect(status().isForbidden());
            }
        };
    }

    @Test
    void getStudentById__notAuthenticated__invalid() throws Exception {
        String firstName = faker.name().firstName();
        String lastName = faker.name().lastName();
        long courseId = ef.createCourse();

        long id = studentService.create(StudentDto.builder()
                .firstName(firstName)
                .lastName(lastName)
                .course(courseId)
                .build()).getId();

        mvc.perform(get("/students/{id}", id))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getStudentById__notExists__invalid() {
        String firstName = faker.name().firstName();
        String lastName = faker.name().lastName();
        long courseId = ef.createCourse();

        long id = studentService.create(StudentDto.builder()
                .firstName(firstName)
                .lastName(lastName)
                .course(courseId)
                .build()).getId();

        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                securePerform(get("/students/{id}", id + 1000))
                        .andExpect(status().isNotFound());
            }
        };
    }

    // ================================================================================================================

    @Test
    void getStudentByCourse__self__valid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                long courseId1 = ef.createCourse(getIdAsLong());
                long courseId2 = ef.createCourse(getIdAsLong());

                String firstName1 = "A" + faker.name().firstName();
                String firstName2 = "B" + faker.name().firstName();

                studentService.create(StudentDto.builder()
                        .firstName(firstName2)
                        .lastName(faker.name().lastName())
                        .course(courseId1)
                        .build());

                studentService.create(StudentDto.builder()
                        .firstName(firstName1)
                        .lastName(faker.name().lastName())
                        .course(courseId1)
                        .build());

                studentService.create(StudentDto.builder()
                        .firstName(firstName1)
                        .lastName(faker.name().lastName())
                        .course(courseId2)
                        .build());

                securePerform(get("/students/course/{id}", courseId1)
                        .queryParam("sort", "firstName,asc"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.totalElements").value(2))
                        .andExpect(jsonPath("$.content[0].firstName").value(firstName1))
                        .andExpect(jsonPath("$.content[1].firstName").value(firstName2));
            }
        };
    }

    @Test
    void getStudentByCourse__otherAsAdmin__valid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                long courseId1 = ef.createCourse();
                long courseId2 = ef.createCourse();

                String firstName1 = "A" + faker.name().firstName();
                String firstName2 = "B" + faker.name().firstName();

                studentService.create(StudentDto.builder()
                        .firstName(firstName2)
                        .lastName(faker.name().lastName())
                        .course(courseId1)
                        .build());

                studentService.create(StudentDto.builder()
                        .firstName(firstName1)
                        .lastName(faker.name().lastName())
                        .course(courseId1)
                        .build());

                studentService.create(StudentDto.builder()
                        .firstName(firstName1)
                        .lastName(faker.name().lastName())
                        .course(courseId2)
                        .build());

                securePerform(get("/students/course/{id}", courseId1)
                        .queryParam("sort", "firstName,asc"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.totalElements").value(2))
                        .andExpect(jsonPath("$.content[0].firstName").value(firstName1))
                        .andExpect(jsonPath("$.content[1].firstName").value(firstName2));
            }
        };
    }

    @Test
    void getStudentByCourse__otherAsTeacher__invalid() {
        long id = ef.createCourse();

        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                securePerform(get("/students/course/{id}", id))
                        .andExpect(status().isForbidden());
            }
        };
    }

    @Test
    void getStudentByCourse__notAuthenticated__invalid() throws Exception {
        long id = ef.createCourse();
        mvc.perform(get("/students/course/{id}", id)).andExpect(status().isUnauthorized());
    }

    // ================================================================================================================

    @Test
    void getStudentByCourseNotPaged__self__valid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                long courseId1 = ef.createCourse(getIdAsLong());
                long courseId2 = ef.createCourse(getIdAsLong());

                String lastName1 = "A" + faker.name().lastName();
                String lastName2 = "B" + faker.name().lastName();

                studentService.create(StudentDto.builder()
                        .firstName(faker.name().firstName())
                        .lastName(lastName2)
                        .course(courseId1)
                        .build());

                studentService.create(StudentDto.builder()
                        .firstName(faker.name().firstName())
                        .lastName(lastName1)
                        .course(courseId1)
                        .build());

                studentService.create(StudentDto.builder()
                        .firstName(faker.name().firstName())
                        .lastName(lastName2)
                        .course(courseId2)
                        .build());

                securePerform(get("/students/course/{id}/all", courseId1))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$", hasSize(2)))
                        .andExpect(jsonPath("$[0].lastName").value(lastName1))
                        .andExpect(jsonPath("$[1].lastName").value(lastName2));
            }
        };
    }

    @Test
    void getStudentByCourseNotPaged__otherAsAdmin__valid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                long courseId1 = ef.createCourse();
                long courseId2 = ef.createCourse();

                String lastName1 = "A" + faker.name().lastName();
                String lastName2 = "B" + faker.name().lastName();

                studentService.create(StudentDto.builder()
                        .firstName(faker.name().firstName())
                        .lastName(lastName2)
                        .course(courseId1)
                        .build());

                studentService.create(StudentDto.builder()
                        .firstName(faker.name().firstName())
                        .lastName(lastName1)
                        .course(courseId1)
                        .build());

                studentService.create(StudentDto.builder()
                        .firstName(faker.name().firstName())
                        .lastName(lastName2)
                        .course(courseId2)
                        .build());

                securePerform(get("/students/course/{id}/all", courseId1))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$", hasSize(2)))
                        .andExpect(jsonPath("$[0].lastName").value(lastName1))
                        .andExpect(jsonPath("$[1].lastName").value(lastName2));
            }
        };
    }

    @Test
    void getStudentByCourseNotPaged__otherAsTeacher__invalid() {
        long id = ef.createCourse();

        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                securePerform(get("/students/course/{id}/all", id))
                        .andExpect(status().isForbidden());
            }
        };
    }

    @Test
    void getStudentByCourseNotPaged__notAuthenticated__invalid() throws Exception {
        long id = ef.createCourse();
        mvc.perform(get("/students/course/{id}/all", id)).andExpect(status().isUnauthorized());
    }

    // ================================================================================================================

    RequestContext<ObjectNode> getCreateStudentRequest(long courseId) {
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
    void createStudent__self__valid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                long courseId = ef.createCourse(getIdAsLong());
                var ctx = getCreateStudentRequest(courseId);

                ObjectNode request = ctx.getRequest();

                securePerform(post("/students/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isCreated())
                        .andExpectAll(ctx.getMatchers());

                securePerform(get("/students/course/{id}", courseId))
                        .andExpect(status().isOk())
                        .andExpectAll(content().string(containsString(request.get("firstName").asText())));
            }
        };
    }

    @Test
    void createStudent__otherAsAdmin__valid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                long courseId = ef.createCourse();
                var ctx = getCreateStudentRequest(courseId);

                ObjectNode request = ctx.getRequest();

                securePerform(post("/students/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isCreated())
                        .andExpectAll(ctx.getMatchers());

                securePerform(get("/students/course/{id}", courseId))
                        .andExpect(status().isOk())
                        .andExpectAll(content().string(containsString(request.get("firstName").asText())));
            }
        };
    }

    @Test
    void createStudent__otherAsTeacher__invalid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                long courseId = ef.createCourse();
                var ctx = getCreateStudentRequest(courseId);

                ObjectNode request = ctx.getRequest();

                securePerform(post("/students/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isForbidden());
            }
        };
    }

    @Test
    void createStudent__notAuthenticated__invalid() throws Exception {
        long courseId = ef.createCourse();
        var ctx = getCreateStudentRequest(courseId);

        ObjectNode request = ctx.getRequest();

        mvc.perform(post("/students/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                .andExpect(status().isUnauthorized());
    }

    // ================================================================================================================

    RequestContext<ObjectNode> getBatchCreateStudentRequest(long courseId) {
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
    void batchCreateStudent__self__valid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                long courseId = ef.createCourse(getIdAsLong());
                RequestContext<ObjectNode> ctx = getBatchCreateStudentRequest(courseId);

                ObjectNode request = ctx.getRequest();

                securePerform(post("/students/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isCreated())
                        .andExpect(jsonPath("$", hasSize(2)))
                        .andExpectAll(ctx.getMatchers());

                securePerform(get("/students/course/{id}", courseId)
                        .queryParam("sort", "lastName,asc"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.totalElements").value(2))
                        .andExpectAll(ctx.getMatchers("$.content"));
            }
        };
    }

    @Test
    void batchCreateStudent__otherAsAdmin__valid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                long courseId = ef.createCourse();
                RequestContext<ObjectNode> ctx = getBatchCreateStudentRequest(courseId);

                ObjectNode request = ctx.getRequest();

                securePerform(post("/students/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isCreated())
                        .andExpect(jsonPath("$", hasSize(2)))
                        .andExpectAll(ctx.getMatchers());

                securePerform(get("/students/course/{id}", courseId)
                        .queryParam("sort", "lastName,asc"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.totalElements").value(2))
                        .andExpectAll(ctx.getMatchers("$.content"));
            }
        };
    }

    @Test
    void batchCreateStudent__otherAsTeacher__invalid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                long courseId = ef.createCourse();
                RequestContext<ObjectNode> ctx = getBatchCreateStudentRequest(courseId);

                ObjectNode request = ctx.getRequest();

                securePerform(post("/students/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isForbidden());
            }
        };
    }

    @Test
    void batchCreateStudent__notAuthenticated__invalid() throws Exception {
        long courseId = ef.createCourse();
        RequestContext<ObjectNode> ctx = getBatchCreateStudentRequest(courseId);

        ObjectNode request = ctx.getRequest();

        mvc.perform(post("/students/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                .andExpect(status().isUnauthorized());
    }

    // ================================================================================================================

    RequestContext<ObjectNode> getPutStudentRequest(long courseId) {
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
    void putStudent__self__valid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                long courseId = ef.createCourse(getIdAsLong());
                long studentId = ef.createStudent(ef.bag().withCourseId(courseId));

                RequestContext<ObjectNode> ctx = getPutStudentRequest(courseId);
                ObjectNode request = ctx.getRequest();

                securePerform(put("/students/{id}", studentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isOk())
                        .andExpectAll(ctx.getMatchers());

                securePerform(get("/students/{id}", studentId))
                        .andExpect(status().isOk())
                        .andExpectAll(ctx.getMatchers());
            }
        };
    }

    @Test
    void putStudent__otherAsAdmin__valid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                long courseId = ef.createCourse();
                long studentId = ef.createStudent(ef.bag().withCourseId(courseId));

                RequestContext<ObjectNode> ctx = getPutStudentRequest(courseId);
                ObjectNode request = ctx.getRequest();

                securePerform(put("/students/{id}", studentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isOk())
                        .andExpectAll(ctx.getMatchers());

                securePerform(get("/students/{id}", studentId))
                        .andExpect(status().isOk())
                        .andExpectAll(ctx.getMatchers());
            }
        };
    }

    @Test
    void putStudent__otherAsTeacher__invalid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                long courseId = ef.createCourse();
                long studentId = ef.createStudent(ef.bag().withCourseId(courseId));

                RequestContext<ObjectNode> ctx = getPutStudentRequest(courseId);
                ObjectNode request = ctx.getRequest();

                securePerform(put("/students/{id}", studentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isForbidden());
            }
        };
    }

    @Test
    void putStudent__courseChange__invalid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                long courseId1 = ef.createCourse();
                long courseId2 = ef.createCourse();
                long studentId = ef.createStudent(ef.bag().withCourseId(courseId1));

                RequestContext<ObjectNode> ctx = getPutStudentRequest(courseId2);
                ObjectNode request = ctx.getRequest();

                securePerform(put("/students/{id}", studentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isBadRequest());
            }
        };
    }

    @Test
    void putStudent__notExists__invalid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                long courseId = ef.createCourse();
                long studentId = ef.createStudent(ef.bag().withCourseId(courseId));

                RequestContext<ObjectNode> ctx = getPutStudentRequest(courseId);
                ObjectNode request = ctx.getRequest();

                securePerform(put("/students/{id}", studentId + 1000)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isNotFound());
            }
        };
    }

    @Test
    void putStudent__notAuthenticated__invalid() throws Exception {
        long courseId = ef.createCourse();
        long studentId = ef.createStudent(ef.bag().withCourseId(courseId));

        RequestContext<ObjectNode> ctx = getPutStudentRequest(courseId);
        ObjectNode request = ctx.getRequest();

        mvc.perform(put("/students/{id}", studentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                .andExpect(status().isUnauthorized());
    }

    // ================================================================================================================

    RequestContext<ObjectNode> getPatchStudentRequest(long courseId, String middleName, String lastName) {
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
    void patchStudent__self__valid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                long courseId = ef.createCourse(getIdAsLong());

                String lastName = faker.name().lastName();
                String middleName = faker.name().firstName();

                long studentId = ef.createStudent(ef.bag()
                        .withCourseId(courseId)
                        .withDto(StudentDto.builder()
                                .lastName(lastName)
                                .middleName(middleName)
                                .build()
                        ));

                RequestContext<ObjectNode> ctx = getPatchStudentRequest(courseId, middleName, lastName);
                ObjectNode request = ctx.getRequest();

                securePerform(patch("/students/{id}", studentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isOk())
                        .andExpectAll(ctx.getMatchers());

                securePerform(get("/students/{id}", studentId))
                        .andExpect(status().isOk())
                        .andExpectAll(ctx.getMatchers());
            }
        };
    }

    @Test
    void patchStudent__otherAsAdmin__valid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                long courseId = ef.createCourse();

                String lastName = faker.name().lastName();
                String middleName = faker.name().firstName();

                long studentId = ef.createStudent(ef.bag()
                        .withCourseId(courseId)
                        .withDto(StudentDto.builder()
                                .lastName(lastName)
                                .middleName(middleName)
                                .build()
                        ));

                RequestContext<ObjectNode> ctx = getPatchStudentRequest(courseId, middleName, lastName);
                ObjectNode request = ctx.getRequest();

                securePerform(patch("/students/{id}", studentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isOk())
                        .andExpectAll(ctx.getMatchers());

                securePerform(get("/students/{id}", studentId))
                        .andExpect(status().isOk())
                        .andExpectAll(ctx.getMatchers());
            }
        };
    }

    @Test
    void patchStudent__otherAsTeacher__invalid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                long courseId = ef.createCourse();

                String lastName = faker.name().lastName();
                String middleName = faker.name().firstName();

                long studentId = ef.createStudent(ef.bag()
                        .withCourseId(courseId)
                        .withDto(StudentDto.builder()
                                .lastName(lastName)
                                .middleName(middleName)
                                .build()
                        ));

                RequestContext<ObjectNode> ctx = getPatchStudentRequest(courseId, middleName, lastName);
                ObjectNode request = ctx.getRequest();

                securePerform(patch("/students/{id}", studentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isForbidden());
            }
        };
    }

    @Test
    void patchStudent__courseChange__invalid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                long courseId1 = ef.createCourse();
                long courseId2 = ef.createCourse();

                String lastName = faker.name().lastName();
                String middleName = faker.name().firstName();

                long studentId = ef.createStudent(ef.bag()
                        .withCourseId(courseId1)
                        .withDto(StudentDto.builder()
                                .lastName(lastName)
                                .middleName(middleName)
                                .build()
                        ));

                RequestContext<ObjectNode> ctx = getPatchStudentRequest(courseId2, middleName, lastName);
                ObjectNode request = ctx.getRequest();

                securePerform(patch("/students/{id}", studentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isBadRequest());
            }
        };
    }

    @Test
    void patchStudent__notExists__invalid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                long courseId = ef.createCourse();

                String lastName = faker.name().lastName();
                String middleName = faker.name().firstName();

                long studentId = ef.createStudent(ef.bag()
                        .withCourseId(courseId)
                        .withDto(StudentDto.builder()
                                .lastName(lastName)
                                .middleName(middleName)
                                .build()
                        ));

                RequestContext<ObjectNode> ctx = getPatchStudentRequest(courseId, middleName, lastName);
                ObjectNode request = ctx.getRequest();

                securePerform(patch("/students/{id}", studentId + 1000)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isNotFound());
            }
        };
    }

    @Test
    void patchStudent__notAuthenticated__invalid() throws Exception {
        long courseId = ef.createCourse();

        String lastName = faker.name().lastName();
        String middleName = faker.name().firstName();

        long studentId = ef.createStudent(ef.bag()
                .withCourseId(courseId)
                .withDto(StudentDto.builder()
                        .lastName(lastName)
                        .middleName(middleName)
                        .build()
                ));

        RequestContext<ObjectNode> ctx = getPatchStudentRequest(courseId, middleName, lastName);
        ObjectNode request = ctx.getRequest();

        mvc.perform(patch("/students/{id}", studentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                .andExpect(status().isUnauthorized());
    }

    // ================================================================================================================

    @Test
    void deleteStudent__self__valid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                long studentId = ef.createStudent(getIdAsLong());

                securePerform(delete("/students/{id}", studentId))
                        .andExpect(status().isOk());

                securePerform(get("/students/{id}", studentId))
                        .andExpect(status().isNotFound());
            }
        };
    }

    @Test
    void deleteStudent__otherAsAdmin__valid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                long studentId = ef.createStudent();

                securePerform(delete("/students/{id}", studentId))
                        .andExpect(status().isOk());

                securePerform(get("/students/{id}", studentId))
                        .andExpect(status().isNotFound());
            }
        };
    }

    @Test
    void deleteStudent__otherAsTeacher__invalid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                long studentId = ef.createStudent();

                securePerform(delete("/students/{id}", studentId))
                        .andExpect(status().isForbidden());
            }
        };
    }

    @Test
    void deleteStudent__notFound__invalid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                long studentId = ef.createStudent(getIdAsLong());

                securePerform(delete("/students/{id}", studentId + 1000))
                        .andExpect(status().isNotFound());
            }
        };
    }

    @Test
    void deleteStudent__notAuthenticated__invalid() throws Exception {
        long studentId = ef.createStudent();

        mvc.perform(delete("/students/{id}", studentId))
                .andExpect(status().isUnauthorized());
    }
}
