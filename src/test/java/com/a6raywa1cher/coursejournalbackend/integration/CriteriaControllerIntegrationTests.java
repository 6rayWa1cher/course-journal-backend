package com.a6raywa1cher.coursejournalbackend.integration;


import com.a6raywa1cher.coursejournalbackend.dto.CriteriaDto;
import com.a6raywa1cher.coursejournalbackend.service.CriteriaService;
import com.a6raywa1cher.coursejournalbackend.service.TaskService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.ResultMatcher;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
public class CriteriaControllerIntegrationTests extends AbstractIntegrationTests {
    @Autowired
    CriteriaService criteriaService;

    @Autowired
    TaskService taskService;

    @Test
    void getCriteriaById__self__valid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                String name = faker.lorem().sentence();
                long id = criteriaService.create(CriteriaDto.builder()
                        .name(name)
                        .criteriaPercent(60)
                        .task(ef.createTask(getIdAsLong()))
                        .build()).getId();

                securePerform(get("/criteria/{id}", id))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.name").value(name));
            }
        };
    }

    @Test
    void getCriteriaById__otherAsAdmin__valid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                String name = faker.lorem().sentence();
                long id = criteriaService.create(CriteriaDto.builder()
                        .name(name)
                        .criteriaPercent(60)
                        .task(ef.createTask())
                        .build()).getId();

                securePerform(get("/criteria/{id}", id))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.name").value(name));
            }
        };
    }

    @Test
    void getCriteriaById__otherAsTeacher__invalid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                String name = faker.lorem().sentence();
                long id = criteriaService.create(CriteriaDto.builder()
                        .name(name)
                        .criteriaPercent(60)
                        .task(ef.createTask())
                        .build()).getId();

                securePerform(get("/criteria/{id}", id))
                        .andExpect(status().isForbidden());
            }
        };
    }

    @Test
    void getCriteriaById__notAuthenticated__invalid() throws Exception {
        String name = faker.lorem().sentence();
        long id = criteriaService.create(CriteriaDto.builder()
                .name(name)
                .criteriaPercent(60)
                .task(ef.createTask())
                .build()).getId();

        mvc.perform(get("/criteria/{id}", id))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getCriteriaById__notExists__invalid() {
        String name = faker.lorem().sentence();
        long id = criteriaService.create(CriteriaDto.builder()
                .name(name)
                .criteriaPercent(60)
                .task(ef.createTask())
                .build()).getId();

        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                securePerform(get("/criteria/{id}", id + 1000))
                        .andExpect(status().isNotFound());
            }
        };
    }

    // ================================================================================================================

    @Test
    void getCriteriaByTask__self__valid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                String sentence1 = faker.lorem().sentence();
                String sentence2 = faker.lorem().sentence();

                long taskId1 = ef.createTask(getIdAsLong());
                long taskId2 = ef.createTask(ef.createUser());

                criteriaService.create(CriteriaDto.builder()
                        .name(sentence2)
                        .criteriaPercent(75)
                        .task(taskId1)
                        .build());

                criteriaService.create(CriteriaDto.builder()
                        .name(sentence1)
                        .criteriaPercent(60)
                        .task(taskId1)
                        .build());

                criteriaService.create(CriteriaDto.builder()
                        .name(sentence1)
                        .criteriaPercent(60)
                        .task(taskId2)
                        .build());

                securePerform(get("/criteria/task/{id}", taskId1))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$[0].name").value(sentence1))
                        .andExpect(jsonPath("$[1].name").value(sentence2));
            }
        };
    }

    @Test
    void getCriteriaByTask__otherAsAdmin__valid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                String sentence1 = faker.lorem().sentence();
                String sentence2 = faker.lorem().sentence();

                long taskId1 = ef.createTask(ef.createUser());
                long taskId2 = ef.createTask(ef.createUser());

                criteriaService.create(CriteriaDto.builder()
                        .name(sentence2)
                        .criteriaPercent(60)
                        .task(taskId1)
                        .build());

                criteriaService.create(CriteriaDto.builder()
                        .name(sentence1)
                        .criteriaPercent(75)
                        .task(taskId1)
                        .build());

                criteriaService.create(CriteriaDto.builder()
                        .name(sentence1)
                        .criteriaPercent(60)
                        .task(taskId2)
                        .build());

                securePerform(get("/criteria/task/{id}", taskId1))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$[0].name").value(sentence2))
                        .andExpect(jsonPath("$[1].name").value(sentence1));
            }
        };
    }

    @Test
    void getCriteriaByTask__otherAsTeacher__invalid() {
        long id = ef.createTask(ef.createUser());

        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                securePerform(get("/criteria/task/{id}", id).queryParam("sort", "id,desc"))
                        .andExpect(status().isForbidden());
            }
        };
    }

    @Test
    void getCriteriaByTask__notAuthenticated__invalid() throws Exception {
        long id = ef.createTask(ef.createUser());
        mvc.perform(get("/criteria/task/{id}", id)).andExpect(status().isUnauthorized());
    }

    // ================================================================================================================

    @Test
    void createCriteria__self__valid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                String name = faker.lorem().sentence();
                long taskId = ef.createTask(getIdAsLong());
                int criteriaPercent = 60;

                securePerform(post("/criteria/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.createObjectNode()
                                .put("name", name)
                                .put("criteriaPercent", criteriaPercent)
                                .put("task", taskId)
                                .toString()))
                        .andExpect(status().isCreated())
                        .andExpectAll(
                                jsonPath("$.id").isNumber(),
                                jsonPath("$.name").value(name),
                                jsonPath("$.criteriaPercent").value(criteriaPercent),
                                jsonPath("$.task").value(taskId)
                        );

                securePerform(get("/criteria/task/{id}", taskId))
                        .andExpect(status().isOk())
                        .andExpect(content().string(containsString(name)));
            }
        };
    }

    @Test
    void createCriteria__otherAsAdmin__valid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                String name = faker.lorem().sentence();
                long taskId = ef.createTask(ef.createUser());
                int criteriaPercent = 60;

                securePerform(post("/criteria/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.createObjectNode()
                                .put("name", name)
                                .put("criteriaPercent", criteriaPercent)
                                .put("task", taskId)
                                .toString()))
                        .andExpect(status().isCreated())
                        .andExpectAll(
                                jsonPath("$.id").isNumber(),
                                jsonPath("$.name").value(name),
                                jsonPath("$.criteriaPercent").value(criteriaPercent),
                                jsonPath("$.task").value(taskId)
                        );

                securePerform(get("/criteria/task/{id}", taskId))
                        .andExpect(status().isOk())
                        .andExpect(content().string(containsString(name)));
            }
        };
    }

    @Test
    void createCriteria__otherAsTeacher__invalid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                String name = faker.lorem().sentence();
                long taskId = ef.createTask(ef.createUser());
                int criteriaPercent = 60;

                securePerform(post("/criteria/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.createObjectNode()
                                .put("name", name)
                                .put("criteriaPercent", criteriaPercent)
                                .put("task", taskId)
                                .toString()))
                        .andExpect(status().isForbidden());
            }
        };
    }

    @Test
    void createCriteria__conflictingName__invalid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                String name = faker.lorem().sentence();
                long taskId = ef.createTask(getIdAsLong());

                criteriaService.create(CriteriaDto.builder()
                        .name(name)
                        .criteriaPercent(60)
                        .task(taskId)
                        .build());

                securePerform(post("/criteria/", taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.createObjectNode()
                                .put("name", name)
                                .put("criteriaPercent", 45)
                                .put("task", taskId)
                                .toString()))
                        .andExpect(status().isConflict());
            }
        };
    }

    @Test
    void createCriteria__notAuthenticated__invalid() throws Exception {
        mvc.perform(post("/criteria/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.createObjectNode()
                                .put("name", faker.lorem().sentence())
                                .put("criteriaPercent", 45)
                                .put("task", ef.createTask(ef.createUser()))
                                .toString()))
                .andExpect(status().isUnauthorized());
    }

    // ================================================================================================================

    @Test
    void putCriteria__self__valid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                String name = faker.lorem().sentence();
                int criteriaPercent = 45;
                long taskId = ef.createTask(getIdAsLong());

                String prevName = faker.lorem().sentence();
                int prevCriteriaPercent = 60;

                long criteriaId = criteriaService.create(CriteriaDto.builder()
                        .name(prevName)
                        .criteriaPercent(prevCriteriaPercent)
                        .task(taskId)
                        .build()).getId();

                ResultMatcher[] resultMatchers = {
                        jsonPath("$.id").value(criteriaId),
                        jsonPath("$.name").value(name),
                        jsonPath("$.criteriaPercent").value(criteriaPercent),
                        jsonPath("$.task").value(taskId),
                };

                securePerform(put("/criteria/{id}", criteriaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.createObjectNode()
                                .put("task", taskId)
                                .put("name", name)
                                .put("criteriaPercent", criteriaPercent)
                                .toString()))
                        .andExpect(status().isOk())
                        .andExpectAll(resultMatchers);


                securePerform(get("/criteria/{id}", criteriaId))
                        .andExpect(status().isOk())
                        .andExpectAll(resultMatchers);
            }
        };
    }

    @Test
    void putCriteria__otherAsAdmin__valid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                String name = faker.lorem().sentence();
                int criteriaPercent = 45;
                long taskId = ef.createTask();

                String prevName = faker.lorem().sentence();
                int prevCriteriaPercent = 60;

                long criteriaId = criteriaService.create(CriteriaDto.builder()
                        .name(prevName)
                        .criteriaPercent(prevCriteriaPercent)
                        .task(taskId)
                        .build()).getId();

                ResultMatcher[] resultMatchers = {
                        jsonPath("$.id").value(criteriaId),
                        jsonPath("$.name").value(name),
                        jsonPath("$.criteriaPercent").value(criteriaPercent),
                        jsonPath("$.task").value(taskId),
                };

                securePerform(put("/criteria/{id}", criteriaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.createObjectNode()
                                .put("task", taskId)
                                .put("name", name)
                                .put("criteriaPercent", criteriaPercent)
                                .toString()))
                        .andExpect(status().isOk())
                        .andExpectAll(resultMatchers);


                securePerform(get("/criteria/{id}", criteriaId))
                        .andExpect(status().isOk())
                        .andExpectAll(resultMatchers);
            }
        };
    }

    @Test
    void putCriteria__otherAsTeacher__invalid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                String name = faker.lorem().sentence();
                int criteriaPercent = 45;
                long taskId = ef.createTask();

                String prevName = faker.lorem().sentence();
                int prevCriteriaPercent = 60;

                long criteriaId = criteriaService.create(CriteriaDto.builder()
                        .name(prevName)
                        .criteriaPercent(prevCriteriaPercent)
                        .task(taskId)
                        .build()).getId();

                securePerform(put("/criteria/{id}", criteriaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.createObjectNode()
                                .put("task", taskId)
                                .put("name", name)
                                .put("criteriaPercent", criteriaPercent)
                                .toString()))
                        .andExpect(status().isForbidden());
            }
        };
    }

    @Test
    void putCriteria__taskChange__invalid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                String name = faker.lorem().sentence();
                int criteriaPercent = 45;
                long taskId = ef.createTask(getIdAsLong());

                String prevName = faker.lorem().sentence();
                int prevCriteriaPercent = 60;
                long prevTaskId = ef.createTask(getIdAsLong());

                long criteriaId = criteriaService.create(CriteriaDto.builder()
                        .name(prevName)
                        .criteriaPercent(prevCriteriaPercent)
                        .task(prevTaskId)
                        .build()).getId();

                securePerform(put("/criteria/{id}", criteriaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.createObjectNode()
                                .put("task", taskId)
                                .put("name", name)
                                .put("criteriaPercent", criteriaPercent)
                                .toString()))
                        .andExpect(status().isBadRequest());
            }
        };
    }

    @Test
    void putCriteria__conflictingName__invalid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                String name = faker.lorem().sentence();
                int criteriaPercent = 45;
                long taskId = ef.createTask(getIdAsLong());

                String prevName = faker.lorem().sentence();

                long criteriaId = criteriaService.create(CriteriaDto.builder()
                        .name(prevName)
                        .criteriaPercent(criteriaPercent)
                        .task(taskId)
                        .build()).getId();

                criteriaService.create(CriteriaDto.builder()
                        .name(name)
                        .criteriaPercent(criteriaPercent)
                        .task(taskId)
                        .build());

                securePerform(put("/criteria/{id}", criteriaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.createObjectNode()
                                .put("task", taskId)
                                .put("name", name)
                                .put("criteriaPercent", criteriaPercent)
                                .toString()))
                        .andExpect(status().isConflict());
            }
        };
    }

    @Test
    void putCriteria__notExists__invalid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                String name = faker.lorem().sentence();
                int criteriaPercent = 45;
                long taskId = ef.createTask(getIdAsLong());

                String prevName = faker.lorem().sentence();
                int prevCriteriaPercent = 60;

                long criteriaId = criteriaService.create(CriteriaDto.builder()
                        .name(prevName)
                        .criteriaPercent(prevCriteriaPercent)
                        .task(taskId)
                        .build()).getId();

                securePerform(put("/criteria/{id}", criteriaId + 1000)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.createObjectNode()
                                .put("task", taskId)
                                .put("name", name)
                                .put("criteriaPercent", criteriaPercent)
                                .toString()))
                        .andExpect(status().isNotFound());
            }
        };
    }

    @Test
    void putCriteria__notAuthenticated__invalid() throws Exception {
        String name = faker.lorem().sentence();
        int criteriaPercent = 45;
        long taskId = ef.createTask();

        String prevName = faker.lorem().sentence();
        int prevCriteriaPercent = 60;

        long criteriaId = criteriaService.create(CriteriaDto.builder()
                .name(prevName)
                .criteriaPercent(prevCriteriaPercent)
                .task(taskId)
                .build()).getId();

        mvc.perform(put("/criteria/{id}", criteriaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.createObjectNode()
                                .put("task", taskId)
                                .put("name", name)
                                .put("criteriaPercent", criteriaPercent)
                                .toString()))
                .andExpect(status().isUnauthorized());
    }

    // ================================================================================================================

    @Test
    void patchCriteria__self__valid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                String name = faker.lorem().sentence();
                long taskId = ef.createTask(getIdAsLong());
                int criteriaPercent = 60;

                String prevName = faker.lorem().sentence();

                long criteriaId = criteriaService.create(CriteriaDto.builder()
                        .name(prevName)
                        .criteriaPercent(criteriaPercent)
                        .task(taskId)
                        .build()).getId();

                ResultMatcher[] resultMatchers = {
                        jsonPath("$.id").value(criteriaId),
                        jsonPath("$.name").value(name),
                        jsonPath("$.criteriaPercent").value(criteriaPercent),
                        jsonPath("$.task").value(taskId),
                };

                securePerform(patch("/criteria/{id}", criteriaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.createObjectNode()
                                .put("name", name)
                                .toString()))
                        .andExpect(status().isOk())
                        .andExpectAll(resultMatchers);


                securePerform(get("/criteria/{id}", criteriaId))
                        .andExpect(status().isOk())
                        .andExpectAll(resultMatchers);
            }
        };
    }

    @Test
    void patchCriteria__otherAsAdmin__valid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                String name = faker.lorem().sentence();
                long taskId = ef.createTask();
                int criteriaPercent = 60;

                String prevName = faker.lorem().sentence();

                long criteriaId = criteriaService.create(CriteriaDto.builder()
                        .name(prevName)
                        .criteriaPercent(criteriaPercent)
                        .task(taskId)
                        .build()).getId();

                ResultMatcher[] resultMatchers = {
                        jsonPath("$.id").value(criteriaId),
                        jsonPath("$.name").value(name),
                        jsonPath("$.criteriaPercent").value(criteriaPercent),
                        jsonPath("$.task").value(taskId),
                };

                securePerform(patch("/criteria/{id}", criteriaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.createObjectNode()
                                .put("name", name)
                                .toString()))
                        .andExpect(status().isOk())
                        .andExpectAll(resultMatchers);


                securePerform(get("/criteria/{id}", criteriaId))
                        .andExpect(status().isOk())
                        .andExpectAll(resultMatchers);
            }
        };
    }

    @Test
    void patchCriteria__otherAsTeacher__invalid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                String name = faker.lorem().sentence();
                long taskId = ef.createTask();
                int criteriaPercent = 60;

                String prevName = faker.lorem().sentence();

                long criteriaId = criteriaService.create(CriteriaDto.builder()
                        .name(prevName)
                        .criteriaPercent(criteriaPercent)
                        .task(taskId)
                        .build()).getId();

                securePerform(patch("/criteria/{id}", criteriaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.createObjectNode()
                                .put("name", name)
                                .toString()))
                        .andExpect(status().isForbidden());
            }
        };
    }

    @Test
    void patchCriteria__courseChange__invalid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                String name = faker.lorem().sentence();
                int criteriaPercent = 45;
                long taskId = ef.createTask(getIdAsLong());

                long prevTaskId = ef.createTask(getIdAsLong());

                long criteriaId = criteriaService.create(CriteriaDto.builder()
                        .name(name)
                        .criteriaPercent(criteriaPercent)
                        .task(prevTaskId)
                        .build()).getId();

                securePerform(patch("/criteria/{id}", criteriaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.createObjectNode()
                                .put("task", taskId)
                                .toString()))
                        .andExpect(status().isBadRequest());
            }
        };
    }

    @Test
    void patchCriteria__conflictingName__invalid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                String name = faker.lorem().sentence();
                int criteriaPercent = 45;
                long taskId = ef.createTask(getIdAsLong());

                String prevName = faker.lorem().sentence();

                long criteriaId = criteriaService.create(CriteriaDto.builder()
                        .name(prevName)
                        .criteriaPercent(criteriaPercent)
                        .task(taskId)
                        .build()).getId();

                criteriaService.create(CriteriaDto.builder()
                        .name(name)
                        .criteriaPercent(criteriaPercent)
                        .task(taskId)
                        .build());

                securePerform(patch("/criteria/{id}", criteriaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.createObjectNode()
                                .put("name", name)
                                .toString()))
                        .andExpect(status().isConflict());
            }
        };
    }

    @Test
    void patchCriteria__notExists__invalid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                String name = faker.lorem().sentence();
                long taskId = ef.createTask(getIdAsLong());
                int criteriaPercent = 60;

                String prevName = faker.lorem().sentence();

                long criteriaId = criteriaService.create(CriteriaDto.builder()
                        .name(prevName)
                        .criteriaPercent(criteriaPercent)
                        .task(taskId)
                        .build()).getId();

                securePerform(patch("/criteria/{id}", criteriaId + 1000)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.createObjectNode()
                                .put("name", name)
                                .toString()))
                        .andExpect(status().isNotFound());
            }
        };
    }

    @Test
    void patchCriteria__notAuthenticated__invalid() throws Exception {
        String name = faker.lorem().sentence();
        int criteriaPercent = 60;
        long taskId = ef.createTask();

        String prevName = faker.lorem().sentence();

        long criteriaId = criteriaService.create(CriteriaDto.builder()
                .name(prevName)
                .criteriaPercent(criteriaPercent)
                .task(taskId)
                .build()).getId();

        mvc.perform(patch("/criteria/{id}", criteriaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.createObjectNode()
                                .put("name", name)
                                .toString()
                        ))
                .andExpect(status().isUnauthorized());
    }

    // ================================================================================================================

    @Test
    void deleteCriteria__self__valid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                String name = faker.lorem().sentence();
                long taskId = ef.createTask(getIdAsLong());
                int criteriaPercent = 60;

                long criteriaId = criteriaService.create(CriteriaDto.builder()
                        .name(name)
                        .criteriaPercent(criteriaPercent)
                        .task(taskId)
                        .build()).getId();

                securePerform(delete("/criteria/{id}", criteriaId))
                        .andExpect(status().isOk());

                securePerform(get("/criteria/{id}", criteriaId))
                        .andExpect(status().isNotFound());
            }
        };
    }

    @Test
    void deleteCriteria__otherAsAdmin__valid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                String name = faker.lorem().sentence();
                long taskId = ef.createTask();
                int criteriaPercent = 60;

                long criteriaId = criteriaService.create(CriteriaDto.builder()
                        .name(name)
                        .criteriaPercent(criteriaPercent)
                        .task(taskId)
                        .build()).getId();

                securePerform(delete("/criteria/{id}", criteriaId))
                        .andExpect(status().isOk());

                securePerform(get("/criteria/{id}", criteriaId))
                        .andExpect(status().isNotFound());
            }
        };
    }

    @Test
    void deleteCriteria__otherAsTeacher__invalid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                String name = faker.lorem().sentence();
                long taskId = ef.createTask();
                int criteriaPercent = 60;

                long criteriaId = criteriaService.create(CriteriaDto.builder()
                        .name(name)
                        .criteriaPercent(criteriaPercent)
                        .task(taskId)
                        .build()).getId();

                securePerform(delete("/criteria/{id}", criteriaId))
                        .andExpect(status().isForbidden());
            }
        };
    }

    @Test
    void deleteCriteria__notFound__invalid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                String name = faker.lorem().sentence();
                long taskId = ef.createTask();
                int criteriaPercent = 60;

                long criteriaId = criteriaService.create(CriteriaDto.builder()
                        .name(name)
                        .criteriaPercent(criteriaPercent)
                        .task(taskId)
                        .build()).getId();

                securePerform(delete("/criteria/{id}", criteriaId + 1000))
                        .andExpect(status().isNotFound());
            }
        };
    }

    @Test
    void deleteCriteria__notAuthenticated__invalid() throws Exception {
        String name = faker.lorem().sentence();
        long taskId = ef.createTask();
        int criteriaPercent = 60;

        long criteriaId = criteriaService.create(CriteriaDto.builder()
                .name(name)
                .criteriaPercent(criteriaPercent)
                .task(taskId)
                .build()).getId();

        mvc.perform(delete("/criteria/{id}", criteriaId))
                .andExpect(status().isUnauthorized());
    }
}
