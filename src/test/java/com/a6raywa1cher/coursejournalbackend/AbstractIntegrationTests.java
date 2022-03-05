package com.a6raywa1cher.coursejournalbackend;

import com.a6raywa1cher.coursejournalbackend.dto.CreateEditUserDto;
import com.a6raywa1cher.coursejournalbackend.model.UserRole;
import com.a6raywa1cher.coursejournalbackend.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javafaker.Faker;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import static com.a6raywa1cher.coursejournalbackend.TestUtils.basic;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public abstract class AbstractIntegrationTests {
    protected static final String ADMIN_USERNAME = "admin";
    protected static final String ADMIN_PASSWORD = "admin";
    protected static final String USERNAME = "leopold";
    protected static final String PASSWORD = "guysletsbefriends";
    protected final Faker faker = new Faker();

    @Autowired
    protected MockMvc mvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected UserService userService;

    @Autowired
    protected TransactionTemplate transactionTemplate;

    @Test
    void contextLoads() {
    }

    abstract class WithUser {
        private String username;
        private String password;

        public WithUser(String username, String password, boolean create) {
            this.username = username;
            this.password = password;
            if (create) {
                userService.createUser(CreateEditUserDto.builder()
                        .username(username)
                        .password(password)
                        .userRole(UserRole.TEACHER)
                        .build());
            }
            this.wrappedRun();
        }

        public WithUser(String username, String password) {
            this(username, password, true);
        }

        private void wrappedRun() {
            try {
                run();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        abstract void run() throws Exception;

        protected ResultActions securePerform(MockHttpServletRequestBuilder builder) throws Exception {
            return mvc.perform(
                    builder
                            .header(AUTHORIZATION, basic(username, password))
            );
        }

        protected String getId() {
            try {
                String content = securePerform(get("/auth/user")).andExpect(status().isOk())
                        .andReturn().getResponse().getContentAsString();
                int id = JsonPath.read(content, "$.id");
                return Integer.toString(id);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        protected long getIdAsLong() {
            return Long.parseLong(getId());
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }
}
