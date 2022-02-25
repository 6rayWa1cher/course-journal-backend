package com.a6raywa1cher.coursejournalbackend;

import com.a6raywa1cher.coursejournalbackend.dto.CreateEditUserDto;
import com.a6raywa1cher.coursejournalbackend.model.UserRole;
import com.a6raywa1cher.coursejournalbackend.model.repo.UserRepository;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.ResultMatcher;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
public class UserControllerIntegrationTests extends AbstractIntegrationTests {
    @Autowired
    UserRepository userRepository;

    @Test
    void getUsers__authenticated__valid() {
        String newUserUsername = "aatjw";
        userService.createUser(CreateEditUserDto.builder()
                .username(newUserUsername)
                .userRole(UserRole.TEACHER)
                .build());

        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                securePerform(get("/users/").queryParam("sort", "id,desc"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.totalElements").value(3))
                        .andExpect(jsonPath("$.content[0].username").value(USERNAME))
                        .andExpect(jsonPath("$.content[1].username").value(newUserUsername))
                        .andExpect(jsonPath("$.content[2].username").value(ADMIN_USERNAME));
            }
        };
    }

    @Test
    void getUsers__notAuthenticated__invalid() throws Exception {
        mvc.perform(get("/users/").queryParam("sort", "id,desc"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getUserById__authenticated__valid() {
        String newUserUsername = "aatjw";
        long userId = userService.createUser(CreateEditUserDto.builder()
                .username(newUserUsername)
                .userRole(UserRole.TEACHER)
                .build()).getId();

        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                securePerform(get("/users/{id}", userId))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.username").value(newUserUsername));
            }
        };
    }

    @Test
    void getUserById__notAuthenticated__invalid() throws Exception {
        long userId = userService.createUser(CreateEditUserDto.builder()
                .username(USERNAME)
                .userRole(UserRole.TEACHER)
                .build()).getId();

        mvc.perform(get("/users/{id}", userId))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getUserById__notExists__invalid() {
        long userId = userService.createUser(CreateEditUserDto.builder()
                .username(USERNAME)
                .userRole(UserRole.TEACHER)
                .build()).getId();

        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                securePerform(get("/users/{id}", userId + 1000))
                        .andExpect(status().isNotFound());
            }
        };
    }

