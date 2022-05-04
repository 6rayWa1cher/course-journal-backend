package com.a6raywa1cher.coursejournalbackend.integration;

import com.a6raywa1cher.coursejournalbackend.RequestContext;
import com.a6raywa1cher.coursejournalbackend.dto.GroupDto;
import com.a6raywa1cher.coursejournalbackend.service.GroupService;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultMatcher;

import java.util.Map;
import java.util.function.Function;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@ActiveProfiles("test")
public class GroupControllerIntegrationTests extends AbstractIntegrationTests {
    @Autowired
    GroupService groupService;

    RequestContext<Long> createGetGroupByIdContextWithCourse(long courseId, long facultyId) {
        String name = faker.lorem().sentence(1);

        long id = groupService.create(GroupDto.builder()
                .name(name)
                .faculty(facultyId)
                .course(courseId)
                .build()).getId();

        Function<String, ResultMatcher[]> matchers = prefix -> new ResultMatcher[]{
                jsonPath(prefix + ".id").value(id),
                jsonPath(prefix + ".name").value(name),
                jsonPath(prefix + ".course").value(courseId),
                jsonPath(prefix + ".faculty").value(facultyId)
        };

        return RequestContext.<Long>builder()
                .request(id)
                .matchersSupplier(matchers)
                .data(Map.of("courseId", Long.toString(courseId)))
                .build();
    }

    RequestContext<Long> createGetGroupByIdContext(long userId) {
        long courseId = ef.createCourse();
        long facultyId = ef.createFaculty();
        return createGetGroupByIdContextWithCourse(courseId, facultyId);
    }

    @Test
    void getGroupById__authenticated__valid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                var context = createGetGroupByIdContext(getSelfEmployeeIdAsLong());

                long id = context.getRequest();
                ResultMatcher[] matchers = context.getMatchers();

