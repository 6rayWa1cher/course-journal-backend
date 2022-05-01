package com.a6raywa1cher.coursejournalbackend.integration;

import com.a6raywa1cher.coursejournalbackend.RequestContext;
import com.a6raywa1cher.coursejournalbackend.dto.CreateEditAuthUserDto;
import com.a6raywa1cher.coursejournalbackend.model.UserRole;
import com.a6raywa1cher.coursejournalbackend.service.StudentService;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultMatcher;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.a6raywa1cher.coursejournalbackend.TestUtils.basic;
import static com.a6raywa1cher.coursejournalbackend.TestUtils.randomUserRole;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
public class AuthUserControllerIntegrationTests extends AbstractIntegrationTests {
    @Autowired
    StudentService studentService;

    RequestContext<Long> createGetAuthUserListContext(UserRole userRole) {
        String username = faker.name().username();
        String password = faker.internet().password();
        Long userInfoId = ef.createUserInfoId(userRole);

        long id = authUserService.create(CreateEditAuthUserDto.builder()
                .username(username)
                .password(password)
                .userRole(userRole)
                .userInfo(userInfoId)
                .build()).getId();


        Function<String, ResultMatcher[]> matchers = prefix ->
                getAuthUserMatchers(prefix, id, username, userRole, userInfoId);

        return RequestContext.<Long>builder()
                .request(id)
                .matchersSupplier(matchers)
                .data(Map.of("username", username, "password", password))
                .build();
    }

    @Test
    void getAuthUserList__admin__valid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                var ctx1 = createGetAuthUserListContext(UserRole.TEACHER);
                var ctx2 = createGetAuthUserListContext(UserRole.HEADMAN);

