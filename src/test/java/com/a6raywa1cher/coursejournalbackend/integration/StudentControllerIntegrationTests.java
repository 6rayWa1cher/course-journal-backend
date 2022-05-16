package com.a6raywa1cher.coursejournalbackend.integration;

import com.a6raywa1cher.coursejournalbackend.RequestContext;
import com.a6raywa1cher.coursejournalbackend.dto.CourseFullDto;
import com.a6raywa1cher.coursejournalbackend.dto.StudentDto;
import com.a6raywa1cher.coursejournalbackend.service.CourseService;
import com.a6raywa1cher.coursejournalbackend.service.StudentService;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultMatcher;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static com.a6raywa1cher.coursejournalbackend.TestUtils.getIdFromResult;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
public class StudentControllerIntegrationTests extends AbstractIntegrationTests {
    @Autowired
    CourseService courseService;

    @Autowired
    StudentService studentService;

    @Test
    void getStudentById__teacher__valid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                long owner = ef.createEmployee();
                String firstName = faker.name().firstName();
                String lastName = faker.name().lastName();
                long groupId = ef.createGroup();

                long id = studentService.create(StudentDto.builder()
                        .firstName(firstName)
                        .lastName(lastName)
                        .group(groupId)
                        .build()).getId();

                List<Long> students = new ArrayList<>();
                students.add(id);

                ef.createCourse(ef.bag().withEmployeeId(owner).withDto(
                        CourseFullDto.builder()
                                .students(students)
                                .build()
                ));

