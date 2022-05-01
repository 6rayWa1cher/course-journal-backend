package com.a6raywa1cher.coursejournalbackend.integration;

import com.a6raywa1cher.coursejournalbackend.RequestContext;
import com.a6raywa1cher.coursejournalbackend.dto.CourseTokenDto;
import com.a6raywa1cher.coursejournalbackend.service.CourseTokenService;
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
public class CourseTokenControllerIntegrationTests extends AbstractIntegrationTests {
    @Autowired
    CourseTokenService courseTokenService;

    RequestContext<Long> createGetCourseTokenByIdContext(long userId) {
        long courseId = ef.createCourse(userId);

        long id = courseTokenService.create(
                CourseTokenDto.builder()
                        .course(courseId)
                        .build()
        ).getId();

        Function<String, ResultMatcher[]> matchers = prefix -> new ResultMatcher[]{
                jsonPath(prefix + ".id").value(id),
                jsonPath(prefix + ".course").value(courseId),
                jsonPath(prefix + ".token").isString()
        };

        return RequestContext.<Long>builder()
                .request(id)
                .matchersSupplier(matchers)
                .data(Map.of("courseId", Long.toString(courseId)))
                .build();
    }

    @Test
    void getCourseTokenById__self__valid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                var ctx = createGetCourseTokenByIdContext(getSelfEmployeeIdAsLong());

                long id = ctx.getRequest();
                ResultMatcher[] matchers = ctx.getMatchers();

