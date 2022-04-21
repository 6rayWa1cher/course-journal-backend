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

    RequestContext<Long> createGetGroupByIdContextWithCourse(long courseId) {
        String name = "ФИиИТ";
        String faculty = "ПМиК";

        long id = groupService.create(GroupDto.builder()
                .name(name)
                .faculty(faculty)
                .course(courseId)
                .build()).getId();

        Function<String, ResultMatcher[]> matchers = prefix -> new ResultMatcher[]{
                jsonPath(prefix + ".id").value(id),
                jsonPath(prefix + ".name").value(name),
                jsonPath(prefix + ".course").value(courseId),
                jsonPath(prefix + ".faculty").value(faculty)
        };

        return RequestContext.<Long>builder()
                .request(id)
                .matchersSupplier(matchers)
                .data(Map.of("courseId", Long.toString(courseId)))
                .build();
    }

    RequestContext<Long> createGetGroupByIdContext(long userId) {
        long courseId = ef.createCourse(userId);
        return createGetGroupByIdContextWithCourse(courseId);
    }

    @Test
    void getGroupById__self__valid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                var context = createGetGroupByIdContext(getIdAsLong());

                long id = context.getRequest();
                ResultMatcher[] matchers = context.getMatchers();

                securePerform(get("/groups/{id}", id))
                        .andExpect(status().isOk())
                        .andExpectAll(matchers);
            }
        };
    }

    @Test
    void getGroupById__otherAsAdmin__valid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                var context = createGetGroupByIdContext(ef.createUser());

                long id = context.getRequest();
                ResultMatcher[] matchers = context.getMatchers();

                securePerform(get("/groups/{id}", id))
                        .andExpect(status().isOk())
                        .andExpectAll(matchers);
            }
        };

    }

    @Test
    void getGroupById__otherAsTeacher__invalid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                var context = createGetGroupByIdContext(ef.createUser());

                long id = context.getRequest();
                ResultMatcher[] matchers = context.getMatchers();

                securePerform(get("/groups/{id}", id))
                        .andExpect(status().isForbidden());
            }
        };
    }

    @Test
    void getGroupById__withCourseToken__valid() {
        new WithCourseToken() {
            @Override
            void run() throws Exception {
                var context = createGetGroupByIdContextWithCourse(getCourseId());

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
        var context = createGetGroupByIdContext(ef.createUser());

        long id = context.getRequest();

        mvc.perform(get("/groups/{id}", id))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getGroupById__notExists__invalid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                var context = createGetGroupByIdContext(ef.createUser());

                long id = context.getRequest();

                securePerform(get("/groups/{id}", id + 1000))
                        .andExpect(status().isNotFound());
            }
        };
    }

    // ================================================================================================================

    RequestContext<Long> createGetGroupByFacultyContext(long courseId, String faculty, String name) {
        long id = groupService.create(GroupDto.builder()
                .course(courseId)
                .faculty(faculty)
                .name(name)
                .build()).getId();

        Function<String, ResultMatcher[]> matchers = prefix -> new ResultMatcher[]{
                jsonPath(prefix + ".id").value(id),
                jsonPath(prefix + ".name").value(name),
                jsonPath(prefix + ".course").value(courseId),
                jsonPath(prefix + ".faculty").value(faculty)
        };

        return RequestContext.<Long>builder()
                .request(id)
                .matchersSupplier(matchers)
                .build();
    }

    @Test
    void getGroupByFaculty__self__valid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                long courseId = ef.createCourse(getIdAsLong());

                String name1 = "A" + "ФИиИТ";
                String name2 = "B" + "ПИ";

                String faculty1 = "ПМиК";
                String faculty2 = "МФ";

                var context1 = createGetGroupByFacultyContext(courseId, name1, faculty1);
                var context2 = createGetGroupByFacultyContext(courseId, name2, faculty1);
                createGetGroupByFacultyContext(courseId, name1, faculty2);

                securePerform(get("/groups/faculty/{id}", faculty1)
                        .queryParam("sort", "name,asc"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$", hasSize(2)))
                        .andExpectAll(context1.getMatchers("$[0]"))
                        .andExpectAll(context2.getMatchers("$[1]"));
            }
        };
    }

    @Test
    void getGroupByFaculty__otherAsAdmin__valid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                long courseId = ef.createCourse(ef.createUser());

                String name1 = "A" + "ФИиИТ";
                String name2 = "B" + "ПИ";

                String faculty1 = "ПМиК";
                String faculty2 = "МФ";

                var context1 = createGetGroupByFacultyContext(courseId, name1, faculty1);
                var context2 = createGetGroupByFacultyContext(courseId, name2, faculty1);
                createGetGroupByFacultyContext(courseId, name1, faculty2);

                securePerform(get("/groups/faculty/{id}", faculty1)
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

                String name1 = "A" + "ФИиИТ";
                String name2 = "B" + "ПИ";

                String faculty1 = "ПМиК";
                String faculty2 = "МФ";

                var context1 = createGetGroupByFacultyContext(courseId, name1, faculty1);
                var context2 = createGetGroupByFacultyContext(courseId, name2, faculty1);
                createGetGroupByFacultyContext(courseId, name1, faculty2);

                securePerform(get("/groups/faculty/{id}", faculty1)
                        .queryParam("sort", "name,asc"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$", hasSize(2)))
                        .andExpectAll(context1.getMatchers("$[0]"))
                        .andExpectAll(context2.getMatchers("$[1]"));
            }
        };
    }

    @Test
    void getGroupByFaculty__otherAsTeacher__invalid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                String faculty = "ПМиК";

                securePerform(get("/groups/faculty/{id}", faculty)
                        .queryParam("sort", "name,asc"))
                        .andExpect(status().isForbidden());
            }
        };
    }

    @Test
    void getGroupByFaculty__facultyNotExists__invalid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                long courseId = ef.createCourse();
                String name = "ФИиИТ";
                String faculty = "ПМиК";

                var context = createGetGroupByFacultyContext(courseId, name, faculty);

                securePerform(get("/groups/faculty/{id}", faculty + "A")
                        .queryParam("sort", "name,asc"))
                        .andExpect(status().isNotFound());
            }
        };
    }

    @Test
    void getGroupByFaculty__notAuthenticated__invalid() throws Exception {
        String faculty = "ПМиК";
        mvc.perform(get("/groups/faculty/{id}", faculty))
                .andExpect(status().isUnauthorized());
    }


    // ================================================================================================================

    RequestContext<Long> createGetGroupByCourseContext(long courseId, String name) {
        String faculty = "ПМиК";

        long id = groupService.create(GroupDto.builder()
                .course(courseId)
                .faculty(faculty)
                .name(name)
                .build()).getId();

        Function<String, ResultMatcher[]> matchers = prefix -> new ResultMatcher[]{
                jsonPath(prefix + ".id").value(id),
                jsonPath(prefix + ".name").value(name),
                jsonPath(prefix + ".course").value(courseId),
                jsonPath(prefix + ".faculty").value(faculty)
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
                long courseId1 = ef.createCourse(getIdAsLong());
                long courseId2 = ef.createCourse(getIdAsLong());

                String name1 = "A" + "ФИиИТ";
                String name2 = "B" + "ПИ";

                var context1 = createGetGroupByCourseContext(courseId1, name1);
                var context2 = createGetGroupByCourseContext(courseId1, name2);
                createGetGroupByCourseContext(courseId2, name1);

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
                long courseId1 = ef.createCourse(ef.createUser());
                long courseId2 = ef.createCourse(ef.createUser());

                String name1 = "A" + "ФИиИТ";
                String name2 = "B" + "ПИ";

                var context1 = createGetGroupByCourseContext(courseId1, name1);
                var context2 = createGetGroupByCourseContext(courseId1, name2);
                createGetGroupByCourseContext(courseId2, name1);

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
                long courseId2 = ef.createCourse(ef.createUser());
                String name1 = "A" + "ФИиИТ";
                String name2 = "B" + "ПИ";

                var context1 = createGetGroupByCourseContext(courseId1, name1);
                var context2 = createGetGroupByCourseContext(courseId1, name2);
                createGetGroupByCourseContext(courseId2, name1);

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
                long courseId = ef.createCourse(ef.createUser());

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
                String name = "ФИиИТ";

                createGetGroupByCourseContext(courseId, name);

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

    RequestContext<Long> createGetGroupByCourseAndFacultyContext(long courseId, String faculty, String name) {
        long id = groupService.create(GroupDto.builder()
                .course(courseId)
                .faculty(faculty)
                .name(name)
                .build()).getId();

        Function<String, ResultMatcher[]> matchers = prefix -> new ResultMatcher[]{
                jsonPath(prefix + ".id").value(id),
                jsonPath(prefix + ".name").value(name),
                jsonPath(prefix + ".course").value(courseId),
                jsonPath(prefix + ".faculty").value(faculty)
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
                long courseId1 = ef.createCourse(getIdAsLong());
                long courseId2 = ef.createCourse(getIdAsLong());

                String name1 = "A" + "ФИиИТ";
                String name2 = "B" + "ПИ";

                String faculty1 = "ПМиК";
                String faculty2 = "МФ";

                var context1 = createGetGroupByCourseAndFacultyContext(courseId1, name1, faculty1);
                var context2 = createGetGroupByCourseAndFacultyContext(courseId1, name2, faculty1);
                createGetGroupByCourseAndFacultyContext(courseId2, name1, faculty1);
                createGetGroupByCourseAndFacultyContext(courseId1, name1, faculty2);

                securePerform(get("/groups/course/{courseId}/faculty/{facultyId}/", courseId1, faculty1)
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
                long userId = ef.createUser();

                long courseId1 = ef.createCourse(userId);
                long courseId2 = ef.createCourse(userId);

                String name1 = "A" + "ФИиИТ";
                String name2 = "B" + "ПИ";

                String faculty1 = "ПМиК";
                String faculty2 = "МФ";

                var context1 = createGetGroupByCourseAndFacultyContext(courseId1, name1, faculty1);
                var context2 = createGetGroupByCourseAndFacultyContext(courseId1, name2, faculty1);
                createGetGroupByCourseAndFacultyContext(courseId1, name1, faculty2);
                createGetGroupByCourseAndFacultyContext(courseId2, name1, faculty1);

                securePerform(get("/groups/course/{courseId}/faculty/{facultyId}", courseId1, faculty1)
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

                String name1 = "A" + "ФИиИТ";
                String name2 = "B" + "ПИ";

                String faculty1 = "ПМиК";
                String faculty2 = "МФ";

                var context1 = createGetGroupByCourseAndFacultyContext(courseId1, name1, faculty1);
                var context2 = createGetGroupByCourseAndFacultyContext(courseId1, name2, faculty1);
                createGetGroupByCourseAndFacultyContext(courseId1, name1, faculty2);
                createGetGroupByCourseAndFacultyContext(courseId2, name1, faculty1);

                securePerform(get("/groups/course/{courseId}/faculty/{facultyId}",  courseId1, faculty1)
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
                Long courseId = ef.createCourse();
                String faculty = "ПМиК";

                securePerform(get("/groups/course/{courseId}/faculty/{facultyId}", courseId, faculty)
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
                String name = "ФИиИТ";
                String faculty = "ПМиК";

                var context = createGetGroupByFacultyContext(courseId, name, faculty);

                securePerform(get("/groups/course/{courseId}/faculty/{facultyId}", courseId + 1000, faculty)
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
                String name = "ФИиИТ";
                String faculty = "ПМиК";

                var context = createGetGroupByFacultyContext(courseId, name, faculty);

                securePerform(get("/groups/course/{courseId}/faculty/{facultyId}", courseId, faculty + "A")
                        .queryParam("sort", "name,asc"))
                        .andExpect(status().isNotFound());
            }
        };
    }

    @Test
    void getGroupByCourseAndFaculty__notAuthenticated__invalid() throws Exception {
        Long courseId = ef.createCourse();
        String faculty = "ПМиК";
        mvc.perform(get("/groups/course/{courseId}/faculty/{facultyId}", courseId, faculty))
                .andExpect(status().isUnauthorized());
    }

    // ================================================================================================================

    RequestContext<ObjectNode> getCreateGroupContext(long courseId, String name, String faculty) {
        ObjectNode request = objectMapper.createObjectNode()
                .put("course", courseId)
                .put("faculty", faculty)
                .put("name", name);

        Function<String, ResultMatcher[]> matchers = prefix -> new ResultMatcher[]{
                jsonPath(prefix + ".id").isNumber(),
                jsonPath(prefix + ".course").value(courseId),
                jsonPath(prefix + "faculty").value(faculty),
                jsonPath(prefix + "name").value(name)
        };

        return new RequestContext<>(request, matchers);
    }

    @Test
    void createGroup__self__valid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                long courseId = ef.createCourse(getIdAsLong());
                String faculty = faker.lorem().sentence(1);
                String name = faker.lorem().sentence(1);

                var context = getCreateGroupContext(courseId, name, faculty);

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
    void createGroup__otherAsAdmin__valid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                long courseId = ef.createCourse(ef.createUser());
                String faculty = faker.lorem().sentence(1);
                String name = faker.lorem().sentence(1);

                var context = getCreateGroupContext(courseId, name, faculty);

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
    void createGroup__otherAsTeacher__invalid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                long courseId = ef.createCourse(ef.createUser());
                String faculty = faker.lorem().sentence(1);
                String name = faker.lorem().sentence(1);

                var context = getCreateGroupContext(courseId, name, faculty);

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

                var context = getCreateGroupContext(courseId, name, faculty);

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
                long courseId = ef.createCourse(ef.createUser());
                String faculty = faker.lorem().sentence(1);
                String name = faker.lorem().sentence(1);

                var context = getCreateGroupContext(courseId, name, faculty);

                ef.createGroup(ef.bag().withCourseId(courseId)
                        .withDto(GroupDto.builder()
                                .name(name)
                                .faculty(faculty)
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
        String faculty = faker.lorem().sentence(1);
        String name = faker.lorem().sentence(1);

        var context = getCreateGroupContext(courseId, name, faculty);

        ObjectNode request = context.getRequest();

        mvc.perform(post("/groups/")
                .contentType(MediaType.APPLICATION_JSON)
                .contentType(request.toString()))
                .andExpect(status().isUnauthorized());
    }

    // ================================================================================================================

    RequestContext<ObjectNode> createPutGroupContext(long courseId, String name) {
        String faculty = faker.lorem().sentence(1);

        ObjectNode request = objectMapper.createObjectNode()
                .put("course", courseId)
                .put("name", name)
                .put("faculty", faculty);

        Function<String, ResultMatcher[]> matchers = prefix -> new ResultMatcher[]{
                jsonPath(prefix + ".id").isNumber(),
                jsonPath(prefix + ".course").value(courseId),
                jsonPath(prefix + ".name").value(name),
                jsonPath(prefix + ".faculty").value(faculty),
        };

        return new RequestContext<>(request, matchers);
    }

    RequestContext<ObjectNode> createPutGroupContextWithFaculty(long courseId, String name, String faculty) {
        ObjectNode request = objectMapper.createObjectNode()
                .put("course", courseId)
                .put("name", name)
                .put("faculty", faculty);

        Function<String, ResultMatcher[]> matchers = prefix -> new ResultMatcher[]{
                jsonPath(prefix + ".id").isNumber(),
                jsonPath(prefix + ".course").value(courseId),
                jsonPath(prefix + ".name").value(name),
                jsonPath(prefix + ".faculty").value(faculty),
        };

        return new RequestContext<>(request, matchers);
    }

    @Test
    void putGroup__self__valid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                long courseId = ef.createCourse(getIdAsLong());
                String name1 = faker.lorem().sentence(1);
                String name2 = faker.lorem().sentence(1);

                long id = ef.createGroup(ef.bag().withCourseId(courseId)
                        .withDto(GroupDto.builder()
                                .name(name1)
                                .build()
                        ));

                var context = createPutGroupContext(courseId, name2);
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
    void putGroup__otherAsAdmin__valid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                long courseId = ef.createCourse();
                String name1 = faker.lorem().sentence(1);
                String name2 = faker.lorem().sentence(1);

                long id = ef.createGroup(ef.bag().withCourseId(courseId)
                        .withDto(GroupDto.builder()
                                .name(name1)
                                .build()
                        ));

                var context = createPutGroupContext(courseId, name2);
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
    void putGroup__otherAsTeacher__invalid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                long courseId = ef.createCourse();
                String name1 = faker.lorem().sentence(1);
                String name2 = faker.lorem().sentence(1);

                long id = ef.createGroup(ef.bag().withCourseId(courseId)
                        .withDto(GroupDto.builder()
                                .name(name1)
                                .build()
                        ));

                var context = createPutGroupContext(courseId, name2);
                ObjectNode request = context.getRequest();

                securePerform(put("/groups/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isForbidden());
            }
        };
    }

    @Test
    void putGroup__withCourseToken__valid() {
        new WithCourseToken() {
            @Override
            void run() throws Exception {
                long courseId = ef.createCourse(getOwnerId());
                String name1 = faker.lorem().sentence(1);
                String name2 = faker.lorem().sentence(1);

                long id = ef.createGroup(ef.bag().withCourseId(courseId)
                        .withDto(GroupDto.builder()
                                .name(name1)
                                .build()
                        ));

                var context = createPutGroupContext(courseId, name2);
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
    void putGroup__courseChanged__invalid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                long courseId1 = ef.createCourse();
                long courseId2 = ef.createCourse();
                String name1 = faker.lorem().sentence(1);
                String name2 = faker.lorem().sentence(1);

                long id = ef.createGroup(ef.bag().withCourseId(courseId1)
                        .withDto(GroupDto.builder()
                                .name(name1)
                                .build()
                        ));

                var context = createPutGroupContext(courseId2, name2);
                ObjectNode request = context.getRequest();

                securePerform(put("/groups/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isConflict());
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
                String faculty1 = faker.lorem().sentence(1);
                String faculty2 = faker.lorem().sentence(1);

                long id = ef.createGroup(ef.bag().withCourseId(courseId)
                        .withDto(GroupDto.builder()
                                .faculty(faculty1)
                                .name(name1)
                                .build()
                        ));

                var context = createPutGroupContextWithFaculty(courseId, name2, faculty2);
                ObjectNode request = context.getRequest();

                securePerform(put("/groups/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isConflict());
            }
        };
    }

    @Test
    void putGroup__notExists__invalid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                long courseId = ef.createCourse();
                String name = faker.lorem().sentence(1);
                long id = ef.createGroup(ef.bag().withCourseId(courseId)
                        .withDto(GroupDto.builder()
                                .name(name)
                                .build()));

                RequestContext<ObjectNode> context = createPutGroupContext(courseId, name);
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
        String name = faker.lorem().sentence(1);
        long id = ef.createGroup(ef.bag().withCourseId(courseId)
                .withDto(GroupDto.builder()
                        .name(name)
                        .build()));

        RequestContext<ObjectNode> context = createPutGroupContext(courseId, name);
        ObjectNode request = context.getRequest();

        mvc.perform(put("/groups/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(request.toString()))
                .andExpect(status().isUnauthorized());
    }

    // ================================================================================================================

    RequestContext<ObjectNode> createPatchGroupContext(long courseId, String name) {
        ObjectNode request = objectMapper.createObjectNode()
                .put("course", courseId)
                .put("name", name);

        Function<String, ResultMatcher[]> matchers = prefix -> new ResultMatcher[]{
                jsonPath(prefix + ".id").isNumber(),
                jsonPath(prefix + ".course").value(courseId),
                jsonPath(prefix + ".name").value(name),
                jsonPath(prefix + ".faculty").isNotEmpty(),
        };

        return new RequestContext<>(request, matchers);
    }

    RequestContext<ObjectNode> createPatchGroupContextWithFaculty(long courseId, String name, String faculty) {
        ObjectNode request = objectMapper.createObjectNode()
                .put("course", courseId)
                .put("faculty", faculty)
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
    void patchGroup__self__valid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                long courseId = ef.createCourse(getIdAsLong());
                String name1 = faker.lorem().sentence(1);
                String name2 = faker.lorem().sentence(1);

                long id = ef.createGroup(ef.bag().withCourseId(courseId)
                        .withDto(GroupDto.builder()
                                .name(name1)
                                .build()
                        ));

                var context = createPatchGroupContext(courseId, name2);
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
    void patchGroup__otherAsAdmin__valid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                long courseId = ef.createCourse();
                String name1 = faker.lorem().sentence(1);
                String name2 = faker.lorem().sentence(1);

                long id = ef.createGroup(ef.bag().withCourseId(courseId)
                        .withDto(GroupDto.builder()
                                .name(name1)
                                .build()
                        ));

                var context = createPatchGroupContext(courseId, name2);
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
    void patchGroup__otherAsTeacher__invalid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                long courseId = ef.createCourse();
                String name1 = faker.lorem().sentence(1);
                String name2 = faker.lorem().sentence(1);

                long id = ef.createGroup(ef.bag().withCourseId(courseId)
                        .withDto(GroupDto.builder()
                                .name(name1)
                                .build()
                        ));

                var context = createPatchGroupContext(courseId, name2);
                ObjectNode request = context.getRequest();

                securePerform(put("/groups/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isForbidden());
            }
        };
    }

    @Test
    void patchGroup__withCourseToken__valid() {
        new WithCourseToken() {
            @Override
            void run() throws Exception {
                long courseId = ef.createCourse(getOwnerId());
                String name1 = faker.lorem().sentence(1);
                String name2 = faker.lorem().sentence(1);

                long id = ef.createGroup(ef.bag().withCourseId(courseId)
                        .withDto(GroupDto.builder()
                                .name(name1)
                                .build()
                        ));

                var context = createPatchGroupContext(courseId, name2);
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
    void patchGroup__courseChanged__invalid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                long courseId1 = ef.createCourse();
                long courseId2 = ef.createCourse();
                String name1 = faker.lorem().sentence(1);
                String name2 = faker.lorem().sentence(1);

                long id = ef.createGroup(ef.bag().withCourseId(courseId1)
                        .withDto(GroupDto.builder()
                                .name(name1)
                                .build()
                        ));

                var context = createPatchGroupContext(courseId2, name2);
                ObjectNode request = context.getRequest();

                securePerform(put("/groups/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isConflict());
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
                String faculty1 = faker.lorem().sentence(1);
                String faculty2 = faker.lorem().sentence(1);

                long id = ef.createGroup(ef.bag().withCourseId(courseId)
                        .withDto(GroupDto.builder()
                                .faculty(faculty1)
                                .name(name1)
                                .build()
                        ));

                var context = createPatchGroupContextWithFaculty(courseId, name2, faculty2);
                ObjectNode request = context.getRequest();

                securePerform(put("/groups/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isConflict());
            }
        };
    }

    @Test
    void patchGroup__notExists__invalid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                long courseId = ef.createCourse();
                String name = faker.lorem().sentence(1);
                long id = ef.createGroup(ef.bag().withCourseId(courseId)
                        .withDto(GroupDto.builder()
                                .name(name)
                                .build()));

                RequestContext<ObjectNode> context = createPatchGroupContext(courseId, name);
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
        String name = faker.lorem().sentence(1);
        long id = ef.createGroup(ef.bag().withCourseId(courseId)
                .withDto(GroupDto.builder()
                        .name(name)
                        .build()));

        RequestContext<ObjectNode> context = createPatchGroupContext(courseId, name);
        ObjectNode request = context.getRequest();

        mvc.perform(put("/groups/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                .andExpect(status().isUnauthorized());
    }

    // ================================================================================================================

    @Test
    void deleteGroup__self__valid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                long id = ef.createGroup(getIdAsLong());

                securePerform(delete("/groups/{id}", id))
                        .andExpect(status().isOk());

                securePerform(get("/groups/{id}", id))
                        .andExpect(status().isNotFound());
            }
        };
    }

    @Test
    void deleteGroup__otherAsAdmin__valid() {
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
    void deleteGroup__otherAsTeacher__invalid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                long id = ef.createGroup();

                securePerform(delete("/groups/{id}", id))
                        .andExpect(status().isForbidden());
            }
        };
    }

    @Test
    void deleteGroup__withCourseToken__invalid() {
        new WithCourseToken() {
            @Override
            void run() throws Exception {
                long id = ef.createGroup(getOwnerId());

                securePerform(delete("/groups/{id}", id))
                        .andExpect(status().isForbidden());
            }
        };
    }

    @Test
    void deleteGroup__notFound__invalid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                long id = ef.createGroup(getIdAsLong());

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
