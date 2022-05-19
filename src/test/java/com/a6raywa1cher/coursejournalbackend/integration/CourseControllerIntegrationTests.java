package com.a6raywa1cher.coursejournalbackend.integration;

import com.a6raywa1cher.coursejournalbackend.RequestContext;
import com.a6raywa1cher.coursejournalbackend.TestUtils;
import com.a6raywa1cher.coursejournalbackend.dto.CourseFullDto;
import com.a6raywa1cher.coursejournalbackend.model.repo.CourseRepository;
import com.a6raywa1cher.coursejournalbackend.model.repo.EmployeeRepository;
import com.a6raywa1cher.coursejournalbackend.service.CourseService;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultMatcher;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import static com.a6raywa1cher.coursejournalbackend.TestUtils.getIdFromResult;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
public class CourseControllerIntegrationTests extends AbstractIntegrationTests {
    @Autowired
    CourseRepository courseRepository;

    @Autowired
    CourseService courseService;

    @Autowired
    EmployeeRepository employeeRepository;

    @Test
    void getCourses__admin__valid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                String sentence1 = faker.lorem().sentence();
                String sentence2 = faker.lorem().sentence();

                courseService.create(CourseFullDto.builder()
                        .name(sentence1)
                        .owner(ef.createEmployee())
                        .build());

                courseService.create(CourseFullDto.builder()
                        .name(sentence2)
                        .owner(ef.createEmployee())
                        .build());

