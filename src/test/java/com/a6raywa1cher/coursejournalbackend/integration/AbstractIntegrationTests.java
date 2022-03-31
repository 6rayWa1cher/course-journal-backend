package com.a6raywa1cher.coursejournalbackend.integration;

import com.a6raywa1cher.coursejournalbackend.EntityFactory;
import com.a6raywa1cher.coursejournalbackend.dto.CourseTokenDto;
import com.a6raywa1cher.coursejournalbackend.dto.CreateEditUserDto;
import com.a6raywa1cher.coursejournalbackend.model.UserRole;
import com.a6raywa1cher.coursejournalbackend.service.CourseTokenService;
import com.a6raywa1cher.coursejournalbackend.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javafaker.Faker;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import static com.a6raywa1cher.coursejournalbackend.TestUtils.basic;
import static com.a6raywa1cher.coursejournalbackend.TestUtils.ctbearer;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@Import(IntegrationTestConfiguration.class)
public abstract class AbstractIntegrationTests {
    protected static final String ADMIN_USERNAME = "admin";
    protected static final String ADMIN_PASSWORD = "admin";
    protected static final String USERNAME = "leopold";
    protected static final String PASSWORD = "guysletsbefriends";

    @Autowired
    protected Faker faker;

    @Autowired
    protected EntityFactory ef;

    @Autowired
    protected MockMvc mvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected UserService userService;

    @Autowired
    protected TransactionTemplate transactionTemplate;

    @Autowired
    private CourseTokenService courseTokenService;

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

    abstract class WithCourseToken {
        private String token;
        private Long courseId;
        private Long tokenId;
        private Long ownerId;

        public WithCourseToken() {
            this(ef.createCourse(), true);
        }

        public WithCourseToken(long userId) {
            this(ef.createCourse(userId), true);
        }

        public WithCourseToken(String token) {
            CourseTokenDto courseTokenDto = courseTokenService.findByToken(token).orElseThrow();
            this.token = token;
            this.tokenId = courseTokenDto.getId();
            this.courseId = courseTokenDto.getCourse();
            this.ownerId = courseTokenService.resolveToken(courseTokenDto.getToken()).getOwner();
            wrappedRun();
        }

        public WithCourseToken(long courseId, boolean create) {
            CourseTokenDto courseTokenDto;
            if (create) {
                courseTokenDto = courseTokenService.create(CourseTokenDto.builder()
                        .course(courseId)
                        .build());
            } else {
                courseTokenDto = courseTokenService.getByCourseId(courseId);
            }
            this.token = courseTokenDto.getToken();
            this.tokenId = courseTokenDto.getId();
            this.courseId = courseTokenDto.getCourse();
            this.ownerId = courseTokenService.resolveToken(courseTokenDto.getToken()).getOwner();
            wrappedRun();
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
                            .header(AUTHORIZATION, ctbearer(token))
            );
        }


        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }

        public Long getCourseId() {
            return courseId;
        }

        public void setCourseId(Long courseId) {
            this.courseId = courseId;
        }

        public Long getTokenId() {
            return tokenId;
        }

        public void setTokenId(Long tokenId) {
            this.tokenId = tokenId;
        }

        public Long getOwnerId() {
            return ownerId;
        }

        public void setOwnerId(Long ownerId) {
            this.ownerId = ownerId;
        }
    }
}
