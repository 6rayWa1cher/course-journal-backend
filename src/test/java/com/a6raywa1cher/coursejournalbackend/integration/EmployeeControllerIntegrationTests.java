package com.a6raywa1cher.coursejournalbackend.integration;

import com.a6raywa1cher.coursejournalbackend.RequestContext;
import com.a6raywa1cher.coursejournalbackend.dto.EmployeeDto;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultMatcher;

import java.util.function.Function;

import static com.a6raywa1cher.coursejournalbackend.TestUtils.getIdFromResult;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
public class EmployeeControllerIntegrationTests extends AbstractIntegrationTests {
    RequestContext<Long> createGetUserListContext() {
        String firstName = faker.name().firstName();
        String middleName = faker.name().firstName();
        String lastName = faker.name().lastName();
        String department = faker.university().name();

        long id = employeeService.create(EmployeeDto.builder()
                .firstName(firstName)
                .middleName(middleName)
                .lastName(lastName)
                .department(department)
                .build()).getId();

        Function<String, ResultMatcher[]> matchers = prefix ->
                getEmployeeMatchers(prefix, id, firstName, middleName, lastName, department);

        return RequestContext.<Long>builder()
                .request(id)
                .matchersSupplier(matchers)
                .build();
    }

    @Test
    void getUserList__asAdmin__valid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                var ctx1 = createGetUserListContext();
                var ctx2 = createGetUserListContext();

