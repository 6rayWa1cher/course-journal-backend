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

import java.util.function.Function;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@ActiveProfiles("test")
public class GroupControllerIntegrationTests extends AbstractIntegrationTests {
    @Autowired
    GroupService groupService;

    RequestContext<Long> createGetGroupByIdContextWithCourse(long facultyId) {
        String name = faker.lorem().sentence(1);

        long id = groupService.create(GroupDto.builder()
                .name(name)
                .faculty(facultyId)
                .build()).getId();

        Function<String, ResultMatcher[]> matchers = prefix -> new ResultMatcher[]{
                jsonPath(prefix + ".id").value(id),
                jsonPath(prefix + ".name").value(name),
                jsonPath(prefix + ".faculty").value(facultyId)
        };

        return RequestContext.<Long>builder()
                .request(id)
                .matchersSupplier(matchers)
                .build();
    }

    RequestContext<Long> createGetGroupByIdContext() {
        long facultyId = ef.createFaculty();
        return createGetGroupByIdContextWithCourse(facultyId);
    }

    @Test
    void getGroupById__authenticated__valid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                var context = createGetGroupByIdContext();

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
                var context = createGetGroupByIdContextWithCourse(facultyId);

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
        var context = createGetGroupByIdContext();

        long id = context.getRequest();

        mvc.perform(get("/groups/{id}", id))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getGroupById__notExists__invalid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                var context = createGetGroupByIdContext();

                long id = context.getRequest();