                ResultMatcher[] matchers = {
                        jsonPath("$.id").value(id),
                        jsonPath("$.group").value(groupId),
                        jsonPath("$.firstName").value(firstName),
                        jsonPath("$.lastName").value(lastName),
                        jsonPath("$.headman").value(false)
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
                long groupId = ef.createGroup();

                long id = studentService.create(StudentDto.builder()
                        .firstName(firstName)
                        .lastName(lastName)
                        .group(groupId)
                        .build()).getId();

                ResultMatcher[] matchers = {
                        jsonPath("$.id").value(id),
                        jsonPath("$.group").value(groupId),
                        jsonPath("$.firstName").value(firstName),
                        jsonPath("$.lastName").value(lastName),
                        jsonPath("$.headman").value(false)
                };

                securePerform(get("/students/{id}", id))
                        .andExpect(status().isOk())
                        .andExpectAll(matchers);
            }
        };
    }

    @Test
    void getStudentById__withCourseToken__invalid() {
        String firstName = faker.name().firstName();
        String lastName = faker.name().lastName();
        long groupId = ef.createGroup();

        long id = studentService.create(StudentDto.builder()
                .firstName(firstName)
                .lastName(lastName)
                .group(groupId)
                .build()).getId();

        List<Long> students = new ArrayList<>();
        students.add(id);

        long courseId = ef.createCourse(ef.bag().withDto(
                CourseFullDto.builder()
                        .students(students)
                        .build()
        ));

        new WithCourseToken(courseId, true) {
            @Override
            void run() throws Exception {
                securePerform(get("/students/{id}", id))
                        .andExpect(status().isForbidden());
            }
        };
    }

    @Test
    void getStudentById__notAuthenticated__invalid() throws Exception {
        String firstName = faker.name().firstName();
        String lastName = faker.name().lastName();

        long groupId = ef.createGroup();

        long id = studentService.create(StudentDto.builder()
                .firstName(firstName)
                .lastName(lastName)
                .group(groupId)
                .build()).getId();

        mvc.perform(get("/students/{id}", id))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getStudentById__notExists__invalid() {
        String firstName = faker.name().firstName();
        String lastName = faker.name().lastName();

        long groupId = ef.createGroup();

        long id = studentService.create(StudentDto.builder()
                .firstName(firstName)
                .lastName(lastName)
                .group(groupId)
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
                long groupId = ef.createGroup();

                String firstName1 = "A" + faker.name().firstName();
                String firstName2 = "B" + faker.name().firstName();

                long student1 = studentService.create(StudentDto.builder()
                        .firstName(firstName2)
                        .lastName(faker.name().lastName())
                        .group(groupId)
                        .build()).getId();

                long student2 = studentService.create(StudentDto.builder()
                        .firstName(firstName1)
                        .lastName(faker.name().lastName())
                        .group(groupId)
                        .build()).getId();

                long student3 = studentService.create(StudentDto.builder()
                        .firstName(firstName1)
                        .lastName(faker.name().lastName())
                        .group(groupId)
                        .build()).getId();

                List<Long> students1 = new ArrayList<>();
                students1.add(student1);
                students1.add(student2);
                List<Long> students2 = new ArrayList<>();
                students2.add(student3);

                long courseId1 = ef.createCourse(ef.bag().withEmployeeId(getSelfEmployeeIdAsLong())
                        .withDto(CourseFullDto.builder()
                                .students(students1)
                                .build()));

                ef.createCourse(ef.bag().withEmployeeId(getSelfEmployeeIdAsLong())
                        .withDto(CourseFullDto.builder()
                                .students(students2)
                                .build()));

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
                long owner = ef.createEmployee();
                long groupId = ef.createGroup();

                String firstName1 = "A" + faker.name().firstName();
                String firstName2 = "B" + faker.name().firstName();

                long student1 = studentService.create(StudentDto.builder()
                        .firstName(firstName2)
                        .lastName(faker.name().lastName())
                        .group(groupId)
                        .build()).getId();

                long student2 = studentService.create(StudentDto.builder()
                        .firstName(firstName1)
                        .lastName(faker.name().lastName())
                        .group(groupId)
                        .build()).getId();

                long student3 = studentService.create(StudentDto.builder()
                        .firstName(firstName1)
                        .lastName(faker.name().lastName())
                        .group(groupId)
                        .build()).getId();

                List<Long> students1 = new ArrayList<>();
                students1.add(student1);
                students1.add(student2);
                List<Long> students2 = new ArrayList<>();
                students2.add(student3);

                long courseId1 = ef.createCourse(ef.bag().withEmployeeId(owner)
                        .withDto(CourseFullDto.builder()
                                .students(students1)
                                .build()));

                ef.createCourse(ef.bag().withEmployeeId(owner)
                        .withDto(CourseFullDto.builder()
                                .students(students2)
                                .build()));

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
    void getStudentByCourse__withCourseToken__valid() {
        long ownerId = ef.createEmployee();

        String firstName1 = "A" + faker.name().firstName();
        String firstName2 = "B" + faker.name().firstName();
        long groupId = ef.createGroup();

        long student1 = studentService.create(StudentDto.builder()
                .firstName(firstName2)
                .lastName(faker.name().lastName())
                .group(groupId)
                .build()).getId();

        long student2 = studentService.create(StudentDto.builder()
                .firstName(firstName1)
                .lastName(faker.name().lastName())
                .group(groupId)
                .build()).getId();

        long student3 = studentService.create(StudentDto.builder()
                .firstName(firstName1)
                .lastName(faker.name().lastName())
                .group(groupId)
                .build()).getId();

        List<Long> students1 = new ArrayList<>();
        students1.add(student1);
        students1.add(student2);
        List<Long> students2 = new ArrayList<>();
        students2.add(student3);

        long courseId1 = ef.createCourse(ef.bag().withEmployeeId(ownerId)
                .withDto(CourseFullDto.builder()
                        .students(students1)
                        .build()));

        ef.createCourse(ef.bag().withEmployeeId(ownerId)
                .withDto(CourseFullDto.builder()
                        .students(students2)
                        .build()));

        new WithCourseToken(courseId1, true) {
            @Override
            void run() throws Exception {

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

                String firstName1 = "A" + faker.name().firstName();
                String firstName2 = "B" + faker.name().firstName();
                long groupId = ef.createGroup();

                long student1 = studentService.create(StudentDto.builder()
                        .firstName(firstName1)
                        .lastName(faker.name().lastName())
                        .group(groupId)
                        .build()).getId();

                long student2 = studentService.create(StudentDto.builder()
                        .firstName(firstName2)
                        .lastName(faker.name().lastName())
                        .group(groupId)
                        .build()).getId();

                long student3 = studentService.create(StudentDto.builder()
                        .firstName(firstName1)
                        .lastName(faker.name().lastName())
                        .group(groupId)
                        .build()).getId();
                
                List<Long> students1 = new ArrayList<>();
                students1.add(student1);
                students1.add(student2);
                List<Long> students2 = new ArrayList<>();
                students2.add(student3);
                
                long courseId1 = ef.createCourse(ef.bag().withEmployeeId(getSelfEmployeeIdAsLong())
                        .withDto(CourseFullDto.builder()
                                .students(students1)
                                .build()));

                ef.createCourse(ef.bag().withEmployeeId(getSelfEmployeeIdAsLong())
                        .withDto(CourseFullDto.builder()
                                .students(students2)
                                .build()));

                securePerform(get("/students/course/{id}/all", courseId1))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$", hasSize(2)))
                        .andExpect(jsonPath("$[0].firstName").value(firstName1))
                        .andExpect(jsonPath("$[1].firstName").value(firstName2));
            }
        };
    }

    @Test
    void getStudentByCourseNotPaged__otherAsAdmin__valid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                String firstName1 = "A" + faker.name().firstName();
                String firstName2 = "B" + faker.name().firstName();
                long groupId = ef.createGroup();

                long student1 = studentService.create(StudentDto.builder()
                        .firstName(firstName1)
                        .lastName(faker.name().lastName())
                        .group(groupId)
                        .build()).getId();

                long student2 = studentService.create(StudentDto.builder()
                        .firstName(firstName2)
                        .lastName(faker.name().lastName())
                        .group(groupId)
                        .build()).getId();

                long student3 = studentService.create(StudentDto.builder()
                        .firstName(firstName1)
                        .lastName(faker.name().lastName())
                        .group(groupId)
                        .build()).getId();

                List<Long> students1 = new ArrayList<>();
                students1.add(student1);
                students1.add(student2);
                List<Long> students2 = new ArrayList<>();
                students2.add(student3);

                long courseId1 = ef.createCourse(ef.bag()
                        .withDto(CourseFullDto.builder()
                                .students(students1)
                                .build()));

                ef.createCourse(ef.bag()
                        .withDto(CourseFullDto.builder()
                                .students(students2)
                                .build()));

                securePerform(get("/students/course/{id}/all", courseId1))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$", hasSize(2)))
                        .andExpect(jsonPath("$[0].firstName").value(firstName1))
                        .andExpect(jsonPath("$[1].firstName").value(firstName2));
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
    void getStudentByCourseNotPaged__withCourseToken__valid() {
        long ownerId = ef.createEmployee();

        String firstName1 = "A" + faker.name().firstName();
        String firstName2 = "B" + faker.name().firstName();
        long groupId = ef.createGroup();

        long student1 = studentService.create(StudentDto.builder()
                .firstName(firstName1)
                .lastName(faker.name().lastName())
                .group(groupId)
                .build()).getId();

        long student2 = studentService.create(StudentDto.builder()
                .firstName(firstName2)
                .lastName(faker.name().lastName())
                .group(groupId)
                .build()).getId();

        long student3 = studentService.create(StudentDto.builder()
                .firstName(firstName1)
                .lastName(faker.name().lastName())
                .group(groupId)
                .build()).getId();

        List<Long> students1 = new ArrayList<>();
        students1.add(student1);
        students1.add(student2);
        List<Long> students2 = new ArrayList<>();
        students2.add(student3);

        long courseId1 = ef.createCourse(ef.bag().withEmployeeId(ownerId)
                .withDto(CourseFullDto.builder()
                        .students(students1)
                        .build()));

        ef.createCourse(ef.bag().withEmployeeId(ownerId)
                .withDto(CourseFullDto.builder()
                        .students(students2)
                        .build()));

        new WithCourseToken(courseId1, true) {
            @Override
            void run() throws Exception {

                securePerform(get("/students/course/{id}/all", courseId1))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$", hasSize(2)))
                        .andExpect(jsonPath("$[0].firstName").value(firstName1))
                        .andExpect(jsonPath("$[1].firstName").value(firstName2));
            }
        };
    }

    @Test
    void getStudentByCourseNotPaged__notAuthenticated__invalid() throws Exception {
        long id = ef.createCourse();
        mvc.perform(get("/students/course/{id}/all", id)).andExpect(status().isUnauthorized());
    }

    // ================================================================================================================

    @Test
    void getStudentByGroup__teacher__valid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                long groupId1 = ef.createGroup();
                long groupId2 = ef.createGroup();

                String lastName1 = "A" + faker.name().lastName();
                String lastName2 = "B" + faker.name().lastName();

                studentService.create(StudentDto.builder()
                        .firstName(faker.name().firstName())
                        .lastName(lastName1)
                        .group(groupId1)
                        .build());

                studentService.create(StudentDto.builder()
                        .firstName(faker.name().firstName())
                        .lastName(lastName2)
                        .group(groupId1)
                        .build());

                studentService.create(StudentDto.builder()
                        .firstName(faker.name().firstName())
                        .lastName(lastName2)
                        .group(groupId2)
                        .build());

                securePerform(get("/students/group/{id}", groupId1))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$", hasSize(2)))
                        .andExpect(jsonPath("$[0].lastName").value(lastName1))
                        .andExpect(jsonPath("$[1].lastName").value(lastName2));
            }
        };
    }

    @Test
    void getStudentByGroup__admin__valid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {

                long groupId1 = ef.createGroup();
                long groupId2 = ef.createGroup();

                String lastName1 = "A" + faker.name().lastName();
                String lastName2 = "B" + faker.name().lastName();

                studentService.create(StudentDto.builder()
                        .firstName(faker.name().firstName())
                        .lastName(lastName1)
                        .group(groupId1)
                        .build());

                studentService.create(StudentDto.builder()
                        .firstName(faker.name().firstName())
                        .lastName(lastName2)
                        .group(groupId1)
                        .build());

                studentService.create(StudentDto.builder()
                        .firstName(faker.name().firstName())
                        .lastName(lastName2)
                        .group(groupId2)
                        .build());

                securePerform(get("/students/group/{id}", groupId1))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$", hasSize(2)))
                        .andExpect(jsonPath("$[0].lastName").value(lastName1))
                        .andExpect(jsonPath("$[1].lastName").value(lastName2));
            }
        };
    }

    @Test
    void getStudentByGroup__withCourseToken__invalid() {
        long ownerId = ef.createEmployee();
        long courseId = ef.createCourse(ownerId);
        long groupId1 = ef.createGroup();
        long groupId2 = ef.createGroup();
        new WithCourseToken(courseId, true) {
            @Override
            void run() throws Exception {
                String lastName1 = "A" + faker.name().lastName();
                String lastName2 = "B" + faker.name().lastName();

                studentService.create(StudentDto.builder()
                        .firstName(faker.name().firstName())
                        .lastName(lastName2)
                        .group(groupId1)
                        .build());

                studentService.create(StudentDto.builder()
                        .firstName(faker.name().firstName())
                        .lastName(lastName1)
                        .group(groupId1)
                        .build());

                studentService.create(StudentDto.builder()
                        .firstName(faker.name().firstName())
                        .lastName(lastName2)
                        .group(groupId2)
                        .build());

                securePerform(get("/students/group/{id}", groupId1))
                        .andExpect(status().isForbidden());
            }
        };
    }

    @Test
    void getStudentByGroup__notAuthenticated__invalid() throws Exception {
        long id = ef.createCourse();
        mvc.perform(get("/students/group/{id}", id)).andExpect(status().isUnauthorized());
    }

    // ================================================================================================================

    RequestContext<ObjectNode> getCreateStudentRequest(long groupId) {
        String firstName = faker.name().firstName();
        String lastName = faker.name().lastName();
        String middleName = faker.name().firstName();

        ObjectNode request = objectMapper.createObjectNode()
                .put("firstName", firstName)
                .put("middleName", middleName)
                .put("lastName", lastName)
                .put("group", groupId);

        Function<String, ResultMatcher[]> matchers = prefix -> new ResultMatcher[]{
                jsonPath("$.id").isNumber(),
                jsonPath("$.firstName").value(firstName),
                jsonPath("$.middleName").value(middleName),
                jsonPath("$.lastName").value(lastName),
                jsonPath("$.group").value(groupId)
        };

        return new RequestContext<>(request, matchers);
    }

    @Test
    void createStudent__admin__valid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {

                long groupId = ef.createGroup();
                var ctx = getCreateStudentRequest(groupId);

                ObjectNode request = ctx.getRequest();

                MvcResult mvcResult = securePerform(post("/students/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isCreated())
                        .andExpectAll(ctx.getMatchers())
                        .andReturn();

                long id = getIdFromResult(mvcResult);

                securePerform(get("/students/{id}", id))
                        .andExpect(status().isOk())
                        .andExpectAll(ctx.getMatchers());
            }
        };
    }

    @Test
    void createStudent__notAdmin__invalid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                long groupId = ef.createGroup();
                var ctx = getCreateStudentRequest(groupId);

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

        long groupId = ef.createGroup();
        var ctx = getCreateStudentRequest(groupId);

        ObjectNode request = ctx.getRequest();

        mvc.perform(post("/students/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                .andExpect(status().isUnauthorized());
    }

    // ================================================================================================================

    RequestContext<ObjectNode> getBatchCreateStudentRequest(long groupId) {
        String firstName1 = faker.name().firstName();
        String lastName1 = "A" + faker.name().lastName();
        String middleName1 = faker.name().firstName();

        String firstName2 = faker.name().firstName();
        String lastName2 = "B" + faker.name().lastName();
        String middleName2 = faker.name().firstName();

        ObjectNode request = objectMapper.createObjectNode()
                .put("group", groupId);

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
                        jsonPath(prefix + "[0].headman").value(false),
                        jsonPath(prefix + "[1].firstName").value(firstName2),
                        jsonPath(prefix + "[1].middleName").value(middleName2),
                        jsonPath(prefix + "[1].lastName").value(lastName2),
                        jsonPath(prefix + "[1].headman").value(false),
                });
    }

    @Test
    void batchCreateStudent__admin__valid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {

                long groupId = ef.createGroup();
                RequestContext<ObjectNode> ctx = getBatchCreateStudentRequest(groupId);

                ObjectNode request = ctx.getRequest();

                securePerform(post("/students/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isCreated())
                        .andExpect(jsonPath("$", hasSize(2)))
                        .andExpectAll(ctx.getMatchers())
                        .andReturn();

                securePerform(get("/students/group/{id}", groupId))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$", hasSize(2)))
                        .andExpectAll(ctx.getMatchers());
            }
        };
    }

    @Test
    void batchCreateStudent__notAdmin__invalid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {

                long groupId = ef.createGroup();
                RequestContext<ObjectNode> ctx = getBatchCreateStudentRequest(groupId);

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

        long groupId = ef.createGroup();
        RequestContext<ObjectNode> ctx = getBatchCreateStudentRequest(groupId);

        ObjectNode request = ctx.getRequest();

        mvc.perform(post("/students/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                .andExpect(status().isUnauthorized());
    }

    // ================================================================================================================

    RequestContext<ObjectNode> getPutStudentRequest(long groupId) {
        String firstName = faker.name().firstName();
        String lastName = faker.name().lastName();
        String middleName = faker.name().firstName();

        ObjectNode request = objectMapper.createObjectNode()
                .put("firstName", firstName)
                .put("middleName", middleName)
                .put("lastName", lastName)
                .put("group", groupId);

        Function<String, ResultMatcher[]> matchers = prefix -> new ResultMatcher[]{
                jsonPath("$.id").isNumber(),
                jsonPath("$.firstName").value(firstName),
                jsonPath("$.middleName").value(middleName),
                jsonPath("$.lastName").value(lastName),
                jsonPath("$.group").value(groupId),
                jsonPath("$.headman").value(false)
        };

        return new RequestContext<>(request, matchers);
    }

    @Test
    void putStudent__admin__valid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                long groupId = ef.createGroup();
                long studentId = ef.createStudent(ef.bag().withGroupId(groupId));

                RequestContext<ObjectNode> ctx = getPutStudentRequest(groupId);
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

                long groupId = ef.createGroup();
                long studentId = ef.createStudent(ef.bag().withGroupId(groupId));

                RequestContext<ObjectNode> ctx = getPutStudentRequest(groupId);
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

                long groupId = ef.createGroup();
                long studentId = ef.createStudent(ef.bag().withGroupId(groupId));

                RequestContext<ObjectNode> ctx = getPutStudentRequest(groupId);
                ObjectNode request = ctx.getRequest();

                securePerform(put("/students/{id}", studentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isForbidden());
            }
        };
    }

    @Test
    void putStudent__withCourseToken__invalid() {
        new WithCourseToken() {
            @Override
            void run() throws Exception {
                long groupId = ef.createGroup();
                long studentId = ef.createStudent(ef.bag().withGroupId(groupId));

                RequestContext<ObjectNode> ctx = getPutStudentRequest(groupId);
                ObjectNode request = ctx.getRequest();

                securePerform(put("/students/{id}", studentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isForbidden());
            }
        };
    }

    @Test
    void putStudent__notExists__invalid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {

                long groupId = ef.createGroup();
                long studentId = ef.createStudent(ef.bag().withGroupId(groupId));

                RequestContext<ObjectNode> ctx = getPutStudentRequest(groupId);
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

        long groupId = ef.createGroup();
        long studentId = ef.createStudent(ef.bag().withGroupId(groupId));

        RequestContext<ObjectNode> ctx = getPutStudentRequest(groupId);
        ObjectNode request = ctx.getRequest();

        mvc.perform(put("/students/{id}", studentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                .andExpect(status().isUnauthorized());
    }

    // ================================================================================================================

    RequestContext<ObjectNode> getPatchStudentRequest(long groupId, String middleName, String lastName) {
        String firstName = faker.name().firstName();

        ObjectNode request = objectMapper.createObjectNode()
                .put("firstName", firstName)
                .put("group", groupId);

        Function<String, ResultMatcher[]> matchers = prefix -> new ResultMatcher[]{
                jsonPath("$.id").isNumber(),
                jsonPath("$.firstName").value(firstName),
                jsonPath("$.middleName").value(middleName),
                jsonPath("$.lastName").value(lastName),
                jsonPath("$.group").value(groupId),
                jsonPath("$.headman").value(false)
        };

        return new RequestContext<>(request, matchers);
    }

    @Test
    void patchStudent__admin__valid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                long groupId = ef.createGroup();

                String lastName = faker.name().lastName();
                String middleName = faker.name().firstName();

                long studentId = ef.createStudent(ef.bag()
                        
                        .withGroupId(groupId)
                        .withDto(StudentDto.builder()
                                .lastName(lastName)
                                .middleName(middleName)
                                .build()
                        ));

                RequestContext<ObjectNode> ctx = getPatchStudentRequest(groupId, middleName, lastName);
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
    void patchStudent__notAdmin__invalid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {

                long groupId = ef.createGroup();

                String lastName = faker.name().lastName();
                String middleName = faker.name().firstName();

                long studentId = ef.createStudent(ef.bag()
                        
                        .withGroupId(groupId)
                        .withDto(StudentDto.builder()
                                .lastName(lastName)
                                .middleName(middleName)
                                .build()
                        ));

                RequestContext<ObjectNode> ctx = getPatchStudentRequest(groupId, middleName, lastName);
                ObjectNode request = ctx.getRequest();

                securePerform(patch("/students/{id}", studentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isForbidden());
            }
        };
    }

    @Test
    void patchStudent__withCourseToken__invalid() {
        new WithCourseToken() {
            @Override
            void run() throws Exception {
                long groupId = ef.createGroup();

                String lastName = faker.name().lastName();
                String middleName = faker.name().firstName();

                long studentId = ef.createStudent(ef.bag()
                        
                        .withGroupId(groupId)
                        .withDto(StudentDto.builder()
                                .lastName(lastName)
                                .middleName(middleName)
                                .build()
                        ));

                RequestContext<ObjectNode> ctx = getPatchStudentRequest(groupId, middleName, lastName);
                ObjectNode request = ctx.getRequest();

                securePerform(patch("/students/{id}", studentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isForbidden());
            }
        };
    }

    @Test
    void patchStudent__notExists__invalid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {

                long groupId = ef.createGroup();

                String lastName = faker.name().lastName();
                String middleName = faker.name().firstName();

                long studentId = ef.createStudent(ef.bag()
                        
                        .withGroupId(groupId)
                        .withDto(StudentDto.builder()
                                .lastName(lastName)
                                .middleName(middleName)
                                .build()
                        ));

                RequestContext<ObjectNode> ctx = getPatchStudentRequest(groupId, middleName, lastName);
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

        long groupId = ef.createGroup();

        String lastName = faker.name().lastName();
        String middleName = faker.name().firstName();

        long studentId = ef.createStudent(ef.bag()
                
                .withGroupId(groupId)
                .withDto(StudentDto.builder()
                        .lastName(lastName)
                        .middleName(middleName)
                        .build()
                ));

        RequestContext<ObjectNode> ctx = getPatchStudentRequest(groupId, middleName, lastName);
        ObjectNode request = ctx.getRequest();

        mvc.perform(patch("/students/{id}", studentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                .andExpect(status().isUnauthorized());
    }

    // ================================================================================================================

    @Test
    void deleteStudent__admin__valid() {
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
    void deleteStudent__notAdmin__invalid() {
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
    void deleteStudent__withCourseToken__invalid() {
        new WithCourseToken() {
            @Override
            void run() throws Exception {
                long studentId = ef.createStudent(ef.bag().withCourseId(getCourseId()));

                securePerform(delete("/students/{id}", studentId))
                        .andExpect(status().isForbidden());
            }
        };
    }

    @Test
    void deleteStudent__notFound__invalid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                long studentId = ef.createStudent();

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