    @Test
    void getUserByUsername__authenticated__valid() {
        String newUserUsername = "aatjw";
        userService.createUser(CreateEditUserDto.builder()
                .username(newUserUsername)
                .userRole(UserRole.TEACHER)
                .build());

        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                securePerform(get("/users/username/{username}", newUserUsername))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.username").value(newUserUsername));
            }
        };
    }

    @Test
    void getUserByUsername__notAuthenticated__invalid() throws Exception {
        userService.createUser(CreateEditUserDto.builder()
                .username(USERNAME)
                .userRole(UserRole.TEACHER)
                .build());

        mvc.perform(get("/users/username/{username}", USERNAME))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getUserByUsername__notExists__invalid() {
        userService.createUser(CreateEditUserDto.builder()
                .username(USERNAME)
                .userRole(UserRole.TEACHER)
                .build());

        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                securePerform(get("/users/username/{username}", USERNAME + "jiogre"))
                        .andExpect(status().isNotFound());
            }
        };
    }

    @Test
    void createUser__adminCreatingTeacher__valid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                String username = "abcdef";
                String password = "qwerty";
                String firstName = "cat";
                String lastName = "dog";
                String middleName = "bird";

                securePerform(post("/users/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.createObjectNode()
                                .put("username", username)
                                .put("password", password)
                                .put("userRole", "TEACHER")
                                .put("firstName", firstName)
                                .put("middleName", middleName)
                                .put("lastName", lastName)
                                .toString()))
                        .andExpect(status().isCreated())
                        .andExpectAll(
                                jsonPath("$.id").isNumber(),
                                jsonPath("$.username").value(username),
                                jsonPath("$.password").doesNotExist(),
                                jsonPath("$.userRole").value("TEACHER"),
                                jsonPath("$.firstName").value(firstName),
                                jsonPath("$.lastName").value(lastName),
                                jsonPath("$.middleName").value(middleName)
                        );

                securePerform(get("/users/"))
                        .andExpect(status().isOk())
                        .andExpect(content().string(containsString(username)));
            }
        };
    }

    @Test
    void createUser__adminCreatingAdmin__valid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                String username = "abcdef";
                String password = "qwerty";
                String firstName = "cat";
                String lastName = "dog";
                String middleName = "bird";

                securePerform(post("/users/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.createObjectNode()
                                .put("username", username)
                                .put("password", password)
                                .put("userRole", "ADMIN")
                                .put("firstName", firstName)
                                .put("middleName", middleName)
                                .put("lastName", lastName)
                                .toString()))
                        .andExpect(status().isCreated())
                        .andExpectAll(
                                jsonPath("$.id").isNumber(),
                                jsonPath("$.username").value(username),
                                jsonPath("$.password").doesNotExist(),
                                jsonPath("$.userRole").value("ADMIN"),
                                jsonPath("$.firstName").value(firstName),
                                jsonPath("$.lastName").value(lastName),
                                jsonPath("$.middleName").value(middleName)
                        );

                securePerform(get("/users/"))
                        .andExpect(status().isOk())
                        .andExpect(content().string(containsString(username)));
            }
        };
    }

    @Test
    void createUser__teacherCreating__invalid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                String username = "abcdef";
                String password = "qwerty";
                String firstName = "cat";
                String lastName = "dog";
                String middleName = "bird";

                securePerform(post("/users/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.createObjectNode()
                                .put("username", username)
                                .put("password", password)
                                .put("userRole", "ADMIN")
                                .put("firstName", firstName)
                                .put("middleName", middleName)
                                .put("lastName", lastName)
                                .toString()))
                        .andExpect(status().isForbidden());

                securePerform(post("/users/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.createObjectNode()
                                .put("username", username)
                                .put("password", password)
                                .put("userRole", "TEACHER")
                                .put("firstName", firstName)
                                .put("middleName", middleName)
                                .put("lastName", lastName)
                                .toString()))
                        .andExpect(status().isForbidden());

                securePerform(get("/users/"))
                        .andExpect(status().isOk())
                        .andExpect(content().string(not(containsString(username))));
            }
        };
    }

    @Test
    void createUser__conflictingUsernames__invalid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                securePerform(post("/users/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.createObjectNode()
                                .put("username", USERNAME)
                                .put("password", PASSWORD)
                                .put("userRole", "TEACHER")
                                .toString()))
                        .andExpect(status().isCreated());

                securePerform(post("/users/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.createObjectNode()
                                .put("username", USERNAME)
                                .put("password", PASSWORD)
                                .put("userRole", "TEACHER")
                                .toString()))
                        .andExpect(status().isConflict());
            }
        };
    }

    @Test
    void putUser__self__full__valid() {
        String newUsername = "abcdef";
        String newFirstName = "cat";
        String newLastName = "dog";
        String newPassword = "qwerty";
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                ResultMatcher[] userMatcher = {
                        jsonPath("$.id").isNumber(),
                        jsonPath("$.username").value(newUsername),
                        jsonPath("$.password").doesNotExist(),
                        jsonPath("$.userRole").value("TEACHER"),
                        jsonPath("$.firstName").value(newFirstName),
                        jsonPath("$.lastName").value(newLastName),
                        jsonPath("$.middleName").isEmpty()
                };

                securePerform(put("/users/{id}", getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.createObjectNode()
                                .put("username", newUsername)
                                .put("password", newPassword)
                                .put("userRole", "TEACHER")
                                .put("firstName", newFirstName)
                                .put("lastName", newLastName)
                                .toString()))
                        .andExpect(status().isOk())
                        .andExpectAll(
                                userMatcher
                        );

                setUsername(newUsername);
                setPassword(newPassword);

                securePerform(get("/users/{id}", getId()))
                        .andExpect(status().isOk())
                        .andExpectAll(userMatcher);
            }
        };
    }

    @Test
    void putUser__otherAsAdmin__valid() {
        String prevUsername = "kt34wo";
        String newUsername = "abcdef";
        String newFirstName = "cat";
        String newLastName = "dog";
        String newPassword = "qwerty";

        long userId = userService.createUser(
                CreateEditUserDto.builder()
                        .username(prevUsername)
                        .userRole(UserRole.TEACHER)
                        .build()
        ).getId();

        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                securePerform(put("/users/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.createObjectNode()
                                .put("username", newUsername)
                                .put("password", newPassword)
                                .put("userRole", "TEACHER")
                                .put("firstName", newFirstName)
                                .put("lastName", newLastName)
                                .toString()))
                        .andExpect(status().isOk())
                        .andExpectAll(
                                jsonPath("$.id").isNumber(),
                                jsonPath("$.username").value(newUsername),
                                jsonPath("$.password").doesNotExist(),
                                jsonPath("$.userRole").value("TEACHER"),
                                jsonPath("$.firstName").value(newFirstName),
                                jsonPath("$.lastName").value(newLastName),
                                jsonPath("$.middleName").isEmpty()
                        );
            }
        };
    }

    @Test
    void putUser__otherAsTeacher__invalid() {
        String prevUsername = "kt34wo";
        String newUsername = "abcdef";
        String newFirstName = "cat";
        String newLastName = "dog";
        String newPassword = "qwerty";

        long userId = userService.createUser(
                CreateEditUserDto.builder()
                        .username(prevUsername)
                        .userRole(UserRole.TEACHER)
                        .build()
        ).getId();

        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                securePerform(put("/users/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.createObjectNode()
                                .put("username", newUsername)
                                .put("password", newPassword)
                                .put("userRole", "TEACHER")
                                .put("firstName", newFirstName)
                                .put("lastName", newLastName)
                                .toString()))
                        .andExpect(status().isForbidden());
            }
        };
    }

    @Test
    void putUser__selfEscalation__invalid() {
        String newUsername = "abcdef";
        String newFirstName = "cat";
        String newLastName = "dog";
        String newPassword = "qwerty";
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                securePerform(put("/users/{id}", getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.createObjectNode()
                                .put("username", newUsername)
                                .put("password", newPassword)
                                .put("userRole", "ADMIN")
                                .put("firstName", newFirstName)
                                .put("lastName", newLastName)
                                .toString()))
                        .andExpect(status().isForbidden());
            }
        };
    }

    @Test
    void putUser__usernameConflict__invalid() {
        String newUsername = "abcdef";
        String newFirstName = "cat";
        String newLastName = "dog";
        String newPassword = "qwerty";

        userService.createUser(
                CreateEditUserDto.builder()
                        .username(newUsername)
                        .userRole(UserRole.TEACHER)
                        .build()
        );

        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                securePerform(put("/users/{id}", getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.createObjectNode()
                                .put("username", newUsername)
                                .put("password", newPassword)
                                .put("userRole", "TEACHER")
                                .put("firstName", newFirstName)
                                .put("lastName", newLastName)
                                .toString()))
                        .andExpect(status().isConflict());
            }
        };
    }

    @Test
    void patchUser__self__full__valid() {
        String newFirstName = "cat";
        String newLastName = "dog";
        String newPassword = "qwerty";
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                ResultMatcher[] userMatcher = {
                        jsonPath("$.id").isNumber(),
                        jsonPath("$.username").value(USERNAME),
                        jsonPath("$.password").doesNotExist(),
                        jsonPath("$.userRole").value("TEACHER"),
                        jsonPath("$.firstName").value(newFirstName),
                        jsonPath("$.lastName").value(newLastName),
                        jsonPath("$.middleName").isEmpty()
                };

                securePerform(patch("/users/{id}", getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.createObjectNode()
                                .put("password", newPassword)
                                .put("firstName", newFirstName)
                                .put("lastName", newLastName)
                                .toString()))
                        .andExpect(status().isOk())
                        .andExpectAll(
                                userMatcher
                        );

                setPassword(newPassword);

                securePerform(get("/users/{id}", getId()))
                        .andExpect(status().isOk())
                        .andExpectAll(userMatcher);
            }
        };
    }

    @Test
    void patchUser__otherAsAdmin__valid() {
        String prevUsername = "kt34wo";
        String newFirstName = "cat";
        String newLastName = "dog";
        String newPassword = "qwerty";

        long userId = userService.createUser(
                CreateEditUserDto.builder()
                        .username(prevUsername)
                        .userRole(UserRole.TEACHER)
                        .build()
        ).getId();

        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                securePerform(patch("/users/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.createObjectNode()
                                .put("password", newPassword)
                                .put("userRole", "TEACHER")
                                .put("firstName", newFirstName)
                                .put("lastName", newLastName)
                                .toString()))
                        .andExpect(status().isOk())
                        .andExpectAll(
                                jsonPath("$.id").isNumber(),
                                jsonPath("$.username").value(prevUsername),
                                jsonPath("$.password").doesNotExist(),
                                jsonPath("$.userRole").value("TEACHER"),
                                jsonPath("$.firstName").value(newFirstName),
                                jsonPath("$.lastName").value(newLastName),
                                jsonPath("$.middleName").isEmpty()
                        );
            }
        };
    }

    @Test
    void patchUser__otherAsTeacher__invalid() {
        String prevUsername = "kt34wo";
        String newFirstName = "cat";

        long userId = userService.createUser(
                CreateEditUserDto.builder()
                        .username(prevUsername)
                        .userRole(UserRole.TEACHER)
                        .build()
        ).getId();

        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                securePerform(patch("/users/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.createObjectNode()
                                .put("firstName", newFirstName)
                                .toString()))
                        .andExpect(status().isForbidden());
            }
        };
    }

    @Test
    void patchUser__selfEscalation__invalid() {
        String newFirstName = "cat";
        String newLastName = "dog";
        String newPassword = "qwerty";
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                securePerform(patch("/users/{id}", getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.createObjectNode()
                                .put("password", newPassword)
                                .put("userRole", "ADMIN")
                                .put("firstName", newFirstName)
                                .put("lastName", newLastName)
                                .toString()))
                        .andExpect(status().isForbidden());
            }
        };
    }

    @Test
    void patchUser__usernameConflict__invalid() {
        String newUsername = "abcdef";

        userService.createUser(
                CreateEditUserDto.builder()
                        .username(newUsername)
                        .userRole(UserRole.TEACHER)
                        .build()
        );

        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                securePerform(patch("/users/{id}", getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.createObjectNode()
                                .put("username", newUsername)
                                .toString()))
                        .andExpect(status().isConflict());
            }
        };
    }

    @Test
    void deleteUser__self__valid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                String id = getId();
                securePerform(delete("/users/{id}", id))
                        .andExpect(status().isOk());

                securePerform(get("/auth/check", id))
                        .andExpect(status().isUnauthorized());
            }
        };
    }

    @Test
    void deleteUser__otherAsAdmin__valid() {
        long userId = userService.createUser(
                CreateEditUserDto.builder()
                        .username(USERNAME)
                        .userRole(UserRole.TEACHER)
                        .build()
        ).getId();

        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                securePerform(delete("/users/{id}", userId))
                        .andExpect(status().isOk());

                securePerform(get("/users/{id}", userId))
                        .andExpect(status().isNotFound());
            }
        };
    }

    @Test
    void deleteUser__otherAsTeacher__invalid() {
        String otherUsername = "tfegbpijo";
        long userId = userService.createUser(
                CreateEditUserDto.builder()
                        .username(otherUsername)
                        .userRole(UserRole.TEACHER)
                        .build()
        ).getId();

        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                securePerform(delete("/users/{id}", userId))
                        .andExpect(status().isForbidden());
            }
        };
    }


    private int getUserIdByUsername(String username, WithUser withUser) throws Exception {
        String contentAsString = withUser.securePerform(get("/users/username/{username}", username))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(contentAsString, "$.id");
    }
}