                securePerform(get("/auth-user/").queryParam("sort", "id,desc"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.totalElements").value(3))
                        .andExpectAll(ctx2.getMatchers("$.content[0]"))
                        .andExpectAll(ctx1.getMatchers("$.content[1]"))
                        .andExpect(jsonPath("$.content[2].username").value(ADMIN_USERNAME));
            }
        };
    }

    @Test
    void getAuthUserList__notAdmin__invalid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                securePerform(get("/auth-user/"))
                        .andExpect(status().isForbidden());
            }
        };
    }

    @Test
    void getAuthUserList__notAuthenticated__invalid() throws Exception {
        mvc.perform(get("/auth-user/"))
                .andExpect(status().isUnauthorized());
    }

    // ================================================================================================================

    RequestContext<Long> createGetAuthUserByIdContext() {
        return createGetAuthUserListContext(randomUserRole());
    }

    @Test
    void getAuthUserById__self__valid() {
        var ctx = createGetAuthUserByIdContext();
        String username = ctx.getData().get("username");
        String password = ctx.getData().get("password");

        new WithUser(username, password, false) {
            @Override
            void run() throws Exception {
                long id = ctx.getRequest();
                securePerform(get("/auth-user/{id}", id))
                        .andExpect(status().isOk())
                        .andExpectAll(ctx.getMatchers());
            }
        };
    }

    @Test
    void getAuthUserById__otherAsAdmin__valid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                var ctx = createGetAuthUserByIdContext();
                long id = ctx.getRequest();

                securePerform(get("/auth-user/{id}", id))
                        .andExpect(status().isOk())
                        .andExpectAll(ctx.getMatchers());
            }
        };
    }

    @Test
    void getAuthUserById__otherAsTeacher__invalid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                var ctx = createGetAuthUserByIdContext();
                long id = ctx.getRequest();

                securePerform(get("/auth-user/{id}", id))
                        .andExpect(status().isForbidden());
            }
        };
    }

    @Test
    void getAuthUserById__withCourseToken__invalid() {
        new WithCourseToken() {
            @Override
            void run() throws Exception {
                var ctx = createGetAuthUserByIdContext();
                long id = ctx.getRequest();

                securePerform(get("/auth-user/{id}", id))
                        .andExpect(status().isForbidden());
            }
        };
    }

    @Test
    void getAuthUserById__notAuthenticated__invalid() throws Exception {
        var ctx = createGetAuthUserByIdContext();
        long id = ctx.getRequest();

        mvc.perform(get("/auth-user/{id}", id))
                .andExpect(status().isUnauthorized());
    }

    // ================================================================================================================

    RequestContext<Long> createGetAuthUserByEmployeeIdContext(long employeeId) {
        String username = faker.name().username();
        String password = faker.internet().password();

        long id = authUserService.create(CreateEditAuthUserDto.builder()
                .username(username)
                .password(password)
                .userRole(UserRole.TEACHER)
                .userInfo(employeeId)
                .build()).getId();


        Function<String, ResultMatcher[]> matchers = prefix ->
                getAuthUserMatchers(prefix, id, username, UserRole.TEACHER, employeeId);

        return RequestContext.<Long>builder()
                .request(employeeId)
                .matchersSupplier(matchers)
                .data(Map.of("username", username, "password", password))
                .build();
    }

    @Test
    void getAuthUserByEmployeeId__self__valid() {
        var ctx = createGetAuthUserByEmployeeIdContext(ef.createEmployee());
        String username = ctx.getData().get("username");
        String password = ctx.getData().get("password");

        new WithUser(username, password, false) {
            @Override
            void run() throws Exception {
                long id = ctx.getRequest();
                securePerform(get("/auth-user/employee/{id}", id))
                        .andExpect(status().isOk())
                        .andExpectAll(ctx.getMatchers());
            }
        };
    }

    @Test
    void getAuthUserByEmployeeId__otherAsAdmin__valid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                var ctx = createGetAuthUserByEmployeeIdContext(ef.createEmployee());
                long id = ctx.getRequest();

                securePerform(get("/auth-user/employee/{id}", id))
                        .andExpect(status().isOk())
                        .andExpectAll(ctx.getMatchers());
            }
        };
    }

    @Test
    void getAuthUserByEmployeeId__otherAsTeacher__invalid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                var ctx = createGetAuthUserByEmployeeIdContext(ef.createEmployee());
                long id = ctx.getRequest();

                securePerform(get("/auth-user/employee/{id}", id))
                        .andExpect(status().isForbidden());
            }
        };
    }

    @Test
    void getAuthUserByEmployeeId__withCourseToken__invalid() {
        new WithCourseToken() {
            @Override
            void run() throws Exception {
                var ctx = createGetAuthUserByEmployeeIdContext(ef.createEmployee());
                long id = ctx.getRequest();

                securePerform(get("/auth-user/employee/{id}", id))
                        .andExpect(status().isForbidden());
            }
        };
    }

    @Test
    void getAuthUserByEmployeeId__notAuthenticated__invalid() throws Exception {
        var ctx = createGetAuthUserByEmployeeIdContext(ef.createEmployee());
        long id = ctx.getRequest();

        mvc.perform(get("/auth-user/employee/{id}", id))
                .andExpect(status().isUnauthorized());
    }

    // ================================================================================================================

    RequestContext<Long> createGetAuthUserByStudentIdContext(long studentId) {
        String username = faker.name().username();
        String password = faker.internet().password();

        long id = authUserService.create(CreateEditAuthUserDto.builder()
                .username(username)
                .password(password)
                .userRole(UserRole.HEADMAN)
                .userInfo(studentId)
                .build()).getId();


        Function<String, ResultMatcher[]> matchers = prefix ->
                getAuthUserMatchers(prefix, id, username, UserRole.HEADMAN, studentId);

        return RequestContext.<Long>builder()
                .request(studentId)
                .matchersSupplier(matchers)
                .data(Map.of("username", username, "password", password))
                .build();
    }

    @Test
    void getAuthUserByStudentId__self__valid() {
        var ctx = createGetAuthUserByStudentIdContext(ef.createStudent());
        String username = ctx.getData().get("username");
        String password = ctx.getData().get("password");

        new WithUser(username, password, false) {
            @Override
            void run() throws Exception {
                long id = ctx.getRequest();
                securePerform(get("/auth-user/student/{id}", id))
                        .andExpect(status().isOk())
                        .andExpectAll(ctx.getMatchers());
            }
        };
    }

    @Test
    void getAuthUserByStudentId__otherAsAdmin__valid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                var ctx = createGetAuthUserByStudentIdContext(ef.createStudent());
                long id = ctx.getRequest();

                securePerform(get("/auth-user/student/{id}", id))
                        .andExpect(status().isOk())
                        .andExpectAll(ctx.getMatchers());
            }
        };
    }

    @Test
    void getAuthUserByStudentId__otherAsTeacher__invalid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                var ctx = createGetAuthUserByStudentIdContext(ef.createStudent());
                long id = ctx.getRequest();

                securePerform(get("/auth-user/student/{id}", id))
                        .andExpect(status().isForbidden());
            }
        };
    }

    @Test
    void getAuthUserByStudentId__withCourseToken__invalid() {
        new WithCourseToken() {
            @Override
            void run() throws Exception {
                var ctx = createGetAuthUserByStudentIdContext(ef.createStudent());
                long id = ctx.getRequest();

                securePerform(get("/auth-user/student/{id}", id))
                        .andExpect(status().isForbidden());
            }
        };
    }

    @Test
    void getAuthUserByStudentId__notAuthenticated__invalid() throws Exception {
        var ctx = createGetAuthUserByStudentIdContext(ef.createStudent());
        long id = ctx.getRequest();

        mvc.perform(get("/auth-user/student/{id}", id))
                .andExpect(status().isUnauthorized());
    }

    // ================================================================================================================

    RequestContext<ObjectNode> getCreateAuthUserRequest(UserRole userRole, Long userInfoId, String username) {
        String password = faker.internet().password();

        ObjectNode request = objectMapper.createObjectNode()
                .put("username", username)
                .put("password", password)
                .put("userRole", userRole.name())
                .put("userInfo", userInfoId);

        Function<String, ResultMatcher[]> matchers = prefix ->
                getAuthUserMatchers(prefix, username, userRole, userInfoId);

        return RequestContext.<ObjectNode>builder()
                .request(request)
                .matchersSupplier(matchers)
                .data(Map.of("username", username, "password", password))
                .build();
    }

    RequestContext<ObjectNode> getCreateAuthUserRequest(UserRole userRole, Long userInfoId) {
        String username = faker.name().username();
        return getCreateAuthUserRequest(userRole, userInfoId, username);
    }

    RequestContext<ObjectNode> getCreateAuthUserRequest(String username) {
        UserRole userRole = randomUserRole();
        Long userInfoId = ef.createUserInfoId(userRole);
        return getCreateAuthUserRequest(userRole, userInfoId, username);
    }

    @Test
    void createAuthUser__admin__asAdmin__valid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                var ctx = getCreateAuthUserRequest(UserRole.ADMIN, null);
                ObjectNode request = ctx.getRequest();

                MvcResult mvcResult = securePerform(post("/auth-user/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isCreated())
                        .andExpectAll(ctx.getMatchers())
                        .andReturn();

                int id = JsonPath.read(mvcResult.getResponse().getContentAsString(), "$.id");

                securePerform(get("/auth-user/{id}", id))
                        .andExpect(status().isOk())
                        .andExpectAll(ctx.getMatchers());

                String username = ctx.getData().get("username");
                String password = ctx.getData().get("password");
                mvc.perform(get("/auth/check")
                                .header(HttpHeaders.AUTHORIZATION, basic(username, password)))
                        .andExpect(status().isOk());
            }
        };
    }

    @Test
    void createAuthUser__teacher__asAdmin__valid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                long employeeId = ef.createEmployee();
                var ctx = getCreateAuthUserRequest(UserRole.TEACHER, employeeId);
                ObjectNode request = ctx.getRequest();

                MvcResult mvcResult = securePerform(post("/auth-user/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isCreated())
                        .andExpectAll(ctx.getMatchers())
                        .andReturn();

                int id = JsonPath.read(mvcResult.getResponse().getContentAsString(), "$.id");

                securePerform(get("/auth-user/{id}", id))
                        .andExpect(status().isOk())
                        .andExpectAll(ctx.getMatchers());

                String username = ctx.getData().get("username");
                String password = ctx.getData().get("password");
                mvc.perform(get("/auth/check")
                                .header(HttpHeaders.AUTHORIZATION, basic(username, password)))
                        .andExpect(status().isOk());
            }
        };
    }

    @Test
    void createAuthUser__headman__asAdmin__valid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                long studentId = ef.createStudent();
                var ctx = getCreateAuthUserRequest(UserRole.HEADMAN, studentId);
                ObjectNode request = ctx.getRequest();

                MvcResult mvcResult = securePerform(post("/auth-user/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isCreated())
                        .andExpectAll(ctx.getMatchers())
                        .andReturn();

                int id = JsonPath.read(mvcResult.getResponse().getContentAsString(), "$.id");

                securePerform(get("/auth-user/{id}", id))
                        .andExpect(status().isOk())
                        .andExpectAll(ctx.getMatchers());

                String username = ctx.getData().get("username");
                String password = ctx.getData().get("password");
                mvc.perform(get("/auth/check")
                                .header(HttpHeaders.AUTHORIZATION, basic(username, password)))
                        .andExpect(status().isOk());
            }
        };
    }

    @Test
    void createAuthUser__notAdmin__invalid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                var ctx = getCreateAuthUserRequest(UserRole.ADMIN, null);
                ObjectNode request = ctx.getRequest();

                securePerform(post("/auth-user/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isForbidden());
            }
        };
    }

    @Test
    void createAuthUser__studentConflict__invalid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                long studentId = ef.createStudent();
                ef.createAuthUser(ef.bag()
                        .withDto(CreateEditAuthUserDto.builder()
                                .userRole(UserRole.HEADMAN)
                                .userInfo(studentId)
                                .build()
                        ));

                var ctx = getCreateAuthUserRequest(UserRole.HEADMAN, studentId);
                ObjectNode request = ctx.getRequest();

                securePerform(post("/auth-user/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isConflict());
            }
        };
    }

    @Test
    void createAuthUser__employeeConflict__invalid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                long employeeId = ef.createEmployee();
                ef.createAuthUser(ef.bag()
                        .withDto(CreateEditAuthUserDto.builder()
                                .userRole(UserRole.TEACHER)
                                .userInfo(employeeId)
                                .build()
                        ));

                var ctx = getCreateAuthUserRequest(UserRole.TEACHER, employeeId);
                ObjectNode request = ctx.getRequest();

                securePerform(post("/auth-user/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isConflict());
            }
        };
    }

    @Test
    void createAuthUser__usernameConflict__invalid() {
        String username = faker.name().username();
        ef.createAuthUser(ef.bag()
                .withDto(CreateEditAuthUserDto.builder()
                        .username(username)
                        .build()
                ));

        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                var ctx = getCreateAuthUserRequest(username);
                ObjectNode request = ctx.getRequest();

                securePerform(post("/auth-user/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isConflict());
            }
        };
    }

    @Test
    void createAuthUser__admin__withTarget__invalid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                long employeeId = ef.createEmployee();
                var ctx = getCreateAuthUserRequest(UserRole.ADMIN, employeeId);
                ObjectNode request = ctx.getRequest();

                securePerform(post("/auth-user/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isBadRequest());
            }
        };
    }

    @Test
    void createAuthUser__teacher__withInvalidTarget__invalid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                long studentId = ef.createStudent();
                var ctx1 = getCreateAuthUserRequest(UserRole.TEACHER, studentId);
                ObjectNode request1 = ctx1.getRequest();

                securePerform(post("/auth-user/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request1.toString()))
                        .andExpect(status().isNotFound());

                var ctx2 = getCreateAuthUserRequest(UserRole.TEACHER, null);
                ObjectNode request2 = ctx2.getRequest();

                securePerform(post("/auth-user/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request2.toString()))
                        .andExpect(status().isBadRequest());
            }
        };
    }

    @Test
    void createAuthUser__headman__withInvalidTarget__invalid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                long employeeId = ef.createEmployee();
                var ctx1 = getCreateAuthUserRequest(UserRole.HEADMAN, employeeId);
                ObjectNode request1 = ctx1.getRequest();

                securePerform(post("/auth-user/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request1.toString()))
                        .andExpect(status().isNotFound());

                var ctx2 = getCreateAuthUserRequest(UserRole.HEADMAN, null);
                ObjectNode request2 = ctx2.getRequest();

                securePerform(post("/auth-user/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request2.toString()))
                        .andExpect(status().isBadRequest());
            }
        };
    }

    @Test
    void createAuthUser__notAuthenticated__invalid() throws Exception {
        var ctx = getCreateAuthUserRequest(UserRole.ADMIN, null);
        ObjectNode request = ctx.getRequest();

        mvc.perform(post("/auth-user/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                .andExpect(status().isUnauthorized());
    }

    // ================================================================================================================

    RequestContext<ObjectNode> getUpdateAuthUserRequest(long id, String username, String password,
                                                        UserRole userRole, Long userInfoId) {
        ObjectNode request = objectMapper.createObjectNode()
                .put("username", username)
                .put("password", password)
                .put("userRole", userRole.name())
                .put("userInfo", userInfoId);

        Function<String, ResultMatcher[]> matchers = prefix ->
                getAuthUserMatchers(prefix, id, username, userRole, userInfoId);

        return RequestContext.<ObjectNode>builder()
                .request(request)
                .matchersSupplier(matchers)
                .build();
    }

    @Test
    void updateAuthUser__self__valid() {
        String prevUsername = faker.name().username();
        String prevPassword = faker.internet().password();
        UserRole userRole = randomUserRole();
        Long userInfoId = ef.createUserInfoId(userRole);
        long id = authUserService.create(CreateEditAuthUserDto.builder()
                .username(prevUsername)
                .password(prevPassword)
                .userRole(userRole)
                .userInfo(userInfoId)
                .build()).getId();

        new WithUser(prevUsername, prevPassword, false) {
            @Override
            void run() throws Exception {
                String newUsername = faker.name().username();
                String newPassword = faker.internet().password();

                var ctx = getUpdateAuthUserRequest(
                        id, newUsername, newPassword, userRole, userInfoId
                );
                ObjectNode request = ctx.getRequest();

                securePerform(put("/auth-user/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isOk())
                        .andExpectAll(ctx.getMatchers());

                securePerform(get("/auth/check"))
                        .andExpect(status().isUnauthorized());

                this.setUsername(newUsername);
                this.setPassword(newPassword);

                securePerform(get("/auth-user/{id}", id))
                        .andExpect(status().isOk())
                        .andExpectAll(ctx.getMatchers());
            }
        };
    }

    @Test
    void updateAuthUser__otherAsAdmin__valid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                String prevUsername = faker.name().username();
                String prevPassword = faker.internet().password();
                UserRole userRole = randomUserRole();
                Long userInfoId = ef.createUserInfoId(userRole);
                long id = authUserService.create(CreateEditAuthUserDto.builder()
                        .username(prevUsername)
                        .password(prevPassword)
                        .userRole(userRole)
                        .userInfo(userInfoId)
                        .build()).getId();

                String newUsername = faker.name().username();
                String newPassword = faker.internet().password();

                var ctx = getUpdateAuthUserRequest(
                        id, newUsername, newPassword, userRole, userInfoId
                );
                ObjectNode request = ctx.getRequest();

                securePerform(put("/auth-user/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isOk())
                        .andExpectAll(ctx.getMatchers());

                securePerform(get("/auth-user/{id}", id))
                        .andExpect(status().isOk())
                        .andExpectAll(ctx.getMatchers());

                mvc.perform(get("/auth/check")
                                .header(HttpHeaders.AUTHORIZATION, basic(newUsername, newPassword)))
                        .andExpect(status().isOk());
            }
        };
    }

    @Test
    void updateAuthUser__otherAsTeacher__invalid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                String prevUsername = faker.name().username();
                String prevPassword = faker.internet().password();
                UserRole userRole = randomUserRole();
                Long userInfoId = ef.createUserInfoId(userRole);
                long id = authUserService.create(CreateEditAuthUserDto.builder()
                        .username(prevUsername)
                        .password(prevPassword)
                        .userRole(userRole)
                        .userInfo(userInfoId)
                        .build()).getId();

                String newUsername = faker.name().username();
                String newPassword = faker.internet().password();

                var ctx = getUpdateAuthUserRequest(
                        id, newUsername, newPassword, userRole, userInfoId
                );
                ObjectNode request = ctx.getRequest();

                securePerform(put("/auth-user/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isForbidden());
            }
        };
    }

    @Test
    void updateAuthUser__withCourseToken__invalid() {
        new WithCourseToken() {
            @Override
            void run() throws Exception {
                String prevUsername = faker.name().username();
                String prevPassword = faker.internet().password();
                UserRole userRole = randomUserRole();
                Long userInfoId = ef.createUserInfoId(userRole);
                long id = authUserService.create(CreateEditAuthUserDto.builder()
                        .username(prevUsername)
                        .password(prevPassword)
                        .userRole(userRole)
                        .userInfo(userInfoId)
                        .build()).getId();

                String newUsername = faker.name().username();
                String newPassword = faker.internet().password();

                var ctx = getUpdateAuthUserRequest(
                        id, newUsername, newPassword, userRole, userInfoId
                );
                ObjectNode request = ctx.getRequest();

                securePerform(put("/auth-user/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isForbidden());
            }
        };
    }

    @Test
    void updateAuthUser__roleChange__invalid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                String prevUsername = faker.name().username();
                String prevPassword = faker.internet().password();
                UserRole userRole = randomUserRole();
                Long userInfoId = ef.createUserInfoId(userRole);
                long id = authUserService.create(CreateEditAuthUserDto.builder()
                        .username(prevUsername)
                        .password(prevPassword)
                        .userRole(userRole)
                        .userInfo(userInfoId)
                        .build()).getId();

                String newUsername = faker.name().username();
                String newPassword = faker.internet().password();
                UserRole newUserRole = getOtherUserRole(userRole);
                Long newUserInfoId = ef.createUserInfoId(newUserRole);

                var ctx = getUpdateAuthUserRequest(
                        id, newUsername, newPassword, newUserRole, newUserInfoId
                );
                ObjectNode request = ctx.getRequest();

                securePerform(put("/auth-user/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isBadRequest());
            }
        };
    }

    @Test
    void updateAuthUser__targetChange__invalid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                String prevUsername = faker.name().username();
                String prevPassword = faker.internet().password();
                // if admin, we can change null to null, which isn't the scope of this test
                UserRole userRole = getOtherUserRole(UserRole.ADMIN);
                Long userInfoId = ef.createUserInfoId(userRole);
                long id = authUserService.create(CreateEditAuthUserDto.builder()
                        .username(prevUsername)
                        .password(prevPassword)
                        .userRole(userRole)
                        .userInfo(userInfoId)
                        .build()).getId();

                String newUsername = faker.name().username();
                String newPassword = faker.internet().password();
                Long newUserInfoId = ef.createUserInfoId(userRole);

                var ctx = getUpdateAuthUserRequest(
                        id, newUsername, newPassword, userRole, newUserInfoId
                );
                ObjectNode request = ctx.getRequest();

                securePerform(put("/auth-user/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isBadRequest());
            }
        };
    }

    @Test
    void updateAuthUser__usernameConflict__invalid() {
        String username = faker.name().username();
        ef.createAuthUser(ef.bag()
                .withDto(CreateEditAuthUserDto.builder()
                        .username(username)
                        .build()
                ));

        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                String prevUsername = faker.name().username();
                String prevPassword = faker.internet().password();
                UserRole userRole = randomUserRole();
                Long userInfoId = ef.createUserInfoId(userRole);
                long id = authUserService.create(CreateEditAuthUserDto.builder()
                        .username(prevUsername)
                        .password(prevPassword)
                        .userRole(userRole)
                        .userInfo(userInfoId)
                        .build()).getId();

                String newPassword = faker.internet().password();

                var ctx = getUpdateAuthUserRequest(
                        id, username, newPassword, userRole, userInfoId
                );
                ObjectNode request = ctx.getRequest();

                securePerform(put("/auth-user/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isConflict());
            }
        };
    }

    @Test
    void updateAuthUser__notFound__invalid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                long id = ef.createAuthUser();
                String newUsername = faker.name().username();
                String newPassword = faker.internet().password();
                UserRole newUserRole = randomUserRole();
                Long newUserInfoId = ef.createUserInfoId(newUserRole);

                var ctx = getUpdateAuthUserRequest(
                        id, newUsername, newPassword, newUserRole, newUserInfoId
                );
                ObjectNode request = ctx.getRequest();

                securePerform(put("/auth-user/{id}", id + 1000)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isNotFound());
            }
        };
    }

    @Test
    void updateAuthUser__notAuthenticated__invalid() throws Exception {
        long id = ef.createAuthUser();
        String newUsername = faker.name().username();
        String newPassword = faker.internet().password();
        UserRole newUserRole = randomUserRole();
        Long newUserInfoId = ef.createUserInfoId(newUserRole);

        var ctx = getUpdateAuthUserRequest(
                id, newUsername, newPassword, newUserRole, newUserInfoId
        );
        ObjectNode request = ctx.getRequest();

        mvc.perform(put("/auth-user/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                .andExpect(status().isUnauthorized());
    }

    // ================================================================================================================

    @Test
    void patchAuthUser__self__valid() {
        String prevUsername = faker.name().username();
        String prevPassword = faker.internet().password();
        UserRole userRole = randomUserRole();
        Long userInfoId = ef.createUserInfoId(userRole);
        long id = authUserService.create(CreateEditAuthUserDto.builder()
                .username(prevUsername)
                .password(prevPassword)
                .userRole(userRole)
                .userInfo(userInfoId)
                .build()).getId();

        new WithUser(prevUsername, prevPassword, false) {
            @Override
            void run() throws Exception {
                String newPassword = faker.internet().password();

                ObjectNode request = objectMapper.createObjectNode()
                        .put("password", newPassword);
                ResultMatcher[] matchers = getAuthUserMatchers(id, prevUsername, userRole, userInfoId);

                securePerform(patch("/auth-user/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isOk())
                        .andExpectAll(matchers);

                securePerform(get("/auth/check"))
                        .andExpect(status().isUnauthorized());

                this.setPassword(newPassword);

                securePerform(get("/auth-user/{id}", id))
                        .andExpect(status().isOk())
                        .andExpectAll(matchers);
            }
        };
    }

    @Test
    void patchAuthUser__otherAsAdmin__valid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                String username = faker.name().username();
                String prevPassword = faker.internet().password();
                UserRole userRole = randomUserRole();
                Long userInfoId = ef.createUserInfoId(userRole);
                long id = authUserService.create(CreateEditAuthUserDto.builder()
                        .username(username)
                        .password(prevPassword)
                        .userRole(userRole)
                        .userInfo(userInfoId)
                        .build()).getId();

                String newPassword = faker.internet().password();

                ObjectNode request = objectMapper.createObjectNode()
                        .put("password", newPassword);
                ResultMatcher[] matchers = getAuthUserMatchers(id, username, userRole, userInfoId);

                securePerform(patch("/auth-user/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isOk())
                        .andExpectAll(matchers);

                securePerform(get("/auth-user/{id}", id))
                        .andExpect(status().isOk())
                        .andExpectAll(matchers);

                mvc.perform(get("/auth/check")
                                .header(HttpHeaders.AUTHORIZATION, basic(username, newPassword)))
                        .andExpect(status().isOk());
            }
        };
    }

    @Test
    void patchAuthUser__otherAsTeacher__invalid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                String username = faker.name().username();
                String prevPassword = faker.internet().password();
                UserRole userRole = randomUserRole();
                Long userInfoId = ef.createUserInfoId(userRole);
                long id = authUserService.create(CreateEditAuthUserDto.builder()
                        .username(username)
                        .password(prevPassword)
                        .userRole(userRole)
                        .userInfo(userInfoId)
                        .build()).getId();

                String newPassword = faker.internet().password();

                ObjectNode request = objectMapper.createObjectNode()
                        .put("password", newPassword);

                securePerform(patch("/auth-user/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isForbidden());
            }
        };
    }

    @Test
    void patchAuthUser__withCourseToken__invalid() {
        new WithCourseToken() {
            @Override
            void run() throws Exception {
                String username = faker.name().username();
                String prevPassword = faker.internet().password();
                UserRole userRole = randomUserRole();
                Long userInfoId = ef.createUserInfoId(userRole);
                long id = authUserService.create(CreateEditAuthUserDto.builder()
                        .username(username)
                        .password(prevPassword)
                        .userRole(userRole)
                        .userInfo(userInfoId)
                        .build()).getId();

                String newPassword = faker.internet().password();

                ObjectNode request = objectMapper.createObjectNode()
                        .put("password", newPassword);

                securePerform(patch("/auth-user/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isForbidden());
            }
        };
    }

    @Test
    void patchAuthUser__roleChange__invalid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                String username = faker.name().username();
                String password = faker.internet().password();
                // we can't assign null to userInfo with the patch method, so ignore the admin role for this test
                UserRole prevUserRole = getOtherUserRole(UserRole.ADMIN);
                Long prevUserInfoId = ef.createUserInfoId(prevUserRole);
                long id = authUserService.create(CreateEditAuthUserDto.builder()
                        .username(username)
                        .password(password)
                        .userRole(prevUserRole)
                        .userInfo(prevUserInfoId)
                        .build()).getId();

                UserRole newUserRole = getOtherUserRole(UserRole.ADMIN, prevUserRole);
                Long newUserInfoId = ef.createUserInfoId(newUserRole);

                ObjectNode request = objectMapper.createObjectNode()
                        .put("userRole", newUserRole.name())
                        .put("userInfo", newUserInfoId);

                securePerform(patch("/auth-user/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isBadRequest());
            }
        };
    }

    @Test
    void patchAuthUser__targetChange__invalid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                String username = faker.name().username();
                String password = faker.internet().password();
                // we can't assign null to userInfo with the patch method, so ignore the admin role for this test
                UserRole userRole = getOtherUserRole(UserRole.ADMIN);
                Long prevUserInfoId = ef.createUserInfoId(userRole);
                long id = authUserService.create(CreateEditAuthUserDto.builder()
                        .username(username)
                        .password(password)
                        .userRole(userRole)
                        .userInfo(prevUserInfoId)
                        .build()).getId();

                Long newUserInfoId = ef.createUserInfoId(userRole);

                ObjectNode request = objectMapper.createObjectNode()
                        .put("userInfo", newUserInfoId);

                securePerform(patch("/auth-user/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isBadRequest());
            }
        };
    }

    @Test
    void patchAuthUser__usernameConflict__invalid() {
        String username = faker.name().username();
        ef.createAuthUser(ef.bag().withDto(CreateEditAuthUserDto.builder()
                .username(username)
                .build())
        );
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                String prevUsername = faker.name().username();
                String password = faker.internet().password();
                UserRole userRole = randomUserRole();
                Long userInfoId = ef.createUserInfoId(userRole);
                long id = authUserService.create(CreateEditAuthUserDto.builder()
                        .username(prevUsername)
                        .password(password)
                        .userRole(userRole)
                        .userInfo(userInfoId)
                        .build()).getId();

                ObjectNode request = objectMapper.createObjectNode()
                        .put("username", username);

                securePerform(patch("/auth-user/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isConflict());
            }
        };
    }

    @Test
    void patchAuthUser__notFound__invalid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                String username = faker.name().username();
                String prevPassword = faker.internet().password();
                UserRole userRole = randomUserRole();
                Long userInfoId = ef.createUserInfoId(userRole);
                long id = authUserService.create(CreateEditAuthUserDto.builder()
                        .username(username)
                        .password(prevPassword)
                        .userRole(userRole)
                        .userInfo(userInfoId)
                        .build()).getId();

                String newPassword = faker.internet().password();

                ObjectNode request = objectMapper.createObjectNode()
                        .put("password", newPassword);

                securePerform(patch("/auth-user/{id}", id + 1000)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isNotFound());
            }
        };
    }

    @Test
    void patchAuthUser__notAuthenticated__invalid() throws Exception {
        String username = faker.name().username();
        String prevPassword = faker.internet().password();
        UserRole userRole = randomUserRole();
        Long userInfoId = ef.createUserInfoId(userRole);
        long id = authUserService.create(CreateEditAuthUserDto.builder()
                .username(username)
                .password(prevPassword)
                .userRole(userRole)
                .userInfo(userInfoId)
                .build()).getId();

        String newPassword = faker.internet().password();

        ObjectNode request = objectMapper.createObjectNode()
                .put("password", newPassword);

        mvc.perform(patch("/auth-user/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                .andExpect(status().isUnauthorized());
    }

    // ================================================================================================================

    @Test
    void deleteAuthUser__self__invalid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                long id = getIdAsLong();
                securePerform(delete("/auth-user/{id}", id))
                        .andExpect(status().isForbidden());
            }
        };
    }

    @Test
    void deleteAuthUser__otherAsAdmin__valid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                String username = faker.name().username();
                String password = faker.internet().password();
                long id = ef.createAuthUser(ef.bag().withDto(CreateEditAuthUserDto.builder()
                        .username(username)
                        .password(password)
                        .build()
                ));

                mvc.perform(get("/auth/check")
                                .header(HttpHeaders.AUTHORIZATION, basic(username, password)))
                        .andExpect(status().isOk());

                securePerform(delete("/auth-user/{id}", id))
                        .andExpect(status().isOk());

                mvc.perform(get("/auth/check")
                                .header(HttpHeaders.AUTHORIZATION, basic(username, password)))
                        .andExpect(status().isUnauthorized());
            }
        };
    }

    @Test
    void deleteAuthUser__otherAsTeacher__invalid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                long id = ef.createAuthUser();
                securePerform(delete("/auth-user/{id}", id))
                        .andExpect(status().isForbidden());
            }
        };
    }

    @Test
    void deleteAuthUser__withCourseToken__invalid() {
        new WithCourseToken() {
            @Override
            void run() throws Exception {
                long id = ef.createAuthUser();
                securePerform(delete("/auth-user/{id}", id))
                        .andExpect(status().isForbidden());
            }
        };
    }

    @Test
    void deleteAuthUser__notFound__invalid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                long id = ef.createAuthUser();
                securePerform(delete("/auth-user/{id}", id + 1000))
                        .andExpect(status().isNotFound());
            }
        };
    }

    @Test
    void deleteAuthUser__notAuthenticated__invalid() throws Exception {
        long id = ef.createAuthUser();
        mvc.perform(delete("/auth-user/{id}", id + 1000))
                .andExpect(status().isUnauthorized());
    }