                securePerform(get("/groups/{id}", id + 1000))
                        .andExpect(status().isNotFound());
            }
        };
    }

    // ================================================================================================================

    RequestContext<Long> createGetGroupByFacultyContext(long facultyId, String name) {
        long id = groupService.create(GroupDto.builder()
                .faculty(facultyId)
                .name(name)
                .build()).getId();

        Function<String, ResultMatcher[]> matchers = prefix -> new ResultMatcher[]{
                jsonPath(prefix + ".id").value(id),
                jsonPath(prefix + ".name").value(name),
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
                long facultyId1 = ef.createFaculty();
                long facultyId2 = ef.createFaculty();

                String name1 = "A" + faker.lorem().sentence(1);
                String name2 = "B" + faker.lorem().sentence(1);

                var context1 = createGetGroupByFacultyContext(facultyId1, name1);
                var context2 = createGetGroupByFacultyContext(facultyId1, name2);
                createGetGroupByFacultyContext(facultyId2, name1);

                securePerform(get("/groups/faculty/{id}", facultyId1))
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
                long facultyId1 = ef.createFaculty();
                long facultyId2 = ef.createFaculty();

                String name1 = "A" + faker.lorem().sentence(1);
                String name2 = "B" + faker.lorem().sentence(1);

                var context1 = createGetGroupByFacultyContext(facultyId1, name1);
                var context2 = createGetGroupByFacultyContext(facultyId1, name2);
                createGetGroupByFacultyContext(facultyId2, name1);

                securePerform(get("/groups/faculty/{id}", facultyId1))
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
                String name = faker.lorem().sentence(1);
                long facultyId = ef.createFaculty();

                var context = createGetGroupByFacultyContext(facultyId, name);

                securePerform(get("/groups/faculty/{id}", facultyId + 1000))
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

    RequestContext<ObjectNode> getCreateGroupContext(long facultyId, String name) {
        ObjectNode request = objectMapper.createObjectNode()
                .put("faculty", facultyId)
                .put("name", name);

        Function<String, ResultMatcher[]> matchers = prefix -> new ResultMatcher[]{
                jsonPath(prefix + ".id").isNumber(),
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
                long facultyId = ef.createFaculty();
                String name = faker.lorem().sentence(1);

                var context = getCreateGroupContext(facultyId, name);

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
                long facultyId = ef.createFaculty();
                String name = faker.lorem().sentence(1);

                var context = getCreateGroupContext(facultyId, name);

                ObjectNode request = context.getRequest();

                securePerform(post("/groups/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isForbidden());
            }
        };
    }

    @Test
    void createGroup__nameAndFacultyNotUnique__invalid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                long facultyId = ef.createFaculty();
                String name = faker.lorem().sentence(1);

                var context = getCreateGroupContext(facultyId, name);

                ef.createGroup(ef.bag()
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
        long facultyId = ef.createFaculty();
        String name = faker.lorem().sentence(1);

        var context = getCreateGroupContext(facultyId, name);

        ObjectNode request = context.getRequest();

        mvc.perform(post("/groups/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .contentType(request.toString()))
                .andExpect(status().isUnauthorized());
    }

    // ================================================================================================================

    RequestContext<ObjectNode> createPutGroupContext(long facultyId, String name) {

        ObjectNode request = objectMapper.createObjectNode()
                .put("name", name)
                .put("faculty", facultyId);

        Function<String, ResultMatcher[]> matchers = prefix -> new ResultMatcher[]{
                jsonPath(prefix + ".id").isNumber(),
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
                long facultyId = ef.createFaculty();
                String name1 = faker.lorem().sentence(1);
                String name2 = faker.lorem().sentence(1);

                long id = ef.createGroup(ef.bag().withFacultyId(facultyId)
                        .withDto(GroupDto.builder()
                                .name(name1)
                                .build()
                        ));

                var context = createPutGroupContext(facultyId, name2);
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
                long facultyId = ef.createFaculty();
                String name1 = faker.lorem().sentence(1);
                String name2 = faker.lorem().sentence(1);

                long id = ef.createGroup(ef.bag().withFacultyId(facultyId)
                        .withDto(GroupDto.builder()
                                .name(name1)
                                .build()
                        ));

                var context = createPutGroupContext(facultyId, name2);
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
                long facultyId = ef.createFaculty();
                String name1 = faker.lorem().sentence(1);
                String name2 = faker.lorem().sentence(1);

                long id = ef.createGroup(ef.bag().withFacultyId(facultyId)
                        .withDto(GroupDto.builder()
                                .name(name1)
                                .build()
                        ));

                var context = createPutGroupContext(facultyId, name2);
                ObjectNode request = context.getRequest();

                securePerform(put("/groups/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isForbidden());
            }
        };
    }

    @Test
    void putGroup__facultyChanged__invalid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                String name1 = faker.lorem().sentence(1);
                String name2 = faker.lorem().sentence(1);
                long facultyId1 = ef.createFaculty();
                long facultyId2 = ef.createFaculty();

                long id = ef.createGroup(ef.bag().withFacultyId(facultyId1)
                        .withDto(GroupDto.builder()
                                .name(name1)
                                .build()
                        ));

                var context = createPutGroupContext(facultyId2, name2);
                ObjectNode request = context.getRequest();

                securePerform(put("/groups/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isBadRequest());
            }
        };
    }

    @Test
    void putGroup__newNameNotUnique__invalid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                String name1 = faker.lorem().sentence(1);
                String name2 = faker.lorem().sentence(1);
                long facultyId = ef.createFaculty();

                ef.createGroup(ef.bag().withFacultyId(facultyId)
                        .withDto(GroupDto.builder()
                                .name(name2)
                                .build()));

                long id = ef.createGroup(ef.bag().withFacultyId(facultyId)
                        .withDto(GroupDto.builder()
                                .name(name1)
                                .build()
                        ));

                var context = createPutGroupContext(facultyId, name2);
                ObjectNode request = context.getRequest();

                securePerform(put("/groups/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isConflict());
            }
        };
    }

    @Test
    void putGroup__notChanged__valid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                long facultyId = ef.createFaculty();
                String name = faker.lorem().sentence(1);

                long id = ef.createGroup(ef.bag().withFacultyId(facultyId)
                        .withDto(GroupDto.builder()
                                .name(name)
                                .build()
                        ));

                var context = createPutGroupContext(facultyId, name);
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
    void putGroup__notExists__invalid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                long facultyId = ef.createFaculty();
                String name = faker.lorem().sentence(1);
                long id = ef.createGroup(ef.bag().withFacultyId(facultyId)
                        .withDto(GroupDto.builder()
                                .name(name)
                                .build()));

                RequestContext<ObjectNode> context = createPutGroupContext(facultyId, name);
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
        long facultyId = ef.createFaculty();
        String name = faker.lorem().sentence(1);
        long id = ef.createGroup(ef.bag().withFacultyId(facultyId)
                .withDto(GroupDto.builder()
                        .name(name)
                        .build()));

        RequestContext<ObjectNode> context = createPutGroupContext(facultyId, name);
        ObjectNode request = context.getRequest();

        mvc.perform(put("/groups/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                .andExpect(status().isUnauthorized());
    }

    // ================================================================================================================

    RequestContext<ObjectNode> createPatchGroupContext(long facultyId, String name) {
        ObjectNode request = objectMapper.createObjectNode()
                .put("faculty", facultyId)
                .put("name", name);

        Function<String, ResultMatcher[]> matchers = prefix -> new ResultMatcher[]{
                jsonPath(prefix + ".id").isNumber(),
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
                long facultyId = ef.createFaculty();
                String name1 = faker.lorem().sentence(1);
                String name2 = faker.lorem().sentence(1);

                long id = ef.createGroup(ef.bag().withFacultyId(facultyId)
                        .withDto(GroupDto.builder()
                                .name(name1)
                                .build()
                        ));

                var context = createPatchGroupContext(facultyId, name2);
                ObjectNode request = context.getRequest();

                securePerform(patch("/groups/{id}", id)
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
                long facultyId = ef.createFaculty();
                String name1 = faker.lorem().sentence(1);
                String name2 = faker.lorem().sentence(1);

                long id = ef.createGroup(ef.bag().withFacultyId(facultyId)
                        .withDto(GroupDto.builder()
                                .name(name1)
                                .build()
                        ));

                var context = createPatchGroupContext(facultyId, name2);
                ObjectNode request = context.getRequest();

                securePerform(patch("/groups/{id}", id)
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
                long facultyId = ef.createFaculty();
                String name1 = faker.lorem().sentence(1);
                String name2 = faker.lorem().sentence(1);

                long id = ef.createGroup(ef.bag().withFacultyId(facultyId)
                        .withDto(GroupDto.builder()
                                .name(name1)
                                .build()
                        ));

                var context = createPatchGroupContext(facultyId, name2);
                ObjectNode request = context.getRequest();

                securePerform(patch("/groups/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isForbidden());
            }
        };
    }

    @Test
    void patchGroup__facultyChanged__invalid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                String name1 = faker.lorem().sentence(1);
                String name2 = faker.lorem().sentence(1);
                long facultyId1 = ef.createFaculty();
                long facultyId2 = ef.createFaculty();

                long id = ef.createGroup(ef.bag().withFacultyId(facultyId1)
                        .withDto(GroupDto.builder()
                                .faculty(facultyId1)
                                .name(name1)
                                .build()
                        ));

                var context = createPatchGroupContext(facultyId2, name2);
                ObjectNode request = context.getRequest();

                securePerform(patch("/groups/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isBadRequest());
            }
        };
    }

    @Test
    void patchGroup__newNameNotUnique__invalid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                String name1 = faker.lorem().sentence(1);
                String name2 = faker.lorem().sentence(1);
                long facultyId = ef.createFaculty();

                ef.createGroup(ef.bag().withFacultyId(facultyId)
                        .withDto(GroupDto.builder()
                                .name(name2)
                                .build()));

                long id = ef.createGroup(ef.bag().withFacultyId(facultyId)
                        .withDto(GroupDto.builder()
                                .name(name1)
                                .build()
                        ));

                var context = createPatchGroupContext(facultyId, name2);
                ObjectNode request = context.getRequest();

                securePerform(patch("/groups/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isConflict());
            }
        };
    }

    @Test
    void patchGroup__notChanged__valid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                long facultyId = ef.createFaculty();
                String name = faker.lorem().sentence(1);

                long id = ef.createGroup(ef.bag().withFacultyId(facultyId)
                        .withDto(GroupDto.builder()
                                .name(name)
                                .build()
                        ));

                var context = createPatchGroupContext(facultyId, name);
                ObjectNode request = context.getRequest();

                securePerform(patch("/groups/{id}", id)
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
    void patchGroup__notExists__invalid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                long facultyId = ef.createFaculty();
                String name = faker.lorem().sentence(1);
                long id = ef.createGroup(ef.bag().withFacultyId(facultyId)
                        .withDto(GroupDto.builder()
                                .name(name)
                                .build()));

                RequestContext<ObjectNode> context = createPatchGroupContext(facultyId, name);
                ObjectNode request = context.getRequest();

                securePerform(patch("/groups/{id}", id + 1000)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpectAll(status().isNotFound());
            }
        };
    }

    @Test
    void patchGroup__notAuthenticated__invalid() throws Exception {
        long facultyId = ef.createFaculty();
        String name = faker.lorem().sentence(1);
        long id = ef.createGroup(ef.bag().withFacultyId(facultyId)
                .withDto(GroupDto.builder()
                        .name(name)
                        .build()));

        RequestContext<ObjectNode> context = createPatchGroupContext(facultyId, name);
        ObjectNode request = context.getRequest();

        mvc.perform(patch("/groups/{id}", id)
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
    }

}