                securePerform(get("/groups/{id}", id))
                        .andExpect(status().isOk())
                        .andExpectAll(matchers);
            }
        };
    }

    @Test
    void getGroupById__withCourseToken__valid() {
        new WithCourseToken() {
            @Override
            void run() throws Exception {
                long facultyId = ef.createFaculty();
                var context = createGetGroupByIdContextWithCourse(getCourseId(), facultyId);

                long id = context.getRequest();
                ResultMatcher[] matchers = context.getMatchers();

                securePerform(get("/groups/{id}", id))
                        .andExpect(status().isOk())
                        .andExpectAll(matchers);
            }
        };
    }

    @Test
    void getGroupById__notAuthenticated__invalid() throws Exception {
        var context = createGetGroupByIdContext(ef.createEmployee());

        long id = context.getRequest();

        mvc.perform(get("/groups/{id}", id))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getGroupById__notExists__invalid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                var context = createGetGroupByIdContext(ef.createEmployee());

                long id = context.getRequest();

                securePerform(get("/groups/{id}", id + 1000))
                        .andExpect(status().isNotFound());
            }
        };
    }

    // ================================================================================================================

    RequestContext<Long> createGetGroupByFacultyContext(long courseId, long facultyId, String name) {
        long id = groupService.create(GroupDto.builder()
                .course(courseId)
                .faculty(facultyId)
                .name(name)
                .build()).getId();

        Function<String, ResultMatcher[]> matchers = prefix -> new ResultMatcher[]{
                jsonPath(prefix + ".id").value(id),
                jsonPath(prefix + ".name").value(name),
                jsonPath(prefix + ".course").value(courseId),
                jsonPath(prefix + ".faculty").value(facultyId)
        };

        return RequestContext.<Long>builder()
                .request(id)
                .matchersSupplier(matchers)
                .build();
    }

    @Test
    void getGroupByFaculty__authenticated__valid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                long courseId = ef.createCourse();
                long facultyId1 = ef.createFaculty();
                long facultyId2 = ef.createFaculty();

                String name1 = "A" + faker.lorem().sentence(1);
                String name2 = "B" + faker.lorem().sentence(1);

                var context1 = createGetGroupByFacultyContext(courseId, facultyId1, name1);
                var context2 = createGetGroupByFacultyContext(courseId, facultyId1, name2);
                createGetGroupByFacultyContext(courseId, facultyId2, name1);

                securePerform(get("/groups/faculty/{id}", facultyId1)
                        .queryParam("sort", "name,asc"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$", hasSize(2)))
                        .andExpectAll(context1.getMatchers("$[0]"))
                        .andExpectAll(context2.getMatchers("$[1]"));
            }
        };
    }

    @Test
    void getGroupByFaculty__withCourseToken__valid() {
        new WithCourseToken() {
            @Override
            void run() throws Exception {
                long courseId = getCourseId();
                long facultyId1 = ef.createFaculty();
                long facultyId2 = ef.createFaculty();

                String name1 = "A" + faker.lorem().sentence(1);
                String name2 = "B" + faker.lorem().sentence(1);

                var context1 = createGetGroupByFacultyContext(courseId, facultyId1, name1);
                var context2 = createGetGroupByFacultyContext(courseId, facultyId1, name2);
                createGetGroupByFacultyContext(courseId, facultyId2, name1);

                securePerform(get("/groups/faculty/{id}", facultyId1)
                        .queryParam("sort", "name,asc"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$", hasSize(2)))
                        .andExpectAll(context1.getMatchers("$[0]"))
                        .andExpectAll(context2.getMatchers("$[1]"));
            }
        };
    }

    @Test
    void getGroupByFaculty__facultyNotExists__invalid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                long courseId = ef.createCourse();
                String name = faker.lorem().sentence(1);
                long facultyId = ef.createFaculty();

                var context = createGetGroupByFacultyContext(courseId, facultyId, name);

                securePerform(get("/groups/faculty/{id}", facultyId + 1000)
                        .queryParam("sort", "name,asc"))
                        .andExpect(status().isNotFound());
            }
        };
    }

    @Test
    void getGroupByFaculty__notAuthenticated__invalid() throws Exception {
        long facultyId = ef.createFaculty();
        mvc.perform(get("/groups/faculty/{id}", facultyId))
                .andExpect(status().isUnauthorized());
    }


    // ================================================================================================================

    RequestContext<Long> createGetGroupByCourseContext(long courseId, long facultyId, String name) {

        long id = groupService.create(GroupDto.builder()
                .course(courseId)
                .faculty(facultyId)
                .name(name)
                .build()).getId();

        Function<String, ResultMatcher[]> matchers = prefix -> new ResultMatcher[]{
                jsonPath(prefix + ".id").value(id),
                jsonPath(prefix + ".name").value(name),
                jsonPath(prefix + ".course").value(courseId),
                jsonPath(prefix + ".faculty").value(facultyId)
        };

        return RequestContext.<Long>builder()
                .request(id)
                .matchersSupplier(matchers)
                .build();
    }

    @Test
    void getGroupByCourse__self__valid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                long courseId1 = ef.createCourse(getSelfEmployeeIdAsLong());
                long courseId2 = ef.createCourse(getSelfEmployeeIdAsLong());

                long facultyId1 = ef.createFaculty();
                long facultyId2 = ef.createFaculty();

                String name1 = "A" + faker.lorem().sentence(1);
                String name2 = "B" + faker.lorem().sentence(1);

                var context1 = createGetGroupByCourseContext(courseId1, facultyId1, name1);
                var context2 = createGetGroupByCourseContext(courseId1, facultyId1, name2);
                createGetGroupByCourseContext(courseId2, facultyId2, name1);

                securePerform(get("/groups/course/{id}", courseId1)
                        .queryParam("sort", "name,asc"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$", hasSize(2)))
                        .andExpectAll(context1.getMatchers("$[0]"))
                        .andExpectAll(context2.getMatchers("$[1]"));
            }
        };
    }

    @Test
    void getGroupByCourse__otherAsAdmin__valid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                long courseId1 = ef.createCourse(ef.createEmployee());
                long courseId2 = ef.createCourse(ef.createEmployee());

                long facultyId1 = ef.createFaculty();
                long facultyId2 = ef.createFaculty();

                String name1 = "A" + faker.lorem().sentence(1);
                String name2 = "B" + faker.lorem().sentence(1);

                var context1 = createGetGroupByCourseContext(courseId1, facultyId1, name1);
                var context2 = createGetGroupByCourseContext(courseId1, facultyId1, name2);
                createGetGroupByCourseContext(courseId2, facultyId2, name1);

                securePerform(get("/groups/course/{id}", courseId1)
                        .queryParam("sort", "name,asc"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$", hasSize(2)))
                        .andExpectAll(context1.getMatchers("$[0]"))
                        .andExpectAll(context2.getMatchers("$[1]"));
            }
        };
    }

    @Test
    void getGroupByCourse__withCourseToken__valid() {
        new WithCourseToken() {
            @Override
            void run() throws Exception {
                long courseId1 = getCourseId();
                long courseId2 = ef.createCourse(ef.createEmployee());

                long facultyId1 = ef.createFaculty();
                long facultyId2 = ef.createFaculty();

                String name1 = "A" + faker.lorem().sentence(1);
                String name2 = "B" + faker.lorem().sentence(1);

                var context1 = createGetGroupByCourseContext(courseId1, facultyId1, name1);
                var context2 = createGetGroupByCourseContext(courseId1, facultyId1, name2);
                createGetGroupByCourseContext(courseId2, facultyId2, name1);

                securePerform(get("/groups/course/{id}", courseId1)
                        .queryParam("sort", "name,asc"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$", hasSize(2)))
                        .andExpectAll(context1.getMatchers("$[0]"))
                        .andExpectAll(context2.getMatchers("$[1]"));
            }
        };
    }

    @Test
    void getGroupByCourse__otherAsTeacher__invalid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                long courseId = ef.createCourse(ef.createEmployee());

                securePerform(get("/groups/course/{id}", courseId)
                        .queryParam("sort", "name,asc"))
                        .andExpect(status().isForbidden());
            }
        };
    }

    @Test
    void getGroupByCourse__courseNotExists__invalid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                long courseId = ef.createCourse();
                long facultyId = ef.createFaculty();
                String name = faker.lorem().sentence(1);

                createGetGroupByCourseContext(courseId, facultyId, name);

                securePerform(get("/groups/course/{id}", courseId + 1000)
                        .queryParam("sort", "name,asc"))
                        .andExpect(status().isNotFound());
            }
        };
    }

    @Test
    void getGroupByCourse__notAuthenticated__invalid() throws Exception {
         long id = ef.createCourse();
         mvc.perform(get("/groups/course/{id}", id))
                 .andExpect(status().isUnauthorized());
    }

    // ================================================================================================================

    RequestContext<Long> createGetGroupByCourseAndFacultyContext(long courseId, long facultyId, String name) {
        long id = groupService.create(GroupDto.builder()
                .course(courseId)
                .faculty(facultyId)
                .name(name)
                .build()).getId();

        Function<String, ResultMatcher[]> matchers = prefix -> new ResultMatcher[]{
                jsonPath(prefix + ".id").value(id),
                jsonPath(prefix + ".name").value(name),
                jsonPath(prefix + ".course").value(courseId),
                jsonPath(prefix + ".faculty").value(facultyId)
        };

        return RequestContext.<Long>builder()
                .request(id)
                .matchersSupplier(matchers)
                .build();
    }

    @Test
    void getGroupByCourseAndFaculty__self__valid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                long courseId1 = ef.createCourse(getSelfEmployeeIdAsLong());
                long courseId2 = ef.createCourse(getSelfEmployeeIdAsLong());

                String name1 = "A" + faker.lorem().sentence(1);
                String name2 = "B" + faker.lorem().sentence(1);
                String name3 = "C" + faker.lorem().sentence(1);

                long facultyId1 = ef.createFaculty();
                long facultyId2 = ef.createFaculty();

                var context1 = createGetGroupByCourseAndFacultyContext(courseId1, facultyId1, name1);
                var context2 = createGetGroupByCourseAndFacultyContext(courseId1, facultyId1, name2);
                createGetGroupByCourseAndFacultyContext(courseId2, facultyId1, name3);
                createGetGroupByCourseAndFacultyContext(courseId1, facultyId2, name3);

                securePerform(get("/groups/course/{courseId}/faculty/{facultyId}/", courseId1, facultyId1)
                        .queryParam("sort", "name,asc"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$", hasSize(2)))
                        .andExpectAll(context1.getMatchers("$[0]"))
                        .andExpectAll(context2.getMatchers("$[1]"));
            }
        };
    }

    @Test
    void getGroupByCourseAndFaculty__otherAsAdmin__valid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                long userId = ef.createEmployee();

                long courseId1 = ef.createCourse(userId);
                long courseId2 = ef.createCourse(userId);

                String name1 = "A" + faker.lorem().sentence(1);
                String name2 = "B" + faker.lorem().sentence(1);
                String name3 = "C" + faker.lorem().sentence(1);

                long facultyId1 = ef.createFaculty();
                long facultyId2 = ef.createFaculty();

                var context1 = createGetGroupByCourseAndFacultyContext(courseId1, facultyId1, name1);
                var context2 = createGetGroupByCourseAndFacultyContext(courseId1, facultyId1, name2);
                createGetGroupByCourseAndFacultyContext(courseId2, facultyId1, name3);
                createGetGroupByCourseAndFacultyContext(courseId1, facultyId2, name3);

                securePerform(get("/groups/course/{courseId}/faculty/{facultyId}/", courseId1, facultyId1)
                        .queryParam("sort", "name,asc"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$", hasSize(2)))
                        .andExpectAll(context1.getMatchers("$[0]"))
                        .andExpectAll(context2.getMatchers("$[1]"));
            }
        };
    }

    @Test
    void getGroupByCourseAndFaculty__withCourseToken__valid() {
        new WithCourseToken() {
            @Override
            void run() throws Exception {
                long courseId1 = getCourseId();
                long courseId2 = ef.createCourse(getOwnerId());

                String name1 = "A" + faker.lorem().sentence(1);
                String name2 = "B" + faker.lorem().sentence(1);
                String name3 = "C" + faker.lorem().sentence(1);

                long facultyId1 = ef.createFaculty();
                long facultyId2 = ef.createFaculty();

                var context1 = createGetGroupByCourseAndFacultyContext(courseId1, facultyId1, name1);
                var context2 = createGetGroupByCourseAndFacultyContext(courseId1, facultyId1, name2);
                createGetGroupByCourseAndFacultyContext(courseId2, facultyId1, name3);
                createGetGroupByCourseAndFacultyContext(courseId1, facultyId2, name3);

                securePerform(get("/groups/course/{courseId}/faculty/{facultyId}/", courseId1, facultyId1)
                        .queryParam("sort", "name,asc"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$", hasSize(2)))
                        .andExpectAll(context1.getMatchers("$[0]"))
                        .andExpectAll(context2.getMatchers("$[1]"));
            }
        };
    }

    @Test
    void getGroupByCourseAndFaculty__otherAsTeacher__invalid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                long courseId = ef.createCourse();
                long facultyId = ef.createFaculty();

                securePerform(get("/groups/course/{courseId}/faculty/{facultyId}", courseId, facultyId)
                        .queryParam("sort", "name,asc"))
                        .andExpect(status().isForbidden());
            }
        };
    }

    @Test
    void getGroupByCourseAndFaculty__courseNotExists__invalid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                long courseId = ef.createCourse();
                String name = faker.lorem().sentence(1);
                long facultyId = ef.createFaculty();

                createGetGroupByFacultyContext(courseId, facultyId, name);

                securePerform(get("/groups/course/{courseId}/faculty/{facultyId}", courseId + 1000, facultyId)
                        .queryParam("sort", "name,asc"))
                        .andExpect(status().isNotFound());
            }
        };
    }

    @Test
    void getGroupByCourseAndFaculty__facultyNotExists__invalid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                long courseId = ef.createCourse();
                String name = faker.lorem().sentence(1);
                long facultyId = ef.createFaculty();

                var context = createGetGroupByFacultyContext(courseId, facultyId, name);

                securePerform(get("/groups/course/{courseId}/faculty/{facultyId}", courseId, facultyId + 1000)
                        .queryParam("sort", "name,asc"))
                        .andExpect(status().isNotFound());
            }
        };
    }

    @Test
    void getGroupByCourseAndFaculty__notAuthenticated__invalid() throws Exception {
        long courseId = ef.createCourse();
        long facultyId = ef.createFaculty();
        mvc.perform(get("/groups/course/{courseId}/faculty/{facultyId}", courseId, facultyId))
                .andExpect(status().isUnauthorized());
    }

    // ================================================================================================================

    RequestContext<ObjectNode> getCreateGroupContext(long courseId, long facultyId, String name) {
        ObjectNode request = objectMapper.createObjectNode()
                .put("course", courseId)
                .put("faculty", facultyId)
                .put("name", name);

        Function<String, ResultMatcher[]> matchers = prefix -> new ResultMatcher[]{
                jsonPath(prefix + ".id").isNumber(),
                jsonPath(prefix + ".course").value(courseId),
                jsonPath(prefix + ".faculty").value(facultyId),
                jsonPath(prefix + ".name").value(name)
        };

        return new RequestContext<>(request, matchers);
    }

    @Test
    void createGroup__admin__valid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                long courseId = ef.createCourse(ef.createEmployee());
                long facultyId = ef.createFaculty();
                String name = faker.lorem().sentence(1);

                var context = getCreateGroupContext(courseId, facultyId, name);

                ObjectNode request = context.getRequest();

                MvcResult mvcResult = securePerform(post("/groups/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isCreated())
                        .andExpectAll(context.getMatchers())
                        .andReturn();

                int id = JsonPath.read(mvcResult.getResponse().getContentAsString(), "$.id");

                securePerform(get("/groups/{id}", id))
                        .andExpect(status().isOk())
                        .andExpectAll(context.getMatchers());
            }
        };
    }

    @Test
    void createGroup__notAdmin__invalid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                long courseId = ef.createCourse(getSelfEmployeeIdAsLong());
                long facultyId = ef.createFaculty();
                String name = faker.lorem().sentence(1);

                var context = getCreateGroupContext(courseId, facultyId, name);

                ObjectNode request = context.getRequest();

                securePerform(post("/groups/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isForbidden());
            }
        };
    }


    /*@Test
    void createGroup__withCourseToken__valid() {
        new WithCourseToken() {
            @Override
            void run() throws Exception {
                long courseId = getCourseId();

                var context = getCreateGroupContext(courseId, facultyId, name);

                ObjectNode request = context.getRequest();

                MvcResult mvcResult = securePerform(post("/groups/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isCreated())
                        .andExpectAll(context.getMatchers())
                        .andReturn();

                int id = JsonPath.read(mvcResult.getResponse().getContentAsString(), "$.id");

                securePerform(get("/groups/{id}", id))
                        .andExpect(status().isOk())
                        .andExpectAll(context.getMatchers());
            }
        };
    }*/

    @Test
    void createGroup__nameAndFacultyNotUnique__invalid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                long courseId = ef.createCourse(ef.createEmployee());
                long facultyId = ef.createFaculty();
                String name = faker.lorem().sentence(1);

                var context = getCreateGroupContext(courseId, facultyId, name);

                ef.createGroup(ef.bag().withCourseId(courseId)
                        .withDto(GroupDto.builder()
                                .name(name)
                                .faculty(facultyId)
                                .build())
                );

                ObjectNode request = context.getRequest();

                securePerform(post("/groups/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isConflict());
            }
        };
    }

    @Test
    void createGroup__notAuthenticated__invalid() throws Exception {
        long courseId = ef.createCourse();
        long facultyId = ef.createFaculty();
        String name = faker.lorem().sentence(1);

        var context = getCreateGroupContext(courseId, facultyId, name);

        ObjectNode request = context.getRequest();

        mvc.perform(post("/groups/")
                .contentType(MediaType.APPLICATION_JSON)
                .contentType(request.toString()))
                .andExpect(status().isUnauthorized());
    }

    // ================================================================================================================

    RequestContext<ObjectNode> createPutGroupContext(long courseId, long facultyId, String name) {

        ObjectNode request = objectMapper.createObjectNode()
                .put("course", courseId)
                .put("name", name)
                .put("faculty", facultyId);

        Function<String, ResultMatcher[]> matchers = prefix -> new ResultMatcher[]{
                jsonPath(prefix + ".id").isNumber(),
                jsonPath(prefix + ".course").value(courseId),
                jsonPath(prefix + ".name").value(name),
                jsonPath(prefix + ".faculty").value(facultyId),
        };

        return new RequestContext<>(request, matchers);
    }

    @Test
    void putGroup__notAdmin__invalid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                long courseId = ef.createCourse(getSelfEmployeeIdAsLong());
                long facultyId = ef.createFaculty();
                String name1 = faker.lorem().sentence(1);
                String name2 = faker.lorem().sentence(1);

                long id = ef.createGroup(ef.bag().withCourseId(courseId).withFacultyId(facultyId)
                        .withDto(GroupDto.builder()
                                .name(name1)
                                .build()
                        ));

                var context = createPutGroupContext(courseId, facultyId, name2);
                ObjectNode request = context.getRequest();

                securePerform(put("/groups/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isForbidden());
            }
        };
    }

    @Test
    void putGroup__admin__valid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                long courseId = ef.createCourse();
                long facultyId = ef.createFaculty();
                String name1 = faker.lorem().sentence(1);
                String name2 = faker.lorem().sentence(1);
                
                long id = ef.createGroup(ef.bag().withCourseId(courseId).withFacultyId(facultyId)
                        .withDto(GroupDto.builder()
                                .name(name1)
                                .build()
                        ));

                var context = createPutGroupContext(courseId, facultyId, name2);
                ObjectNode request = context.getRequest();

                securePerform(put("/groups/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isOk())
                        .andExpectAll(context.getMatchers());

                securePerform(get("/groups/{id}", id))
                        .andExpect(status().isOk())
                        .andExpectAll(context.getMatchers());
            }
        };
    }

    @Test
    void putGroup__withCourseToken__invalid() {
        new WithCourseToken() {
            @Override
            void run() throws Exception {
                long courseId = ef.createCourse(getOwnerId());
                long facultyId = ef.createFaculty();
                String name1 = faker.lorem().sentence(1);
                String name2 = faker.lorem().sentence(1);

                long id = ef.createGroup(ef.bag().withCourseId(courseId).withFacultyId(facultyId)
                        .withDto(GroupDto.builder()
                                .name(name1)
                                .build()
                        ));

                var context = createPutGroupContext(courseId, facultyId, name2);
                ObjectNode request = context.getRequest();

                securePerform(put("/groups/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isForbidden());
            }
        };
    }

    @Test
    void putGroup__courseChanged__invalid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                long courseId1 = ef.createCourse();
                long courseId2 = ef.createCourse();
                long facultyId = ef.createFaculty();
                String name1 = faker.lorem().sentence(1);
                String name2 = faker.lorem().sentence(1);

                long id = ef.createGroup(ef.bag().withCourseId(courseId1).withFacultyId(facultyId)
                        .withDto(GroupDto.builder()
                                .name(name1)
                                .build()
                        ));

                var context = createPutGroupContext(courseId2, facultyId, name2);
                ObjectNode request = context.getRequest();

                securePerform(put("/groups/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isBadRequest());
            }
        };
    }

    @Test
    void putGroup__facultyChanged__invalid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                long courseId = ef.createCourse();
                String name1 = faker.lorem().sentence(1);
                String name2 = faker.lorem().sentence(1);
                long facultyId1 = ef.createFaculty();
                long facultyId2 = ef.createFaculty();

                long id = ef.createGroup(ef.bag().withCourseId(courseId).withFacultyId(facultyId1)
                        .withDto(GroupDto.builder()
                                .name(name1)
                                .build()
                        ));

                var context = createPutGroupContext(courseId, facultyId2, name2);
                ObjectNode request = context.getRequest();

                securePerform(put("/groups/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isBadRequest());
            }
        };
    }

    @Test
    void putGroup__notExists__invalid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                long courseId = ef.createCourse();
                long facultyId = ef.createFaculty();
                String name = faker.lorem().sentence(1);
                long id = ef.createGroup(ef.bag().withCourseId(courseId).withFacultyId(facultyId)
                        .withDto(GroupDto.builder()
                                .name(name)
                                .build()));

                RequestContext<ObjectNode> context = createPutGroupContext(courseId, facultyId, name);
                ObjectNode request = context.getRequest();

                securePerform(put("/groups/{id}", id + 1000)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpectAll(status().isNotFound());
            }
        };
    }

    @Test
    void putGroup__notAuthenticated__invalid() throws Exception {
        long courseId = ef.createCourse();
        long facultyId = ef.createFaculty();
        String name = faker.lorem().sentence(1);
        long id = ef.createGroup(ef.bag().withCourseId(courseId).withFacultyId(facultyId)
                .withDto(GroupDto.builder()
                        .name(name)
                        .build()));

        RequestContext<ObjectNode> context = createPutGroupContext(courseId, facultyId, name);
        ObjectNode request = context.getRequest();

        mvc.perform(put("/groups/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(request.toString()))
                .andExpect(status().isUnauthorized());
    }

    // ================================================================================================================

    RequestContext<ObjectNode> createPatchGroupContext(long courseId, long facultyId, String name) {
        ObjectNode request = objectMapper.createObjectNode()
                .put("course", courseId)
                .put("faculty", facultyId)
                .put("name", name);

        Function<String, ResultMatcher[]> matchers = prefix -> new ResultMatcher[]{
                jsonPath(prefix + ".id").isNumber(),
                jsonPath(prefix + ".course").value(courseId),
                jsonPath(prefix + ".name").value(name),
                jsonPath(prefix + ".faculty").isNotEmpty(),
        };

        return new RequestContext<>(request, matchers);
    }

    @Test
    void patchGroup__notAdmin__invalid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                long courseId = ef.createCourse(getSelfEmployeeIdAsLong());
                long facultyId = ef.createFaculty();
                String name1 = faker.lorem().sentence(1);
                String name2 = faker.lorem().sentence(1);

                long id = ef.createGroup(ef.bag().withCourseId(courseId).withFacultyId(facultyId)
                        .withDto(GroupDto.builder()
                                .name(name1)
                                .build()
                        ));

                var context = createPatchGroupContext(courseId, facultyId, name2);
                ObjectNode request = context.getRequest();

                securePerform(put("/groups/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isForbidden());
            }
        };
    }

    @Test
    void patchGroup__admin__valid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                long courseId = ef.createCourse();
                long facultyId = ef.createFaculty();
                String name1 = faker.lorem().sentence(1);
                String name2 = faker.lorem().sentence(1);

                long id = ef.createGroup(ef.bag().withCourseId(courseId).withFacultyId(facultyId)
                        .withDto(GroupDto.builder()
                                .name(name1)
                                .build()
                        ));

                var context = createPatchGroupContext(courseId, facultyId, name2);
                ObjectNode request = context.getRequest();

                securePerform(put("/groups/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isOk())
                        .andExpectAll(context.getMatchers());

                securePerform(get("/groups/{id}", id))
                        .andExpect(status().isOk())
                        .andExpectAll(context.getMatchers());
            }
        };
    }

    @Test
    void patchGroup__withCourseToken__invalid() {
        new WithCourseToken() {
            @Override
            void run() throws Exception {
                long courseId = ef.createCourse(getOwnerId());
                long facultyId = ef.createFaculty();
                String name1 = faker.lorem().sentence(1);
                String name2 = faker.lorem().sentence(1);

                long id = ef.createGroup(ef.bag().withCourseId(courseId).withFacultyId(facultyId)
                        .withDto(GroupDto.builder()
                                .name(name1)
                                .build()
                        ));

                var context = createPatchGroupContext(courseId, facultyId, name2);
                ObjectNode request = context.getRequest();

                securePerform(put("/groups/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isForbidden());
            }
        };
    }

    @Test
    void patchGroup__courseChanged__invalid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                long courseId1 = ef.createCourse();
                long courseId2 = ef.createCourse();
                long facultyId = ef.createFaculty();
                String name1 = faker.lorem().sentence(1);
                String name2 = faker.lorem().sentence(1);

                long id = ef.createGroup(ef.bag().withCourseId(courseId1).withFacultyId(facultyId)
                        .withDto(GroupDto.builder()
                                .name(name1)
                                .build()
                        ));

                var context = createPatchGroupContext(courseId2, facultyId, name2);
                ObjectNode request = context.getRequest();

                securePerform(put("/groups/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isBadRequest());
            }
        };
    }

    @Test
    void patchGroup__facultyChanged__invalid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                long courseId = ef.createCourse();
                String name1 = faker.lorem().sentence(1);
                String name2 = faker.lorem().sentence(1);
                long facultyId1 = ef.createFaculty();
                long facultyId2 = ef.createFaculty();

                long id = ef.createGroup(ef.bag().withCourseId(courseId).withFacultyId(facultyId1)
                        .withDto(GroupDto.builder()
                                .faculty(facultyId1)
                                .name(name1)
                                .build()
                        ));

                var context = createPatchGroupContext(courseId, facultyId2, name2);
                ObjectNode request = context.getRequest();

                securePerform(put("/groups/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isBadRequest());
            }
        };
    }

    @Test
    void patchGroup__notExists__invalid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                long courseId = ef.createCourse();
                long facultyId = ef.createFaculty();
                String name = faker.lorem().sentence(1);
                long id = ef.createGroup(ef.bag().withCourseId(courseId).withFacultyId(facultyId)
                        .withDto(GroupDto.builder()
                                .name(name)
                                .build()));

                RequestContext<ObjectNode> context = createPatchGroupContext(courseId, facultyId, name);
                ObjectNode request = context.getRequest();

                securePerform(put("/groups/{id}", id + 1000)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpectAll(status().isNotFound());
            }
        };
    }

    @Test
    void patchGroup__notAuthenticated__invalid() throws Exception {
        long courseId = ef.createCourse();
        long facultyId = ef.createFaculty();
        String name = faker.lorem().sentence(1);
        long id = ef.createGroup(ef.bag().withCourseId(courseId).withFacultyId(facultyId)
                .withDto(GroupDto.builder()
                        .name(name)
                        .build()));

        RequestContext<ObjectNode> context = createPatchGroupContext(courseId, facultyId, name);
        ObjectNode request = context.getRequest();

        mvc.perform(put("/groups/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                .andExpect(status().isUnauthorized());
    }

    // ================================================================================================================

    @Test
    void deleteGroup__notAdmin__invalid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                long id = ef.createGroup(getSelfEmployeeIdAsLong());

                securePerform(delete("/groups/{id}", id))
                        .andExpect(status().isForbidden());
            }
        };
    }

    @Test
    void deleteGroup__admin__valid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                long id = ef.createGroup();

                securePerform(delete("/groups/{id}", id))
                        .andExpect(status().isOk());

                securePerform(get("/groups/{id}", id))
                        .andExpect(status().isNotFound());
            }
        };
    }

    @Test
    void deleteGroup__notFound__invalid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                long id = ef.createGroup();

                securePerform(delete("/groups/{id}", id + 1000))
                        .andExpect(status().isNotFound());
            }
        };
    }

    @Test
    void deleteGroup__notAuthenticated__invalid() throws Exception {
                long id = ef.createGroup();

                mvc.perform(delete("/groups/{id}", id))
                        .andExpect(status().isUnauthorized());
        };
}
