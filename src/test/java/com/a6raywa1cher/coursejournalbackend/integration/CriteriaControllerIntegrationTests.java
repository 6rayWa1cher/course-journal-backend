package com.a6raywa1cher.coursejournalbackend.integration;


import com.a6raywa1cher.coursejournalbackend.ObjectRequestContext;
import com.a6raywa1cher.coursejournalbackend.RequestContext;
import com.a6raywa1cher.coursejournalbackend.dto.CriteriaDto;
import com.a6raywa1cher.coursejournalbackend.dto.SubmissionDto;
import com.a6raywa1cher.coursejournalbackend.integration.models.CriteriaInfo;
import com.a6raywa1cher.coursejournalbackend.integration.models.GetSetForTaskData;
import com.a6raywa1cher.coursejournalbackend.service.CriteriaService;
import com.a6raywa1cher.coursejournalbackend.service.TaskService;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.ResultMatcher;

import java.util.List;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.*;
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
                        .task(ef.createTask(getSelfEmployeeIdAsLong()))
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
    void getCriteriaById__withCourseToken__valid() {
        String name = faker.lorem().sentence();

        new WithCourseToken() {
            @Override
            void run() throws Exception {
                long id = criteriaService.create(CriteriaDto.builder()
                        .name(name)
                        .criteriaPercent(60)
                        .task(ef.createTask(ef.bag().withCourseId(getCourseId())))
                        .build()).getId();
                securePerform(get("/criteria/{id}", id))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.name").value(name));
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

                long taskId1 = ef.createTask(getSelfEmployeeIdAsLong());
                long taskId2 = ef.createTask(ef.createEmployee());

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
                        .andExpect(jsonPath("$[0].name").value(sentence2))
                        .andExpect(jsonPath("$[1].name").value(sentence1));
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

                long taskId1 = ef.createTask(ef.createEmployee());
                long taskId2 = ef.createTask(ef.createEmployee());

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
        long id = ef.createTask(ef.createEmployee());

        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                securePerform(get("/criteria/task/{id}", id).queryParam("sort", "id,desc"))
                        .andExpect(status().isForbidden());
            }
        };
    }

    @Test
    void getCriteriaByTask__withCourseToken__valid() {
        new WithCourseToken() {
            @Override
            void run() throws Exception {
                String sentence1 = faker.lorem().sentence();
                String sentence2 = faker.lorem().sentence();

                long taskId1 = ef.createTask(ef.bag().withCourseId(getCourseId()));
                long taskId2 = ef.createTask(ef.createEmployee());

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
    void getCriteriaByTask__notAuthenticated__invalid() throws Exception {
        long id = ef.createTask(ef.createEmployee());
        mvc.perform(get("/criteria/task/{id}", id)).andExpect(status().isUnauthorized());
    }

    // ================================================================================================================

    @Test
    void createCriteria__self__valid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                String name = faker.lorem().sentence();
                long taskId = ef.createTask(getSelfEmployeeIdAsLong());
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
                long taskId = ef.createTask(ef.createEmployee());
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
                long taskId = ef.createTask(ef.createEmployee());
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
    void createCriteria__withCourseToken__invalid() {
        new WithCourseToken() {
            @Override
            void run() throws Exception {
                String name = faker.lorem().sentence();
                long taskId = ef.createTask(ef.bag().withCourseId(getCourseId()));
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
                long taskId = ef.createTask(getSelfEmployeeIdAsLong());

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
                                .put("task", ef.createTask(ef.createEmployee()))
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
                long taskId = ef.createTask(getSelfEmployeeIdAsLong());

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
    void putCriteria__withCourseToken__invalid() {
        new WithCourseToken() {
            @Override
            void run() throws Exception {
                String name = faker.lorem().sentence();
                int criteriaPercent = 45;
                long taskId = ef.createTask(ef.bag().withCourseId(getCourseId()));

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
                long employeeId = ef.createEmployee();
                String name = faker.lorem().sentence();
                int criteriaPercent = 45;
                long taskId = ef.createTask(employeeId);

                String prevName = faker.lorem().sentence();
                int prevCriteriaPercent = 60;
                long prevTaskId = ef.createTask(employeeId);

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
                long taskId = ef.createTask(getSelfEmployeeIdAsLong());

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
                long taskId = ef.createTask();

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

    RequestContext<ObjectNode> getSetForTaskContext(List<CriteriaInfo> criteriaInfoList) {
        ObjectNode objectNode = objectMapper.createObjectNode();
        ArrayNode array = objectNode.putArray("criteria");
        for (CriteriaInfo info : criteriaInfoList) {
            array.add(objectMapper.createObjectNode()
                    .put("name", info.name())
                    .put("criteriaPercent", info.criteriaPercent()));
        }

        Function<String, ResultMatcher[]> matchers = prefix -> IntStream.range(0, criteriaInfoList.size())
                .boxed()
                .flatMap(i -> {
                    CriteriaInfo info = criteriaInfoList.get(i);
                    return Stream.of(
                            jsonPath(prefix + "[%d].name".formatted(i)).value(info.name()),
                            jsonPath(prefix + "[%d].criteriaPercent".formatted(i)).value(info.criteriaPercent())
                    );
                })
                .toArray(ResultMatcher[]::new);

        return RequestContext.<ObjectNode>builder()
                .request(objectNode)
                .matchersSupplier(matchers)
                .build();
    }

    ObjectRequestContext<ObjectNode, GetSetForTaskData> prepareGetSetForTaskContext(
            long employeeId, int dbSize, int requestSize
    ) {
        long taskId = ef.createTask(employeeId);
        List<Long> criteria = IntStream.range(0, dbSize)
                .boxed()
                .map(i -> ef.createCriteria(ef.bag().withTaskId(taskId)))
                .toList();
        long submissionId = ef.createSubmission(ef.bag().withTaskId(taskId)
                .withDto(SubmissionDto.builder().satisfiedCriteria(criteria).build()));

        var ctx = getSetForTaskContext(IntStream.range(0, requestSize)
                .boxed()
                .map(i -> new CriteriaInfo(faker.lorem().word(), faker.number().numberBetween(20, 80)))
                .toList());
        return new ObjectRequestContext<>(
                ctx.getRequest(),
                ctx.getMatchersSupplier(),
                new GetSetForTaskData(submissionId, taskId, criteria)
        );
    }

    @Test
    void setCriteriaForTask__requestSameSizeAsDb__self__valid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                // GIVEN
                var ctx = prepareGetSetForTaskContext(
                        getSelfEmployeeIdAsLong(),
                        2, 2
                );
                ObjectNode request = ctx.getRequest();
                long taskId = ctx.getData().taskId();
                long submissionId = ctx.getData().submissionId();
                List<Long> criteria = ctx.getData().criteria();

                // WHEN
                securePerform(post("/criteria/task/{id}/set", taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$", hasSize(2)))
                        .andExpectAll(ctx.getMatchers());

                // THEN
                securePerform(get("/criteria/task/{id}", taskId))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$", hasSize(2)))
                        .andExpectAll(ctx.getMatchers());

                securePerform(get("/submissions/{id}", submissionId))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.satisfiedCriteria",
                                contains(criteria.stream()
                                        .map(Math::toIntExact)
                                        .toArray()
                                )
                        ));
            }
        };
    }

    @Test
    void setCriteriaForTask__requestBiggerThanDb__self__valid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                // GIVEN
                var ctx = prepareGetSetForTaskContext(
                        getSelfEmployeeIdAsLong(),
                        2, 3
                );
                ObjectNode request = ctx.getRequest();
                long taskId = ctx.getData().taskId();
                long submissionId = ctx.getData().submissionId();
                List<Long> criteria = ctx.getData().criteria();

                // WHEN
                securePerform(post("/criteria/task/{id}/set", taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$", hasSize(3)))
                        .andExpectAll(ctx.getMatchers());

                // THEN
                securePerform(get("/criteria/task/{id}", taskId))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$", hasSize(3)))
                        .andExpectAll(ctx.getMatchers());

                securePerform(get("/submissions/{id}", submissionId))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.satisfiedCriteria",
                                contains(criteria.stream()
                                        .map(Math::toIntExact)
                                        .toArray()
                                )
                        ));
            }
        };
    }

    @Test
    void setCriteriaForTask__requestSmallerThanDb__self__valid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                // GIVEN
                var ctx = prepareGetSetForTaskContext(
                        getSelfEmployeeIdAsLong(),
                        3, 2
                );
                ObjectNode request = ctx.getRequest();
                long taskId = ctx.getData().taskId();
                long submissionId = ctx.getData().submissionId();
                List<Long> criteria = ctx.getData().criteria();

                // WHEN
                securePerform(post("/criteria/task/{id}/set", taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$", hasSize(2)))
                        .andExpectAll(ctx.getMatchers());

                // THEN
                securePerform(get("/criteria/task/{id}", taskId))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$", hasSize(2)))
                        .andExpectAll(ctx.getMatchers());

                securePerform(get("/submissions/{id}", submissionId))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.satisfiedCriteria",
                                contains(criteria.stream()
                                        .limit(2)
                                        .map(Math::toIntExact)
                                        .toArray()
                                )
                        ));
            }
        };
    }

    @Test
    void setCriteriaForTask__moveOneWithoutSubmissions__self__valid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                // GIVEN
                long taskId = ef.createTask(getSelfEmployeeIdAsLong());
                String sharedName = faker.lorem().word();
                List<Long> criteria = List.of(
                        ef.createCriteria(ef.bag().withTaskId(taskId)),
                        ef.createCriteria(ef.bag().withTaskId(taskId)
                                .withDto(CriteriaDto.builder().name(sharedName).build()))
                );
                long submissionId = ef.createSubmission(ef.bag().withTaskId(taskId)
                        .withDto(SubmissionDto.builder().satisfiedCriteria(criteria).build()));

                var ctx = getSetForTaskContext(List.of(
                        new CriteriaInfo(sharedName, faker.number().numberBetween(20, 80)),
                        new CriteriaInfo(faker.lorem().sentence(), faker.number().numberBetween(20, 80))
                ));
                ObjectNode request = ctx.getRequest();

                // WHEN
                securePerform(post("/criteria/task/{id}/set", taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$", hasSize(2)))
                        .andExpectAll(ctx.getMatchers());

                // THEN
                securePerform(get("/criteria/task/{id}", taskId))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$", hasSize(2)))
                        .andExpectAll(ctx.getMatchers());

                securePerform(get("/submissions/{id}", submissionId))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.satisfiedCriteria",
                                contains(criteria.stream()
                                        .limit(2)
                                        .map(Math::toIntExact)
                                        .toArray()
                                )
                        ));
            }
        };
    }

    @Test
    void setCriteriaForTask__otherAsAdmin__valid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                // GIVEN
                var ctx = prepareGetSetForTaskContext(
                        ef.createEmployee(),
                        2, 2
                );
                ObjectNode request = ctx.getRequest();
                long taskId = ctx.getData().taskId();
                long submissionId = ctx.getData().submissionId();
                List<Long> criteria = ctx.getData().criteria();

                // WHEN
                securePerform(post("/criteria/task/{id}/set", taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$", hasSize(2)))
                        .andExpectAll(ctx.getMatchers());

                // THEN
                securePerform(get("/criteria/task/{id}", taskId))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$", hasSize(2)))
                        .andExpectAll(ctx.getMatchers());

                securePerform(get("/submissions/{id}", submissionId))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.satisfiedCriteria",
                                contains(criteria.stream()
                                        .map(Math::toIntExact)
                                        .toArray()
                                )
                        ));
            }
        };
    }

    @Test
    void setCriteriaForTask__otherAsTeacher__invalid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                // GIVEN
                var ctx = prepareGetSetForTaskContext(
                        ef.createEmployee(),
                        2, 2
                );
                ObjectNode request = ctx.getRequest();
                long taskId = ctx.getData().taskId();

                // WHEN
                securePerform(post("/criteria/task/{id}/set", taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        // THEN
                        .andExpect(status().isForbidden());

            }
        };
    }

    @Test
    void setCriteriaForTask__withCourseToken__invalid() {
        new WithCourseToken() {
            @Override
            void run() throws Exception {
                // GIVEN
                var ctx = prepareGetSetForTaskContext(
                        ef.createEmployee(),
                        2, 2
                );
                ObjectNode request = ctx.getRequest();
                long taskId = ctx.getData().taskId();

                // WHEN
                securePerform(post("/criteria/task/{id}/set", taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        // THEN
                        .andExpect(status().isForbidden());

            }
        };
    }

    @Test
    void setCriteriaForTask__notExists__invalid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                // GIVEN
                var ctx = prepareGetSetForTaskContext(
                        getSelfEmployeeIdAsLong(),
                        2, 2
                );
                ObjectNode request = ctx.getRequest();
                long taskId = ctx.getData().taskId();

                // WHEN
                securePerform(post("/criteria/task/{id}/set", taskId + 1000)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        // THEN
                        .andExpect(status().isNotFound());

            }
        };
    }

    @Test
    void setCriteriaForTask__notAuthenticated__invalid() throws Exception {
        // GIVEN
        var ctx = prepareGetSetForTaskContext(
                ef.createEmployee(),
                2, 2
        );
        ObjectNode request = ctx.getRequest();
        long taskId = ctx.getData().taskId();

        // WHEN
        mvc.perform(post("/criteria/task/{id}/set", taskId + 1000)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                // THEN
                .andExpect(status().isUnauthorized());
    }

    // ================================================================================================================

    @Test
    void patchCriteria__self__valid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                String name = faker.lorem().sentence();
                long taskId = ef.createTask(getSelfEmployeeIdAsLong());
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
    void patchCriteria__withCourseToken__invalid() {
        new WithCourseToken() {
            @Override
            void run() throws Exception {
                String name = faker.lorem().sentence();
                long taskId = ef.createTask(ef.bag().withCourseId(getCourseId()));
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
                long employeeId = ef.createEmployee();
                String name = faker.lorem().sentence();
                int criteriaPercent = 45;
                long taskId = ef.createTask(employeeId);

                long prevTaskId = ef.createTask(employeeId);

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
                long taskId = ef.createTask();

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
                long taskId = ef.createTask();
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
                long taskId = ef.createTask(getSelfEmployeeIdAsLong());
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
    void deleteCriteria__withCourseToken__invalid() {
        new WithCourseToken() {
            @Override
            void run() throws Exception {
                String name = faker.lorem().sentence();
                long taskId = ef.createTask(ef.bag().withCourseId(getCourseId()));
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