//
//    // ================================================================================================================

    ResultMatcher getResultMatcherForUserInfo(String prefix, UserRole userRole, Long userInfoId) {
        return switch (userRole) {
            case ADMIN -> null;
            case TEACHER -> jsonPath(prefix + ".employee").value(userInfoId);
            case HEADMAN -> jsonPath(prefix + ".student").value(userInfoId);
        };
    }

    UserRole getOtherUserRole(UserRole... prev) {
        List<UserRole> allowedRoles = new ArrayList<>(List.of(UserRole.values()));
        for (UserRole userRole : prev) allowedRoles.remove(userRole);
        if (allowedRoles.size() == 0) throw new IllegalArgumentException("excluded all UserRoles");
        int pick = faker.random().nextInt(allowedRoles.size());
        return allowedRoles.get(pick);
    }

    ResultMatcher[] getAuthUserMatchers(Long id, String username, UserRole userRole, Long userInfoId) {
        return getAuthUserMatchers("$", id, username, userRole, userInfoId);
    }

    ResultMatcher[] getAuthUserMatchers(String prefix, String username, UserRole userRole, Long userInfoId) {
        return getAuthUserMatchers(prefix, null, username, userRole, userInfoId);
    }

    ResultMatcher[] getAuthUserMatchers(String prefix, Long id, String username, UserRole userRole, Long userInfoId) {
        return Stream.of(
                        id != null ?
                                jsonPath(prefix + ".id").value(id) :
                                jsonPath(prefix + ".id").isNumber(),
                        jsonPath(prefix + ".username").value(username),
                        jsonPath(prefix + ".password").doesNotExist(),
                        jsonPath(prefix + ".userRole").value(userRole.name()),
                        getResultMatcherForUserInfo(prefix, userRole, userInfoId)
                )
                .filter(Objects::nonNull)
                .toArray(ResultMatcher[]::new);
    }
}