                securePerform(get("/employees/")
                        .param("sort", "id,desc"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.totalElements").value(2))
                        .andExpectAll(ctx2.getMatchers("$.content[0]"))
                        .andExpectAll(ctx1.getMatchers("$.content[1]"));
            }
        };
    }

    @Test
    void getUserList__asTeacher__valid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                var ctx1 = createGetUserListContext();
                var ctx2 = createGetUserListContext();

                securePerform(get("/employees/")
                        .param("sort", "id,desc"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.totalElements").value(3))
                        .andExpectAll(ctx2.getMatchers("$.content[0]"))
                        .andExpectAll(ctx1.getMatchers("$.content[1]"))
                        .andExpect(jsonPath("$.content[2].id").value(getSelfEmployeeIdAsLong()));
            }
        };
    }

    @Test
    void getUserList__withCourseToken__valid() {
        new WithCourseToken() {
            @Override
            void run() throws Exception {
                var ctx1 = createGetUserListContext();
                var ctx2 = createGetUserListContext();

                securePerform(get("/employees/")
                        .param("sort", "id,desc"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.totalElements").value(3))
                        .andExpectAll(ctx2.getMatchers("$.content[0]"))
                        .andExpectAll(ctx1.getMatchers("$.content[1]"));
            }
        };
    }

    @Test
    void getUserList__notAuthenticated__invalid() throws Exception {
        mvc.perform(get("/employees/"))
                .andExpect(status().isUnauthorized());
    }

    // ================================================================================================================

    RequestContext<Long> createGetUserByIdContext() {
        return createGetUserListContext();
    }

    @Test
    void getUserById__asAdmin__valid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                var ctx = createGetUserByIdContext();
                long id = ctx.getRequest();

                securePerform(get("/employees/{id}", id))
                        .andExpect(status().isOk())
                        .andExpectAll(ctx.getMatchers());
            }
        };
    }

    @Test
    void getUserById__asTeacher__valid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                var ctx = createGetUserByIdContext();
                long id = ctx.getRequest();

                securePerform(get("/employees/{id}", id))
                        .andExpect(status().isOk())
                        .andExpectAll(ctx.getMatchers());
            }
        };
    }

    @Test
    void getUserById__withCourseToken__valid() {
        new WithCourseToken() {
            @Override
            void run() throws Exception {
                var ctx = createGetUserByIdContext();
                long id = ctx.getRequest();

                securePerform(get("/employees/{id}", id))
                        .andExpect(status().isOk())
                        .andExpectAll(ctx.getMatchers());
            }
        };
    }

    @Test
    void getUserById__notAuthenticated__invalid() throws Exception {
        var ctx = createGetUserByIdContext();
        long id = ctx.getRequest();

        mvc.perform(get("/employees/{id}", id))
                .andExpect(status().isUnauthorized());
    }

    // ================================================================================================================
    RequestContext<ObjectNode> getCreateEmployeeContext(String firstName, String middleName,
                                                        String lastName, String department) {
        ObjectNode objectNode = objectMapper.createObjectNode()
                .put("firstName", firstName)
                .put("middleName", middleName)
                .put("lastName", lastName)
                .put("department", department);

        Function<String, ResultMatcher[]> matchers = prefix ->
                getEmployeeMatchers(prefix, firstName, middleName, lastName, department);

        return RequestContext.<ObjectNode>builder()
                .request(objectNode)
                .matchersSupplier(matchers)
                .build();
    }

    RequestContext<ObjectNode> getCreateEmployeeContext() {
        String firstName = faker.name().firstName();
        String middleName = faker.random().nextBoolean() ? faker.name().firstName() : null;
        String lastName = faker.name().lastName();
        String department = faker.university().name();

        return getCreateEmployeeContext(firstName, middleName, lastName, department);
    }

    @Test
    void createEmployee__asAdmin__valid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                var ctx = getCreateEmployeeContext();
                ObjectNode request = ctx.getRequest();

                MvcResult mvcResult = securePerform(post("/employees/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isCreated())
                        .andExpectAll(ctx.getMatchers())
                        .andReturn();

                long id = getIdFromResult(mvcResult);

                securePerform(get("/employees/{id}", id))
                        .andExpect(status().isOk())
                        .andExpectAll(ctx.getMatchers());
            }
        };
    }

    @Test
    void createEmployee__asTeacher__invalid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                var ctx = getCreateEmployeeContext();
                ObjectNode request = ctx.getRequest();

                securePerform(post("/employees/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isForbidden());
            }
        };
    }

    @Test
    void createEmployee__withCourseToken__invalid() {
        new WithCourseToken() {
            @Override
            void run() throws Exception {
                var ctx = getCreateEmployeeContext();
                ObjectNode request = ctx.getRequest();

                securePerform(post("/employees/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isForbidden());
            }
        };
    }

    @Test
    void createEmployee__conflict__invalid() {
        String firstName = faker.name().firstName();
        String middleName = faker.random().nextBoolean() ? faker.name().firstName() : null;
        String lastName = faker.name().lastName();
        String department = faker.university().name();
        employeeService.create(EmployeeDto.builder()
                .firstName(firstName)
                .middleName(middleName)
                .lastName(lastName)
                .department(department)
                .build());

        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                var ctx = getCreateEmployeeContext(firstName, middleName, lastName, department);
                ObjectNode request = ctx.getRequest();

                securePerform(post("/employees/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isConflict());
            }
        };
    }

    @Test
    void createEmployee__notAuthenticated__invalid() throws Exception {
        var ctx = getCreateEmployeeContext();
        ObjectNode request = ctx.getRequest();

        mvc.perform(post("/employees/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                .andExpect(status().isUnauthorized());
    }

    // ================================================================================================================

    RequestContext<ObjectNode> getUpdateEmployeeContext(long id, String firstName, String middleName, String lastName,
                                                        String department) {
        ObjectNode objectNode = objectMapper.createObjectNode()
                .put("firstName", firstName)
                .put("middleName", middleName)
                .put("lastName", lastName)
                .put("department", department);

        Function<String, ResultMatcher[]> matchers = prefix ->
                getEmployeeMatchers(prefix, id, firstName, middleName, lastName, department);

        return RequestContext.<ObjectNode>builder()
                .request(objectNode)
                .matchersSupplier(matchers)
                .build();
    }

    RequestContext<ObjectNode> getUpdateEmployeeContext(long id) {
        String firstName = faker.name().firstName();
        String middleName = faker.random().nextBoolean() ? faker.name().firstName() : null;
        String lastName = faker.name().lastName();
        String department = faker.university().name();

        return getUpdateEmployeeContext(id, firstName, middleName, lastName, department);
    }

    @Test
    void updateEmployee__self__invalid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                long employeeId = getSelfEmployeeIdAsLong();
                var ctx = getUpdateEmployeeContext(employeeId);
                ObjectNode request = ctx.getRequest();

                securePerform(put("/employees/{id}", employeeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isForbidden());
            }
        };
    }

    @Test
    void updateEmployee__otherAsAdmin__valid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                long employeeId = ef.createEmployee();
                var ctx = getUpdateEmployeeContext(employeeId);
                ObjectNode request = ctx.getRequest();

                securePerform(put("/employees/{id}", employeeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isOk())
                        .andExpectAll(ctx.getMatchers());

                securePerform(get("/employees/{id}", employeeId))
                        .andExpect(status().isOk())
                        .andExpectAll(ctx.getMatchers());
            }
        };
    }

    @Test
    void updateEmployee__otherAsTeacher__invalid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                long employeeId = ef.createEmployee();
                var ctx = getUpdateEmployeeContext(employeeId);
                ObjectNode request = ctx.getRequest();

                securePerform(put("/employees/{id}", employeeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isForbidden());
            }
        };
    }

    @Test
    void updateEmployee__withCourseToken__invalid() {
        long employeeId = ef.createEmployee();

        new WithCourseToken(employeeId) {
            @Override
            void run() throws Exception {
                var ctx = getUpdateEmployeeContext(employeeId);
                ObjectNode request = ctx.getRequest();

                securePerform(put("/employees/{id}", employeeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isForbidden());
            }
        };
    }

    @Test
    void updateEmployee__conflict__invalid() {
        String firstName = faker.name().firstName();
        String middleName = faker.random().nextBoolean() ? faker.name().firstName() : null;
        String lastName = faker.name().lastName();
        String department = faker.university().name();
        employeeService.create(EmployeeDto.builder()
                .firstName(firstName)
                .middleName(middleName)
                .lastName(lastName)
                .department(department)
                .build());

        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                long employeeId = ef.createEmployee();
                var ctx = getUpdateEmployeeContext(employeeId, firstName, middleName,
                        lastName, department);
                ObjectNode request = ctx.getRequest();

                securePerform(put("/employees/{id}", employeeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isConflict());
            }
        };
    }

    @Test
    void updateEmployee__notAuthenticated__invalid() throws Exception {
        long employeeId = ef.createEmployee();
        var ctx = getUpdateEmployeeContext(employeeId);
        ObjectNode request = ctx.getRequest();

        mvc.perform(put("/employees/{id}", employeeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                .andExpect(status().isUnauthorized());
    }

    // ================================================================================================================

    @Test
    void patchEmployee__self__invalid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                long id = getSelfEmployeeIdAsLong();
                String newFirstName = faker.name().firstName();
                ObjectNode request = objectMapper.createObjectNode()
                        .put("firstName", newFirstName);

                securePerform(patch("/employees/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isForbidden());
            }
        };
    }

    @Test
    void patchEmployee__otherAsAdmin__valid() {
        String prevFirstName = faker.name().firstName();
        String middleName = faker.random().nextBoolean() ? faker.name().firstName() : null;
        String lastName = faker.name().lastName();
        String department = faker.university().name();
        long id = employeeService.create(EmployeeDto.builder()
                .firstName(prevFirstName)
                .middleName(middleName)
                .lastName(lastName)
                .department(department)
                .build()).getId();

        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                String newFirstName = faker.name().firstName();
                ObjectNode request = objectMapper.createObjectNode()
                        .put("firstName", newFirstName);
                ResultMatcher[] matchers = getEmployeeMatchers(id, newFirstName, middleName, lastName, department);

                securePerform(patch("/employees/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isOk())
                        .andExpectAll(matchers);

                securePerform(get("/employees/{id}", id))
                        .andExpect(status().isOk())
                        .andExpectAll(matchers);
            }
        };
    }

    @Test
    void patchEmployee__otherAsTeacher__invalid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                long id = ef.createEmployee();
                String newFirstName = faker.name().firstName();
                ObjectNode request = objectMapper.createObjectNode()
                        .put("firstName", newFirstName);

                securePerform(patch("/employees/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isForbidden());
            }
        };
    }

    @Test
    void patchEmployee__withCourseToken__invalid() {
        new WithCourseToken() {
            @Override
            void run() throws Exception {
                long id = ef.createEmployee();
                String newFirstName = faker.name().firstName();
                ObjectNode request = objectMapper.createObjectNode()
                        .put("firstName", newFirstName);

                securePerform(patch("/employees/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isForbidden());
            }
        };
    }

    @Test
    void patchEmployee__conflict__invalid() {
        String firstName = faker.name().firstName();
        String middleName = faker.random().nextBoolean() ? faker.name().firstName() : null;
        String lastName = faker.name().lastName();
        String department = faker.university().name();
        employeeService.create(EmployeeDto.builder()
                .firstName(firstName)
                .middleName(middleName)
                .lastName(lastName)
                .department(department)
                .build());

        String prevFirstName = faker.name().firstName();
        long id = employeeService.create(EmployeeDto.builder()
                .firstName(prevFirstName)
                .middleName(middleName)
                .lastName(lastName)
                .department(department)
                .build()).getId();

        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                ObjectNode request = objectMapper.createObjectNode()
                        .put("firstName", firstName);

                securePerform(patch("/employees/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isConflict());
            }
        };
    }

    @Test
    void patchEmployee__notAuthenticated__invalid() throws Exception {
        long id = ef.createEmployee();
        String newFirstName = faker.name().firstName();
        ObjectNode request = objectMapper.createObjectNode()
                .put("firstName", newFirstName);

        mvc.perform(patch("/employees/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                .andExpect(status().isUnauthorized());
    }

    // ================================================================================================================

    @Test
    void deleteEmployee__self__invalid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                long employeeId = getSelfEmployeeIdAsLong();

                securePerform(delete("/employees/{id}", employeeId))
                        .andExpect(status().isForbidden());
            }
        };
    }

    @Test
    void deleteEmployee__otherAsAdmin__valid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                long employeeId = ef.createEmployee();

                securePerform(delete("/employees/{id}", employeeId))
                        .andExpect(status().isOk());

                securePerform(get("/employees/{id}", employeeId))
                        .andExpect(status().isNotFound());
            }
        };
    }

    @Test
    void deleteEmployee__otherAsTeacher__invalid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                long employeeId = ef.createEmployee();

                securePerform(delete("/employees/{id}", employeeId))
                        .andExpect(status().isForbidden());
            }
        };
    }

    @Test
    void deleteEmployee__withCourseToken__invalid() {
        new WithCourseToken() {
            @Override
            void run() throws Exception {
                long employeeId = ef.createEmployee();

                securePerform(delete("/employees/{id}", employeeId))
                        .andExpect(status().isForbidden());
            }
        };
    }

    @Test
    void deleteEmployee__notAuthenticated__invalid() throws Exception {
        long employeeId = ef.createEmployee();

        mvc.perform(delete("/employees/{id}", employeeId))
                .andExpect(status().isUnauthorized());
    }

    // ================================================================================================================

    ResultMatcher[] getEmployeeMatchers(String firstName, String middleName, String lastName, String department) {
        return getEmployeeMatchers("$", null, firstName, middleName, lastName, department);
    }

    ResultMatcher[] getEmployeeMatchers(String prefix, String firstName, String middleName, String lastName,
                                        String department) {
        return getEmployeeMatchers(prefix, null, firstName, middleName, lastName, department);
    }

    ResultMatcher[] getEmployeeMatchers(Long id, String firstName, String middleName, String lastName,
                                        String department) {
        return getEmployeeMatchers("$", id, firstName, middleName, lastName, department);
    }

    ResultMatcher[] getEmployeeMatchers(String prefix, Long id, String firstName, String middleName,
                                        String lastName, String department) {
        return new ResultMatcher[]{
                id != null ?
                        jsonPath(prefix + ".id").value(id) :
                        jsonPath(prefix + ".id").isNumber(),
                jsonPath(prefix + ".firstName").value(firstName),
                jsonPath(prefix + ".middleName").value(middleName),
                jsonPath(prefix + ".lastName").value(lastName),
                jsonPath(prefix + ".department").value(department)
        };
    }
}
