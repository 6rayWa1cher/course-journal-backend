package com.a6raywa1cher.coursejournalbackend.integration;

import com.a6raywa1cher.coursejournalbackend.RequestContext;
import com.a6raywa1cher.coursejournalbackend.dto.FacultyDto;
import com.a6raywa1cher.coursejournalbackend.service.FacultyService;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
public class FacultyControllerIntegrationTests extends AbstractIntegrationTests {

    @Autowired
    FacultyService facultyService;

    RequestContext<Long> createGetFacultyByIdContext() {
        String name = faker.lorem().sentence(2);

        long id = facultyService.create(FacultyDto.builder()
                .name(name)
                .build()).getId();

        Function<String, ResultMatcher[]> matchers = prefix -> new ResultMatcher[]{
                jsonPath(prefix + ".id").value(id),
                jsonPath(prefix + ".name").value(name)
        };

        return RequestContext.<Long>builder()
                .request(id)
                .matchersSupplier(matchers)
                .data(Map.of("facultyId", Long.toString(id)))
                .build();
    }

    @Test
    void getFacultyById__everyone__valid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                var context = createGetFacultyByIdContext();

                long id = context.getRequest();
                ResultMatcher[] matchers = context.getMatchers();

                securePerform(get("/faculties/{id}", id))
                        .andExpect(status().isOk())
                        .andExpectAll(matchers);
            }
        };
    }

    @Test
    void getFacultyById__notFound__invalid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                var context = createGetFacultyByIdContext();

                long id = context.getRequest();
                ResultMatcher[] matchers = context.getMatchers();

                securePerform(get("/faculties/{id}", id + 1000))
                        .andExpect(status().isNotFound());
            }
        };
    }

    @Test
    void getFacultyById__notAuthenticated__invalid() throws Exception {
        long id = ef.createFaculty();

        mvc.perform(get("/faculties/{id}", id))
                .andExpect(status().isUnauthorized());
    }

    // ================================================================================================================

    RequestContext<ObjectNode> getCreateFacultyContext() {
        String name = faker.lorem().sentence(2);

        ObjectNode request = objectMapper.createObjectNode()
                .put("name", name);

        Function<String, ResultMatcher[]> matchers = prefix -> new ResultMatcher[]{
                jsonPath(prefix + ".id").isNumber(),
                jsonPath(prefix + ".name").value(name)
        };

        return new RequestContext<>(request, matchers);
    }

    RequestContext<ObjectNode> getCreateFacultyContextWithName(String name) {
        ObjectNode request = objectMapper.createObjectNode()
                .put("name", name);

        Function<String, ResultMatcher[]> matchers = prefix -> new ResultMatcher[]{
                jsonPath(prefix + ".id").isNumber(),
                jsonPath(prefix + ".name").value(name)
        };

        return new RequestContext<>(request, matchers);
    }

    @Test
    void createFaculty__admin__valid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                var context = getCreateFacultyContext();

                ObjectNode request = context.getRequest();

                MvcResult mvcResult = securePerform(post("/faculties/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isCreated())
                        .andExpectAll(context.getMatchers())
                        .andReturn();

                int id = JsonPath.read(mvcResult.getResponse().getContentAsString(), "$.id");

                securePerform(get("/faculties/{id}", id))
                        .andExpect(status().isOk())
                        .andExpectAll(context.getMatchers());
            }
        };
    }

    @Test
    void createFaculty__notAdmin__invalid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                var context = getCreateFacultyContext();

                ObjectNode request = context.getRequest();

                securePerform(post("/faculties/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isForbidden());
            }
        };
    }

    @Test
    void createFaculty__nameNotUnique__invalid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                String name = faker.lorem().sentence(2);

                var context = getCreateFacultyContextWithName(name);
                ef.createFaculty(ef.bag().withDto(
                        FacultyDto.builder()
                                .name(name)
                                .build()
                ));

                ObjectNode request = context.getRequest();

                securePerform(post("/faculties/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isConflict());
            }
        };
    }

    @Test
    void createFaculty__notAuthenticated__invalid() throws Exception {
        var context = getCreateFacultyContext();

        ObjectNode request = context.getRequest();

        mvc.perform(post("/faculties/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request.toString()))
                .andExpect(status().isUnauthorized());
    }

    // ================================================================================================================

    RequestContext<ObjectNode> createPutFacultyRequest(String name) {
        ObjectNode request = objectMapper.createObjectNode()
                .put("name", name);

        Function<String, ResultMatcher[]> matchers = prefix -> new ResultMatcher[]{
                jsonPath(prefix + ".id").isNumber(),
                jsonPath(prefix + ".name").value(name)
        };

        return new RequestContext<>(request, matchers);
    }

    @Test
    void putFaculty__admin__valid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                String name1 = faker.lorem().sentence(2);
                String name2 = name1 + faker.lorem().sentence(1);

                long id = ef.createFaculty(ef.bag().withDto(
                        FacultyDto.builder()
                                .name(name1)
                                .build()
                ));

                RequestContext<ObjectNode> context = createPutFacultyRequest(name2);
                ObjectNode request = context.getRequest();

                securePerform(put("/faculties/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isOk())
                        .andExpectAll(context.getMatchers());

                securePerform(get("/faculties/{id}", id))
                        .andExpect(status().isOk())
                        .andExpectAll(context.getMatchers());
            }
        };
    }

    @Test
    void putFaculty__notAdmin__invalid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                String name1 = faker.lorem().sentence(2);
                String name2 = name1 + faker.lorem().sentence(1);

                long id = ef.createFaculty(ef.bag().withDto(
                        FacultyDto.builder()
                                .name(name1)
                                .build()
                ));

                RequestContext<ObjectNode> context = createPutFacultyRequest(name2);
                ObjectNode request = context.getRequest();

                securePerform(put("/faculties/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isForbidden());
            }
        };
    }

    @Test
    void putFaculty__notExists__invalid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                String name1 = faker.lorem().sentence(2);
                String name2 = name1 + faker.lorem().sentence(1);

                long id = ef.createFaculty(ef.bag().withDto(
                        FacultyDto.builder()
                                .name(name1)
                                .build()
                ));

                RequestContext<ObjectNode> context = createPutFacultyRequest(name2);
                ObjectNode request = context.getRequest();

                securePerform(put("/faculties/{id}", id + 1000)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isNotFound());
            }
        };
    }

    @Test
    void putFaculty__notAuthenticated__invalid() throws Exception {
                String name1 = faker.lorem().sentence(2);
                String name2 = name1 + faker.lorem().sentence(1);

                long id = ef.createFaculty(ef.bag().withDto(
                        FacultyDto.builder()
                                .name(name1)
                                .build()
                ));

                RequestContext<ObjectNode> context = createPutFacultyRequest(name2);
                ObjectNode request = context.getRequest();

                mvc.perform(put("/faculties/{id}", id + 1000)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isUnauthorized());
            }

    // ================================================================================================================

    RequestContext<ObjectNode> createPatchFacultyRequest(String name) {
        ObjectNode request = objectMapper.createObjectNode()
                .put("name", name);

        Function<String, ResultMatcher[]> matchers = prefix -> new ResultMatcher[]{
                jsonPath(prefix + ".id").isNumber(),
                jsonPath(prefix + ".name").value(name)
        };

        return new RequestContext<>(request, matchers);
    }

    @Test
    void patchFaculty__admin__valid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                String name1 = faker.lorem().sentence(2);
                String name2 = name1 + faker.lorem().sentence(1);

                long id = ef.createFaculty(ef.bag().withDto(
                        FacultyDto.builder()
                                .name(name1)
                                .build()
                ));

                RequestContext<ObjectNode> context = createPatchFacultyRequest(name2);
                ObjectNode request = context.getRequest();

                securePerform(patch("/faculties/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isOk())
                        .andExpectAll(context.getMatchers());

                securePerform(get("/faculties/{id}", id))
                        .andExpect(status().isOk())
                        .andExpectAll(context.getMatchers());
            }
        };
    }

    @Test
    void patchFaculty__notAdmin__invalid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                String name1 = faker.lorem().sentence(2);
                String name2 = name1 + faker.lorem().sentence(1);

                long id = ef.createFaculty(ef.bag().withDto(
                        FacultyDto.builder()
                                .name(name1)
                                .build()
                ));

                RequestContext<ObjectNode> context = createPatchFacultyRequest(name2);
                ObjectNode request = context.getRequest();

                securePerform(patch("/faculties/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isForbidden());
            }
        };
    }

    @Test
    void patchFaculty__notExists__invalid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                String name1 = faker.lorem().sentence(2);
                String name2 = name1 + faker.lorem().sentence(1);

                long id = ef.createFaculty(ef.bag().withDto(
                        FacultyDto.builder()
                                .name(name1)
                                .build()
                ));

                RequestContext<ObjectNode> context = createPatchFacultyRequest(name2);
                ObjectNode request = context.getRequest();

                securePerform(patch("/faculties/{id}", id + 1000)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isNotFound());
            }
        };
    }

    @Test
    void patchFaculty__notAuthenticated__invalid() throws Exception {
        String name1 = faker.lorem().sentence(2);
        String name2 = name1 + faker.lorem().sentence(1);

        long id = ef.createFaculty(ef.bag().withDto(
                FacultyDto.builder()
                        .name(name1)
                        .build()
        ));

        RequestContext<ObjectNode> context = createPatchFacultyRequest(name2);
        ObjectNode request = context.getRequest();

        mvc.perform(patch("/faculties/{id}", id + 1000)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                .andExpect(status().isUnauthorized());
    }

    // ================================================================================================================

    @Test
    void deleteFaculty__admin__valid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                long id = ef.createFaculty();

                securePerform(delete("/faculties/{id}", id))
                        .andExpect(status().isOk());
            }
        };
    }

    @Test
    void deleteFaculty__notAdmin__invalid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                long id = ef.createFaculty();

                securePerform(delete("/faculties/{id}", id))
                        .andExpect(status().isForbidden());
            }
        };
    }

    @Test
    void deleteFaculty__notExists__invalid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                long id = ef.createFaculty();

                securePerform(delete("/faculties/{id}", id + 1000))
                        .andExpect(status().isNotFound());
            }
        };
    }

    @Test
    void deleteFaculty__notAuthenticated__invalid() throws Exception {
        long id = ef.createFaculty();

        mvc.perform(delete("/faculties/{id}", id + 1000))
                .andExpect(status().isUnauthorized());

    }
}