                securePerform(get("/courses/tokens/{id}", id))
                        .andExpect(status().isOk())
                        .andExpectAll(matchers);
            }
        };
    }

    @Test
    void getCourseTokenById__otherAsAdmin__valid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                var ctx = createGetCourseTokenByIdContext(ef.createEmployee());

                long id = ctx.getRequest();
                ResultMatcher[] matchers = ctx.getMatchers();

                securePerform(get("/courses/tokens/{id}", id))
                        .andExpect(status().isOk())
                        .andExpectAll(matchers);
            }
        };
    }

    @Test
    void getCourseTokenById__otherAsTeacher__invalid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                var ctx = createGetCourseTokenByIdContext(ef.createEmployee());

                long id = ctx.getRequest();

                securePerform(get("/courses/tokens/{id}", id))
                        .andExpect(status().isForbidden());
            }
        };
    }

    @Test
    void getCourseTokenById__withCourseToken__valid() {
        long ownerId = ef.createEmployee();
        var ctx = createGetCourseTokenByIdContext(ownerId);

        long id = ctx.getRequest();
        ResultMatcher[] matchers = ctx.getMatchers();
        new WithCourseToken(Long.parseLong(ctx.getData().get("courseId")), false) {
            @Override
            void run() throws Exception {

                securePerform(get("/courses/tokens/{id}", id))
                        .andExpect(status().isOk())
                        .andExpectAll(matchers);
            }
        };
    }

    @Test
    void getCourseTokenById__notAuthenticated__invalid() throws Exception {
        var ctx = createGetCourseTokenByIdContext(ef.createEmployee());

        long id = ctx.getRequest();

        mvc.perform(get("/courses/tokens/{id}", id))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getCourseTokenById__notExists__invalid() {
        var ctx = createGetCourseTokenByIdContext(ef.createEmployee());

        long id = ctx.getRequest();

        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                securePerform(get("/courses/tokens/{id}", id + 1000))
                        .andExpect(status().isNotFound());
            }
        };
    }

    // ================================================================================================================

    RequestContext<Long> createGetCourseTokenByCourseContext(long courseId) {
        long id = courseTokenService.create(
                CourseTokenDto.builder()
                        .course(courseId)
                        .build()
        ).getId();

        Function<String, ResultMatcher[]> matchers = prefix -> new ResultMatcher[]{
                jsonPath(prefix + ".id").value(id),
                jsonPath(prefix + ".course").value(courseId),
                jsonPath(prefix + ".token").isString()
        };

        return RequestContext.<Long>builder()
                .request(id)
                .matchersSupplier(matchers)
                .build();
    }

    @Test
    void getCourseTokenByCourse__self__valid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                long courseId1 = ef.createCourse(getSelfEmployeeIdAsLong());
                long courseId2 = ef.createCourse(getSelfEmployeeIdAsLong());

                var ctx1 = createGetCourseTokenByCourseContext(courseId1);
                createGetCourseTokenByCourseContext(courseId2);
                ResultMatcher[] matchers = ctx1.getMatchers();

                securePerform(get("/courses/{id}/token", courseId1))
                        .andExpect(status().isOk())
                        .andExpectAll(matchers);
            }
        };
    }

    @Test
    void getCourseTokenByCourse__otherAsAdmin__valid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                long courseId1 = ef.createCourse();
                long courseId2 = ef.createCourse();

                var ctx1 = createGetCourseTokenByCourseContext(courseId1);
                createGetCourseTokenByCourseContext(courseId2);
                ResultMatcher[] matchers = ctx1.getMatchers();

                securePerform(get("/courses/{id}/token", courseId1))
                        .andExpect(status().isOk())
                        .andExpectAll(matchers);
            }
        };
    }

    @Test
    void getCourseTokenByCourse__otherAsTeacher__invalid() {
        long id = ef.createCourse();

        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                securePerform(get("/courses/{id}/token", id))
                        .andExpect(status().isForbidden());
            }
        };
    }

    @Test
    void getCourseTokenByCourse__withCourseToken__valid() {
        long ownerId = ef.createEmployee();
        long courseId1 = ef.createCourse(ownerId);
        long courseId2 = ef.createCourse(ownerId);
        var ctx1 = createGetCourseTokenByCourseContext(courseId1);
        createGetCourseTokenByCourseContext(courseId2);
        ResultMatcher[] matchers = ctx1.getMatchers();

        new WithCourseToken(courseId1, false) {
            @Override
            void run() throws Exception {
                securePerform(get("/courses/{id}/token", courseId1))
                        .andExpect(status().isOk())
                        .andExpectAll(matchers);
            }
        };
    }

    @Test
    void getCourseTokenByCourse__notAuthenticated__invalid() throws Exception {
        long id = ef.createCourse();
        mvc.perform(get("/courses/{id}/token", id)).andExpect(status().isUnauthorized());
    }

    @Test
    void getCourseTokenByCourse__notExists__invalid() {
        long course = ef.createCourse();
        var ctx = createGetCourseTokenByCourseContext(course);

        long id = ctx.getRequest();

        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                securePerform(get("/courses/{id}/token", id + 1000))
                        .andExpect(status().isNotFound());
            }
        };
    }

    // ================================================================================================================

    RequestContext<ObjectNode> createResolveCourseByTokenContext(long courseId) {
        String token = courseTokenService.create(
                CourseTokenDto.builder()
                        .course(courseId)
                        .build()
        ).getToken();

        ObjectNode request = objectMapper.createObjectNode()
                .put("token", token);

        Function<String, ResultMatcher[]> matchers = prefix -> new ResultMatcher[]{
                jsonPath(prefix + ".id").value(courseId),
        };

        return RequestContext.<ObjectNode>builder()
                .request(request)
                .matchersSupplier(matchers)
                .data(Map.of("token", token))
                .build();
    }

    @Test
    void resolveCourseByToken__self__invalid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                long courseId = ef.createCourse(getSelfEmployeeIdAsLong());

                var ctx = createResolveCourseByTokenContext(courseId);
                ObjectNode request = ctx.getRequest();

                securePerform(post("/courses/tokens/resolve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isForbidden());
            }
        };
    }

    @Test
    void resolveCourseByToken__otherAsAdmin__invalid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                long courseId = ef.createCourse();

                var ctx = createResolveCourseByTokenContext(courseId);
                ObjectNode request = ctx.getRequest();

                securePerform(post("/courses/tokens/resolve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isForbidden());
            }
        };
    }

    @Test
    void resolveCourseByToken__otherAsTeacher__invalid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                long courseId = ef.createCourse();

                var ctx = createResolveCourseByTokenContext(courseId);
                ObjectNode request = ctx.getRequest();

                securePerform(post("/courses/tokens/resolve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isForbidden());
            }
        };
    }

    @Test
    void resolveCourseByToken__withCourseToken__valid() {
        long courseId = ef.createCourse();
        var ctx = createResolveCourseByTokenContext(courseId);
        ObjectNode request = ctx.getRequest();
        ResultMatcher[] matchers = ctx.getMatchers();
        String token = ctx.getData().get("token");

        new WithCourseToken(token) {
            @Override
            void run() throws Exception {
                securePerform(post("/courses/tokens/resolve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isOk())
                        .andExpectAll(matchers);
            }
        };
    }

    @Test
    void resolveCourseByToken__withNotMatchingCourseToken__invalid() {
        long courseId = ef.createCourse();
        var ctx = createResolveCourseByTokenContext(courseId);
        String token = ctx.getData().get("token");

        new WithCourseToken(token) {
            @Override
            void run() throws Exception {
                ObjectNode request = objectMapper.createObjectNode()
                        .put("token", token + "abc");
                securePerform(post("/courses/tokens/resolve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isForbidden());
            }
        };
    }

    @Test
    void resolveCourseByToken__notAuthenticated__invalid() throws Exception {
        long courseId = ef.createCourse();
        var ctx = createResolveCourseByTokenContext(courseId);
        ObjectNode request = ctx.getRequest();
        mvc.perform(post("/courses/tokens/resolve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                .andExpect(status().isUnauthorized());
    }

    // ================================================================================================================

    RequestContext<ObjectNode> getCreateCourseTokenRequest(long courseId) {
        ObjectNode request = objectMapper.createObjectNode()
                .put("course", courseId);

        Function<String, ResultMatcher[]> matchers = prefix -> new ResultMatcher[]{
                jsonPath(prefix + ".id").isNumber(),
                jsonPath(prefix + ".course").value(courseId),
                jsonPath(prefix + ".token").isString()
        };

        return new RequestContext<>(request, matchers);
    }

    @Test
    void createCourseToken__self__valid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                long courseId = ef.createCourse(getSelfEmployeeIdAsLong());
                var ctx = getCreateCourseTokenRequest(courseId);

                ObjectNode request = ctx.getRequest();

                MvcResult mvcResult = securePerform(post("/courses/tokens/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isCreated())
                        .andExpectAll(ctx.getMatchers())
                        .andReturn();

                int id = JsonPath.read(mvcResult.getResponse().getContentAsString(), "$.id");

                securePerform(get("/courses/tokens/{id}", id))
                        .andExpect(status().isOk())
                        .andExpectAll(ctx.getMatchers());
            }
        };
    }

    @Test
    void createCourseToken__otherAsAdmin__valid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                long courseId = ef.createCourse();
                var ctx = getCreateCourseTokenRequest(courseId);

                ObjectNode request = ctx.getRequest();

                MvcResult mvcResult = securePerform(post("/courses/tokens/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isCreated())
                        .andExpectAll(ctx.getMatchers())
                        .andReturn();

                int id = JsonPath.read(mvcResult.getResponse().getContentAsString(), "$.id");

                securePerform(get("/courses/tokens/{id}", id))
                        .andExpect(status().isOk())
                        .andExpectAll(ctx.getMatchers());
            }
        };
    }

    @Test
    void createCourseToken__otherAsTeacher__invalid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                long courseId = ef.createCourse();
                var ctx = getCreateCourseTokenRequest(courseId);

                ObjectNode request = ctx.getRequest();

                securePerform(post("/courses/tokens/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isForbidden());
            }
        };
    }

    @Test
    void createCourseToken__withCourseToken__invalid() {
        new WithCourseToken() {
            @Override
            void run() throws Exception {
                var ctx = getCreateCourseTokenRequest(getCourseId());

                ObjectNode request = ctx.getRequest();

                securePerform(post("/courses/tokens/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isForbidden());
            }
        };
    }

    @Test
    void createCourseToken__tokenForCourseExists__invalid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                long courseId = ef.createCourse();
                var ctx = getCreateCourseTokenRequest(courseId);

                ef.createCourseToken(ef.bag().withCourseId(courseId));

                ObjectNode request = ctx.getRequest();

                securePerform(post("/courses/tokens/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isConflict());
            }
        };
    }

    @Test
    void createCourseToken__notAuthenticated__invalid() throws Exception {
        long courseId = ef.createCourse();
        var ctx = getCreateCourseTokenRequest(courseId);
        ObjectNode request = ctx.getRequest();

        mvc.perform(post("/courses/tokens/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                .andExpect(status().isUnauthorized());
    }

    // ================================================================================================================

    @Test
    void deleteCourseToken__self__valid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                long courseTokenId = ef.createCourseToken(getSelfEmployeeIdAsLong());

                securePerform(delete("/courses/tokens/{id}", courseTokenId))
                        .andExpect(status().isOk());

                securePerform(get("/courses/tokens/{id}", courseTokenId))
                        .andExpect(status().isNotFound());
            }
        };
    }

    @Test
    void deleteCourseToken__otherAsAdmin__valid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                long courseTokenId = ef.createCourseToken();

                securePerform(delete("/courses/tokens/{id}", courseTokenId))
                        .andExpect(status().isOk());

                securePerform(get("/courses/tokens/{id}", courseTokenId))
                        .andExpect(status().isNotFound());
            }
        };
    }

    @Test
    void deleteCourseToken__otherAsTeacher__invalid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                long courseTokenId = ef.createCourseToken();

                securePerform(delete("/courses/tokens/{id}", courseTokenId))
                        .andExpect(status().isForbidden());
            }
        };
    }

    @Test
    void deleteCourseToken__withCourseToken__invalid() {
        new WithCourseToken() {
            @Override
            void run() throws Exception {
                securePerform(delete("/courses/tokens/{id}", getTokenId()))
                        .andExpect(status().isForbidden());
            }
        };
    }

    @Test
    void deleteCourseToken__notFound__invalid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                long courseTokenId = ef.createCourseToken(getSelfEmployeeIdAsLong());

                securePerform(delete("/courses/tokens/{id}", courseTokenId + 1000))
                        .andExpect(status().isNotFound());
            }
        };
    }

    @Test
    void deleteCourseToken__notAuthenticated__invalid() throws Exception {
        long courseTokenId = ef.createCourseToken();

        mvc.perform(delete("/courses/tokens/{id}", courseTokenId))
                .andExpect(status().isUnauthorized());
    }
}