                securePerform(get("/courses/").queryParam("sort", "id,desc"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.totalElements").value(2))
                        .andExpect(jsonPath("$.content[0].name").value(sentence2))
                        .andExpect(jsonPath("$.content[1].name").value(sentence1));
            }
        };
    }

    @Test
    void getCourses__notAdmin__invalid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                securePerform(get("/courses/").queryParam("sort", "id,desc"))
                        .andExpect(status().isForbidden());
            }
        };
    }

    @Test
    void getCourses__notAuthenticated__invalid() throws Exception {
        mvc.perform(get("/courses/")).andExpect(status().isUnauthorized());
    }

    // ================================================================================================================

    @Test
    void getCoursesByOwner__self__valid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                String sentence1 = "ABC>>" + faker.lorem().sentence();
                String sentence2 = "DEF>>" + faker.lorem().sentence();
                String sentence3 = faker.lorem().sentence();

                courseService.create(CourseFullDto.builder()
                        .name(sentence1)
                        .owner(getSelfEmployeeIdAsLong())
                        .build());

                courseService.create(CourseFullDto.builder()
                        .name(sentence2)
                        .owner(getSelfEmployeeIdAsLong())
                        .build());

                courseService.create(CourseFullDto.builder()
                        .name(sentence3)
                        .owner(ef.createEmployee())
                        .build());

                securePerform(get("/courses/owner/{id}", getSelfEmployeeId()).queryParam("sort", "id,desc"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.totalElements").value(2))
                        .andExpect(jsonPath("$.content[0].name").value(sentence2))
                        .andExpect(jsonPath("$.content[1].name").value(sentence1));

                securePerform(get("/courses/owner/{id}", getSelfEmployeeId())
                        .queryParam("sort", "id,desc")
                        .queryParam("name", "c>"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.totalElements").value(1))
                        .andExpect(jsonPath("$.content[0].name").value(sentence1));


                securePerform(get("/courses/owner/{id}", getSelfEmployeeId())
                        .queryParam("sort", "id,desc")
                        .queryParam("name", "f>"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.totalElements").value(1))
                        .andExpect(jsonPath("$.content[0].name").value(sentence2));


                securePerform(get("/courses/owner/{id}", getSelfEmployeeId())
                        .queryParam("sort", "id,desc")
                        .queryParam("name", ">>"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.totalElements").value(2))
                        .andExpect(jsonPath("$.content[0].name").value(sentence2))
                        .andExpect(jsonPath("$.content[1].name").value(sentence1));
            }
        };
    }

    @Test
    void getCoursesByOwner__otherAsAdmin__valid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                String sentence1 = faker.lorem().sentence();
                String sentence2 = faker.lorem().sentence();
                long owner1 = ef.createEmployee();
                long owner2 = ef.createEmployee();

                courseService.create(CourseFullDto.builder()
                        .name(sentence1)
                        .owner(owner1)
                        .build());

                courseService.create(CourseFullDto.builder()
                        .name(sentence2)
                        .owner(owner2)
                        .build());

                securePerform(get("/courses/owner/{id}", owner1))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.totalElements").value(1))
                        .andExpect(jsonPath("$.content[0].name").value(sentence1));
            }
        };
    }

    @Test
    void getCoursesByOwner__otherAsTeacher__invalid() {
        long id = ef.createEmployee();

        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                securePerform(get("/courses/owner/{id}", id).queryParam("sort", "id,desc"))
                        .andExpect(status().isForbidden());
            }
        };
    }

    @Test
    void getCoursesByOwner__withCourseToken__invalid() {
        long id = ef.createEmployee();

        new WithCourseToken(ef.createCourse(id), true) {
            @Override
            void run() throws Exception {
                securePerform(get("/courses/owner/{id}", id).queryParam("sort", "id,desc"))
                        .andExpect(status().isForbidden());
            }
        };
    }

    @Test
    void getCoursesByOwner__notAuthenticated__invalid() throws Exception {
        long id = ef.createEmployee();
        mvc.perform(get("/courses/owner/{id}", id)).andExpect(status().isUnauthorized());
    }

    // ================================================================================================================

    @Test
    void getCourseById__self__valid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                String name = faker.lorem().sentence();
                long id = courseService.create(CourseFullDto.builder()
                        .name(name)
                        .owner(getSelfEmployeeIdAsLong())
                        .build()).getId();

                securePerform(get("/courses/{id}", id))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.name").value(name));
            }
        };
    }

    @Test
    void getCourseById__otherAsAdmin__valid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                String name = faker.lorem().sentence();
                long id = courseService.create(CourseFullDto.builder()
                        .name(name)
                        .owner(getSelfEmployeeIdAsLong())
                        .build()).getId();

                securePerform(get("/courses/{id}", id))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.name").value(name));
            }
        };
    }

    @Test
    void getCourseById__otherAsTeacher__invalid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                String name = faker.lorem().sentence();
                long id = courseService.create(CourseFullDto.builder()
                        .name(name)
                        .owner(ef.createEmployee())
                        .build()).getId();

                securePerform(get("/courses/{id}", id))
                        .andExpect(status().isForbidden());
            }
        };
    }

    @Test
    void getCourseById__withCourseToken__valid() {
        String name = faker.lorem().sentence();
        long id = courseService.create(CourseFullDto.builder()
                .name(name)
                .owner(ef.createEmployee())
                .build()).getId();

        new WithCourseToken(id, true) {
            @Override
            void run() throws Exception {
                securePerform(get("/courses/{id}", id))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.name").value(name));
            }
        };
    }

    @Test
    void getCourseById__notAuthenticated__invalid() throws Exception {
        String name = faker.lorem().sentence();
        long id = courseService.create(CourseFullDto.builder()
                .name(name)
                .owner(ef.createEmployee())
                .build()).getId();

        mvc.perform(get("/courses/{id}", id))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getCourseById__notExists__invalid() {
        String name = faker.lorem().sentence();
        long id = courseService.create(CourseFullDto.builder()
                .name(name)
                .owner(ef.createEmployee())
                .build()).getId();

        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                securePerform(get("/courses/{id}", id + 1000))
                        .andExpect(status().isNotFound());
            }
        };
    }

    // ================================================================================================================

    @Test
    void getCourseByName__self__invalid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                String name = faker.lorem().sentence();
                courseService.create(CourseFullDto.builder()
                        .name(name)
                        .owner(getSelfEmployeeIdAsLong())
                        .build());

                securePerform(get("/courses/name").queryParam("query", name.substring(1)))
                        .andExpect(status().isForbidden());
            }
        };
    }

    @Test
    void getCourseByName__otherAsAdmin__valid() {
        String name = faker.lorem().sentence();
        courseService.create(CourseFullDto.builder()
                .name(name)
                .owner(ef.createEmployee())
                .build());

        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                securePerform(get("/courses/name").queryParam("query", name.substring(1)))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.totalElements").value(1))
                        .andExpect(jsonPath("$.content[0].name").value(name));
            }
        };
    }

    @Test
    void getCourseByName__otherAsTeacher__invalid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                String name = faker.lorem().sentence();
                courseService.create(CourseFullDto.builder()
                        .name(name)
                        .owner(ef.createEmployee())
                        .build());

                securePerform(get("/courses/name").queryParam("query", name.substring(1)))
                        .andExpect(status().isForbidden());
            }
        };
    }

    @Test
    void getCourseByName__withCourseToken__invalid() {
        String name = faker.lorem().sentence();
        long id = courseService.create(CourseFullDto.builder()
                .name(name)
                .owner(ef.createEmployee())
                .build()).getId();

        new WithCourseToken(id, true) {
            @Override
            void run() throws Exception {
                securePerform(get("/courses/name").queryParam("query", name.substring(1)))
                        .andExpect(status().isForbidden());
            }
        };
    }

    @Test
    void getCourseByName__notAuthenticated__invalid() throws Exception {
        String name = faker.lorem().sentence();
        courseService.create(CourseFullDto.builder()
                .name(name)
                .owner(ef.createEmployee())
                .build());

        mvc.perform(get("/courses/name").queryParam("query", name))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getCourseByName__notExists__invalid() {
        String name = faker.lorem().sentence();
        courseService.create(CourseFullDto.builder()
                .name(name)
                .owner(ef.createEmployee())
                .build());

        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                securePerform(get("/courses/name").queryParam("query", name + "fpkiejwr"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.totalElements").value(0));
            }
        };
    }


    // ================================================================================================================

    RequestContext<ObjectNode> getCreateCourseContext(long ownerId, String name) {
        ObjectNode request = objectMapper.createObjectNode()
                .put("name", name)
                .put("owner", ownerId);

        long studentId1 = ef.createStudent();
        long studentId2 = ef.createStudent();
        List<Long> studentIds = List.of(studentId1, studentId2);
        ArrayNode studentsNode = request.putArray("students");
        studentIds.forEach(studentsNode::add);

        Function<String, ResultMatcher[]> matchers = prefix -> getCourseMatchers(prefix, ownerId, name, studentIds);

        return RequestContext.<ObjectNode>builder()
                .request(request)
                .matchersSupplier(matchers)
                .build();
    }

    @Test
    void createCourse__self__valid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                String name = faker.lorem().sentence();
                long ownerId = getSelfEmployeeIdAsLong();

                var ctx = getCreateCourseContext(ownerId, name);
                ObjectNode request = ctx.getRequest();

                MvcResult mvcResult = securePerform(post("/courses/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isCreated())
                        .andExpectAll(ctx.getMatchers())
                        .andReturn();

                long id = getIdFromResult(mvcResult);

                securePerform(get("/courses/{id}", id))
                        .andExpect(status().isOk())
                        .andExpectAll(ctx.getMatchers());
            }
        };
    }

    @Test
    void createCourse__otherAsAdmin__valid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                String name = faker.lorem().sentence();
                long ownerId = ef.createEmployee();

                var ctx = getCreateCourseContext(ownerId, name);
                ObjectNode request = ctx.getRequest();

                MvcResult mvcResult = securePerform(post("/courses/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isCreated())
                        .andExpectAll(ctx.getMatchers())
                        .andReturn();

                long id = getIdFromResult(mvcResult);

                securePerform(get("/courses/{id}", id))
                        .andExpect(status().isOk())
                        .andExpectAll(ctx.getMatchers());
            }
        };
    }

    @Test
    void createCourse__otherAsTeacher__invalid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                String name = faker.lorem().sentence();
                long ownerId = ef.createEmployee();

                var ctx = getCreateCourseContext(ownerId, name);
                ObjectNode request = ctx.getRequest();

                securePerform(post("/courses/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isForbidden());
            }
        };
    }

    @Test
    void createCourse__withCourseToken__invalid() {
        long ownerId = ef.createEmployee();

        new WithCourseToken(ownerId) {
            @Override
            void run() throws Exception {
                String name = faker.lorem().sentence();

                var ctx = getCreateCourseContext(ownerId, name);
                ObjectNode request = ctx.getRequest();

                securePerform(post("/courses/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isForbidden());
            }
        };
    }

    @Test
    void createCourse__conflictingNames__invalid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                String name = faker.lorem().sentence();
                long ownerId = ef.createEmployee();

                ef.createCourse(ef.bag()
                        .withEmployeeId(ownerId)
                        .withDto(CourseFullDto.builder().name(name).build())
                );

                var ctx = getCreateCourseContext(ownerId, name);
                ObjectNode request = ctx.getRequest();

                securePerform(post("/courses/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isConflict());
            }
        };
    }

    @Test
    void createCourse__notAuthenticated__invalid() throws Exception {
        String name = faker.lorem().sentence();
        long ownerId = ef.createEmployee();

        var ctx = getCreateCourseContext(ownerId, name);
        ObjectNode request = ctx.getRequest();

        mvc.perform(post("/courses/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                .andExpect(status().isUnauthorized());
    }

    // ================================================================================================================

    RequestContext<ObjectNode> getPutCourseContext(long id, long ownerId, String name, List<Long> studentIds) {
        ObjectNode request = objectMapper.createObjectNode()
                .put("name", name)
                .put("owner", ownerId);

        ArrayNode studentsNode = request.putArray("students");
        studentIds.forEach(studentsNode::add);

        Function<String, ResultMatcher[]> matchers = prefix -> getCourseMatchers(prefix, id, ownerId, name, studentIds);

        return RequestContext.<ObjectNode>builder()
                .request(request)
                .matchersSupplier(matchers)
                .build();
    }

    @Test
    void putCourse__self__valid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                // GIVEN
                String name = faker.lorem().sentence();
                long ownerId = getSelfEmployeeIdAsLong();

                long studentId1 = ef.createStudent();
                long studentId2 = ef.createStudent();
                List<Long> studentIds = List.of(studentId1, studentId2);

                long courseId = courseService.create(CourseFullDto.builder()
                        .name(name)
                        .owner(ownerId)
                        .students(studentIds)
                        .build()).getId();

                String newName = faker.lorem().sentence();
                long studentId3 = ef.createStudent();
                var ctx = getPutCourseContext(courseId, ownerId, newName, List.of(studentId1, studentId3));
                ObjectNode request = ctx.getRequest();

                // WHEN
                securePerform(put("/courses/{id}", courseId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isOk())
                        .andExpectAll(ctx.getMatchers());

                // THEN
                securePerform(get("/courses/{id}", courseId))
                        .andExpect(status().isOk())
                        .andExpectAll(ctx.getMatchers());
            }
        };
    }

    @Test
    void putCourse__otherAsAdmin__valid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                // GIVEN
                String name = faker.lorem().sentence();
                long ownerId = ef.createEmployee();

                long studentId1 = ef.createStudent();
                long studentId2 = ef.createStudent();
                List<Long> studentIds = List.of(studentId1, studentId2);

                long courseId = courseService.create(CourseFullDto.builder()
                        .name(name)
                        .owner(ownerId)
                        .students(studentIds)
                        .build()).getId();

                String newName = faker.lorem().sentence();
                long studentId3 = ef.createStudent();
                var ctx = getPutCourseContext(courseId, ownerId, newName, List.of(studentId1, studentId3));
                ObjectNode request = ctx.getRequest();

                // WHEN
                securePerform(put("/courses/{id}", courseId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isOk())
                        .andExpectAll(ctx.getMatchers());

                // THEN
                securePerform(get("/courses/{id}", courseId))
                        .andExpect(status().isOk())
                        .andExpectAll(ctx.getMatchers());
            }
        };
    }

    @Test
    void putCourse__otherAsTeacher__invalid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                // GIVEN
                String name = faker.lorem().sentence();
                long ownerId = ef.createEmployee();

                long studentId1 = ef.createStudent();
                long studentId2 = ef.createStudent();
                List<Long> studentIds = List.of(studentId1, studentId2);

                long courseId = courseService.create(CourseFullDto.builder()
                        .name(name)
                        .owner(ownerId)
                        .students(studentIds)
                        .build()).getId();

                String newName = faker.lorem().sentence();
                long studentId3 = ef.createStudent();
                var ctx = getPutCourseContext(courseId, ownerId, newName, List.of(studentId1, studentId3));
                ObjectNode request = ctx.getRequest();

                // WHEN
                securePerform(put("/courses/{id}", courseId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        // THEN
                        .andExpect(status().isForbidden());
            }
        };
    }

    @Test
    void putCourse__withCourseToken__invalid() {
        long ownerId = ef.createEmployee();
        new WithCourseToken(ownerId) {
            @Override
            void run() throws Exception {
                // GIVEN
                String name = faker.lorem().sentence();

                long studentId1 = ef.createStudent();
                long studentId2 = ef.createStudent();
                List<Long> studentIds = List.of(studentId1, studentId2);

                long courseId = courseService.create(CourseFullDto.builder()
                        .name(name)
                        .owner(ownerId)
                        .students(studentIds)
                        .build()).getId();

                String newName = faker.lorem().sentence();
                long studentId3 = ef.createStudent();
                var ctx = getPutCourseContext(courseId, ownerId, newName, List.of(studentId1, studentId3));
                ObjectNode request = ctx.getRequest();

                // WHEN
                securePerform(put("/courses/{id}", courseId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        // THEN
                        .andExpect(status().isForbidden());
            }
        };
    }

    @Test
    void putCourse__ownershipTransferAsAdmin__valid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                // GIVEN
                String name = faker.lorem().sentence();
                long ownerId = ef.createEmployee();

                long studentId1 = ef.createStudent();
                long studentId2 = ef.createStudent();
                List<Long> studentIds = List.of(studentId1, studentId2);

                long courseId = courseService.create(CourseFullDto.builder()
                        .name(name)
                        .owner(ownerId)
                        .students(studentIds)
                        .build()).getId();

                String newName = faker.lorem().sentence();
                long newOwnerId = ef.createEmployee();
                long studentId3 = ef.createStudent();
                var ctx = getPutCourseContext(courseId, newOwnerId, newName, List.of(studentId1, studentId3));
                ObjectNode request = ctx.getRequest();

                // WHEN
                securePerform(put("/courses/{id}", courseId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isOk())
                        .andExpectAll(ctx.getMatchers());

                // THEN
                securePerform(get("/courses/{id}", courseId))
                        .andExpect(status().isOk())
                        .andExpectAll(ctx.getMatchers());
            }
        };
    }

    @Test
    void putCourse__ownershipTransferAsTeacher__fromSelf__invalid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                // GIVEN
                String name = faker.lorem().sentence();
                long ownerId = getSelfEmployeeIdAsLong();

                long studentId1 = ef.createStudent();
                long studentId2 = ef.createStudent();
                List<Long> studentIds = List.of(studentId1, studentId2);

                long courseId = courseService.create(CourseFullDto.builder()
                        .name(name)
                        .owner(ownerId)
                        .students(studentIds)
                        .build()).getId();

                String newName = faker.lorem().sentence();
                long newOwnerId = ef.createEmployee();
                long studentId3 = ef.createStudent();
                var ctx = getPutCourseContext(courseId, newOwnerId, newName, List.of(studentId1, studentId3));
                ObjectNode request = ctx.getRequest();

                // WHEN
                securePerform(put("/courses/{id}", courseId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        // THEN
                        .andExpect(status().isForbidden());
            }
        };
    }

    @Test
    void putCourse__ownershipTransferAsTeacher__toSelf__invalid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                // GIVEN
                String name = faker.lorem().sentence();
                long ownerId = ef.createEmployee();

                long studentId1 = ef.createStudent();
                long studentId2 = ef.createStudent();
                List<Long> studentIds = List.of(studentId1, studentId2);

                long courseId = courseService.create(CourseFullDto.builder()
                        .name(name)
                        .owner(ownerId)
                        .students(studentIds)
                        .build()).getId();

                String newName = faker.lorem().sentence();
                long newOwnerId = getSelfEmployeeIdAsLong();
                long studentId3 = ef.createStudent();
                var ctx = getPutCourseContext(courseId, newOwnerId, newName, List.of(studentId1, studentId3));
                ObjectNode request = ctx.getRequest();

                // WHEN
                securePerform(put("/courses/{id}", courseId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        // THEN
                        .andExpect(status().isForbidden());
            }
        };
    }

    @Test
    void putCourse__ownershipTransferAsTeacher__other__invalid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                // GIVEN
                String name = faker.lorem().sentence();
                long ownerId = ef.createEmployee();

                long studentId1 = ef.createStudent();
                long studentId2 = ef.createStudent();
                List<Long> studentIds = List.of(studentId1, studentId2);

                long courseId = courseService.create(CourseFullDto.builder()
                        .name(name)
                        .owner(ownerId)
                        .students(studentIds)
                        .build()).getId();

                String newName = faker.lorem().sentence();
                long newOwnerId = ef.createEmployee();
                long studentId3 = ef.createStudent();
                var ctx = getPutCourseContext(courseId, newOwnerId, newName, List.of(studentId1, studentId3));
                ObjectNode request = ctx.getRequest();

                // WHEN
                securePerform(put("/courses/{id}", courseId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        // THEN
                        .andExpect(status().isForbidden());
            }
        };
    }

    @Test
    void putCourse__conflictingNames__sameOwner__invalid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                // GIVEN
                String name1 = faker.lorem().sentence();
                String name2 = faker.lorem().sentence();
                long ownerId = getSelfEmployeeIdAsLong();

                long studentId1 = ef.createStudent();
                long studentId2 = ef.createStudent();
                List<Long> studentIds = List.of(studentId1, studentId2);

                long courseId = courseService.create(CourseFullDto.builder()
                        .name(name1)
                        .owner(ownerId)
                        .students(studentIds)
                        .build()).getId();

                courseService.create(CourseFullDto.builder()
                        .name(name2)
                        .owner(ownerId)
                        .students(studentIds)
                        .build());

                var ctx = getPutCourseContext(courseId, ownerId, name2, studentIds);
                ObjectNode request = ctx.getRequest();

                // WHEN
                securePerform(put("/courses/{id}", courseId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        // THEN
                        .andExpect(status().isConflict());
            }
        };
    }

    @Test
    void putCourse__conflictingNames__transfer__invalid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                // GIVEN
                String name = faker.lorem().sentence();
                long ownerId1 = ef.createEmployee();
                long ownerId2 = ef.createEmployee();

                long studentId1 = ef.createStudent();
                long studentId2 = ef.createStudent();
                List<Long> studentIds = List.of(studentId1, studentId2);

                long courseId = courseService.create(CourseFullDto.builder()
                        .name(name)
                        .owner(ownerId1)
                        .students(studentIds)
                        .build()).getId();

                courseService.create(CourseFullDto.builder()
                        .name(name)
                        .owner(ownerId2)
                        .students(studentIds)
                        .build());

                var ctx = getPutCourseContext(courseId, ownerId2, name, studentIds);
                ObjectNode request = ctx.getRequest();

                // WHEN
                securePerform(put("/courses/{id}", courseId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        // THEN
                        .andExpect(status().isConflict());
            }
        };
    }

    @Test
    void putCourse__notAuthenticated__invalid() throws Exception {
        // GIVEN
        String name = faker.lorem().sentence();
        long ownerId = ef.createEmployee();

        long studentId1 = ef.createStudent();
        long studentId2 = ef.createStudent();
        List<Long> studentIds = List.of(studentId1, studentId2);

        long courseId = courseService.create(CourseFullDto.builder()
                .name(name)
                .owner(ownerId)
                .students(studentIds)
                .build()).getId();

        var ctx = getPutCourseContext(courseId, ownerId, name, studentIds);
        ObjectNode request = ctx.getRequest();

        // WHEN
        mvc.perform(put("/courses/{id}", courseId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                // THEN
                .andExpect(status().isUnauthorized());
    }

    // ================================================================================================================

    ObjectNode getPatchCourseRequest(String name, List<Long> students) {
        ObjectNode objectNode = objectMapper.createObjectNode()
                .put("name", name);
        ArrayNode studentsNode = objectNode.putArray("students");
        students.forEach(studentsNode::add);

        return objectNode;
    }

    @Test
    void patchCourse__self__valid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                // GIVEN
                String name = faker.lorem().sentence();
                long ownerId = getSelfEmployeeIdAsLong();

                long studentId1 = ef.createStudent();
                long studentId2 = ef.createStudent();
                List<Long> studentIds = List.of(studentId1, studentId2);

                long courseId = courseService.create(CourseFullDto.builder()
                        .name(name)
                        .owner(ownerId)
                        .students(studentIds)
                        .build()).getId();

                String newName = faker.lorem().sentence();
                long studentId3 = ef.createStudent();
                List<Long> newStudentIds = List.of(studentId1, studentId3);
                ObjectNode request = getPatchCourseRequest(newName, newStudentIds);
                ResultMatcher[] resultMatchers = getCourseMatchers("$", courseId, ownerId, newName, newStudentIds);

                // WHEN
                securePerform(patch("/courses/{id}", courseId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isOk())
                        .andExpectAll(resultMatchers);

                // THEN
                securePerform(get("/courses/{id}", courseId))
                        .andExpect(status().isOk())
                        .andExpectAll(resultMatchers);
            }
        };
    }

    @Test
    void patchCourse__otherAsAdmin__valid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                String name = faker.lorem().sentence();
                long ownerId = ef.createEmployee();

                long studentId1 = ef.createStudent();
                long studentId2 = ef.createStudent();
                List<Long> studentIds = List.of(studentId1, studentId2);

                long courseId = courseService.create(CourseFullDto.builder()
                        .name(name)
                        .owner(ownerId)
                        .students(studentIds)
                        .build()).getId();

                String newName = faker.lorem().sentence();
                long studentId3 = ef.createStudent();
                List<Long> newStudentIds = List.of(studentId1, studentId3);
                ObjectNode request = getPatchCourseRequest(newName, newStudentIds);
                ResultMatcher[] resultMatchers = getCourseMatchers("$", courseId, ownerId, newName, newStudentIds);

                securePerform(patch("/courses/{id}", courseId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isOk())
                        .andExpectAll(resultMatchers);


                securePerform(get("/courses/{id}", courseId))
                        .andExpect(status().isOk())
                        .andExpectAll(resultMatchers);
            }
        };
    }

    @Test
    void patchCourse__otherAsTeacher__invalid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                // GIVEN
                String name = faker.lorem().sentence();
                long ownerId = ef.createEmployee();

                long studentId1 = ef.createStudent();
                long studentId2 = ef.createStudent();
                List<Long> studentIds = List.of(studentId1, studentId2);

                long courseId = courseService.create(CourseFullDto.builder()
                        .name(name)
                        .owner(ownerId)
                        .students(studentIds)
                        .build()).getId();

                String newName = faker.lorem().sentence();
                long studentId3 = ef.createStudent();
                List<Long> newStudentIds = List.of(studentId1, studentId3);
                ObjectNode request = getPatchCourseRequest(newName, newStudentIds);

                // WHEN
                securePerform(patch("/courses/{id}", courseId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        // THEN
                        .andExpect(status().isForbidden());
            }
        };
    }

    @Test
    void patchCourse__withCourseToken__invalid() {
        long ownerId = ef.createEmployee();
        new WithCourseToken(ownerId) {
            @Override
            void run() throws Exception {
                String name = faker.lorem().sentence();

                long studentId1 = ef.createStudent();
                long studentId2 = ef.createStudent();
                List<Long> studentIds = List.of(studentId1, studentId2);

                long courseId = courseService.create(CourseFullDto.builder()
                        .name(name)
                        .owner(ownerId)
                        .students(studentIds)
                        .build()).getId();

                String newName = faker.lorem().sentence();
                long studentId3 = ef.createStudent();
                List<Long> newStudentIds = List.of(studentId1, studentId3);
                ObjectNode request = getPatchCourseRequest(newName, newStudentIds);

                securePerform(patch("/courses/{id}", courseId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isForbidden());
            }
        };
    }

    @Test
    void patchCourse__ownershipTransferAsAdmin__valid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                // GIVEN
                String name = faker.lorem().sentence();
                long ownerId = ef.createEmployee();

                long courseId = courseService.create(CourseFullDto.builder()
                        .name(name)
                        .owner(ownerId)
                        .build()).getId();

                long newOwnerId = ef.createEmployee();

                ResultMatcher[] resultMatchers = getCourseMatchers(
                        "$", courseId, newOwnerId, name, Collections.emptyList()
                );

                // WHEN
                securePerform(patch("/courses/{id}", courseId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.createObjectNode()
                                .put("owner", newOwnerId)
                                .toString()))
                        .andExpect(status().isOk())
                        .andExpectAll(resultMatchers);

                // THEN
                securePerform(get("/courses/{id}", courseId))
                        .andExpect(status().isOk())
                        .andExpectAll(resultMatchers);
            }
        };
    }

    @Test
    void patchCourse__ownershipTransferAsTeacher__fromSelf__invalid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                // GIVEN
                String name = faker.lorem().sentence();
                long ownerId = getSelfEmployeeIdAsLong();

                long courseId = courseService.create(CourseFullDto.builder()
                        .name(name)
                        .owner(ownerId)
                        .build()).getId();

                long newOwnerId = ef.createEmployee();

                // WHEN
                securePerform(patch("/courses/{id}", courseId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.createObjectNode()
                                .put("owner", newOwnerId)
                                .toString()))
                        // THEN
                        .andExpect(status().isForbidden());
            }
        };
    }

    @Test
    void patchCourse__ownershipTransferAsTeacher__toSelf__invalid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                // GIVEN
                String name = faker.lorem().sentence();
                long ownerId = ef.createEmployee();

                long courseId = courseService.create(CourseFullDto.builder()
                        .name(name)
                        .owner(ownerId)
                        .build()).getId();

                long newOwnerId = getSelfEmployeeIdAsLong();

                // WHEN
                securePerform(patch("/courses/{id}", courseId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.createObjectNode()
                                .put("owner", newOwnerId)
                                .toString()))
                        // THEN
                        .andExpect(status().isForbidden());
            }
        };
    }

    @Test
    void patchCourse__ownershipTransferAsTeacher__other__invalid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                // GIVEN
                String name = faker.lorem().sentence();
                long ownerId = ef.createEmployee();

                long courseId = courseService.create(CourseFullDto.builder()
                        .name(name)
                        .owner(ownerId)
                        .build()).getId();

                long newOwnerId = ef.createEmployee();

                // WHEN
                securePerform(patch("/courses/{id}", courseId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.createObjectNode()
                                .put("owner", newOwnerId)
                                .toString()))
                        // THEN
                        .andExpect(status().isForbidden());
            }
        };
    }

    @Test
    void patchCourse__conflictingNames__sameOwner__invalid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                // GIVEN
                String name1 = faker.lorem().sentence();
                String name2 = faker.lorem().sentence();
                long ownerId = getSelfEmployeeIdAsLong();

                long courseId = courseService.create(CourseFullDto.builder()
                        .name(name1)
                        .owner(ownerId)
                        .build()).getId();

                courseService.create(CourseFullDto.builder()
                        .name(name2)
                        .owner(ownerId)
                        .build());

                // WHEN
                securePerform(patch("/courses/{id}", courseId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.createObjectNode()
                                .put("name", name2)
                                .toString()))
                        // THEN
                        .andExpect(status().isConflict());
            }
        };
    }

    @Test
    void patchCourse__conflictingNames__transfer__invalid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                // GIVEN
                String name = faker.lorem().sentence();
                long ownerId1 = ef.createEmployee();
                long ownerId2 = ef.createEmployee();

                long courseId = courseService.create(CourseFullDto.builder()
                        .name(name)
                        .owner(ownerId1)
                        .build()).getId();

                courseService.create(CourseFullDto.builder()
                        .name(name)
                        .owner(ownerId2)
                        .build());

                // WHEN
                securePerform(patch("/courses/{id}", courseId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.createObjectNode()
                                .put("owner", ownerId2)
                                .toString()))
                        // THEN
                        .andExpect(status().isConflict());
            }
        };
    }

    @Test
    void patchCourse__notAuthenticated__invalid() throws Exception {
        // GIVEN
        String name = faker.lorem().sentence();
        long ownerId = ef.createEmployee();

        long courseId = courseService.create(CourseFullDto.builder()
                .name(name)
                .owner(ownerId)
                .build()).getId();

        // WHEN
        mvc.perform(
                        patch("/courses/{id}", courseId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.createObjectNode()
                                        .put("name", faker.lorem().sentence())
                                        .toString()
                                )
                )
                // THEN
                .andExpect(status().isUnauthorized());
    }

    // ================================================================================================================

    @Test
    void deleteCourse__self__valid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                String name = faker.lorem().sentence();
                long ownerId = getSelfEmployeeIdAsLong();

                long courseId = courseService.create(CourseFullDto.builder()
                        .name(name)
                        .owner(ownerId)
                        .build()).getId();

                securePerform(delete("/courses/{id}", courseId))
                        .andExpect(status().isOk());

                securePerform(get("/courses/{id}", courseId))
                        .andExpect(status().isNotFound());
            }
        };
    }

    @Test
    void deleteCourse__otherAsAdmin__valid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                String name = faker.lorem().sentence();
                long ownerId = ef.createEmployee();

                long courseId = courseService.create(CourseFullDto.builder()
                        .name(name)
                        .owner(ownerId)
                        .build()).getId();

                securePerform(delete("/courses/{id}", courseId))
                        .andExpect(status().isOk());

                securePerform(get("/courses/{id}", courseId))
                        .andExpect(status().isNotFound());
            }
        };
    }

    @Test
    void deleteCourse__otherAsTeacher__invalid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                String name = faker.lorem().sentence();
                long ownerId = ef.createEmployee();

                long courseId = courseService.create(CourseFullDto.builder()
                        .name(name)
                        .owner(ownerId)
                        .build()).getId();

                securePerform(delete("/courses/{id}", courseId))
                        .andExpect(status().isForbidden());
            }
        };
    }

    @Test
    void deleteCourse__withCourseToken__invalid() {
        long ownerId = ef.createEmployee();
        new WithCourseToken(ownerId) {
            @Override
            void run() throws Exception {
                String name = faker.lorem().sentence();

                long courseId = courseService.create(CourseFullDto.builder()
                        .name(name)
                        .owner(ownerId)
                        .build()).getId();

                securePerform(delete("/courses/{id}", courseId))
                        .andExpect(status().isForbidden());
            }
        };
    }

    @Test
    void deleteCourse__notAuthenticated__invalid() throws Exception {
        String name = faker.lorem().sentence();
        long ownerId = ef.createEmployee();

        long courseId = courseService.create(CourseFullDto.builder()
                .name(name)
                .owner(ownerId)
                .build()).getId();

        mvc.perform(delete("/courses/{id}", courseId))
                .andExpect(status().isUnauthorized());
    }

    // ================================================================================================================

    ResultMatcher[] getCourseMatchers(String prefix, long ownerId, String name, List<Long> studentIds) {
        return getCourseMatchers(prefix, null, ownerId, name, studentIds);
    }

    ResultMatcher[] getCourseMatchers(String prefix, Long id, long ownerId, String name, List<Long> studentIds) {
        return new ResultMatcher[]{
                id != null ?
                        jsonPath(prefix + ".id").value(id) :
                        jsonPath(prefix + ".id").isNumber(),
                jsonPath(prefix + ".name").value(name),
                jsonPath(prefix + ".owner").value(ownerId),
                jsonPath(prefix + ".students", new TestUtils.ContainsAllIdsMatcher(studentIds))
        };
    }
}
