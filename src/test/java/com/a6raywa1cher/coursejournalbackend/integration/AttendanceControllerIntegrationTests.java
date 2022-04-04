package com.a6raywa1cher.coursejournalbackend.integration;

import com.a6raywa1cher.coursejournalbackend.RequestContext;
import com.a6raywa1cher.coursejournalbackend.TestUtils;
import com.a6raywa1cher.coursejournalbackend.dto.AttendanceDto;
import com.a6raywa1cher.coursejournalbackend.model.AttendanceType;
import com.a6raywa1cher.coursejournalbackend.service.AttendanceService;
import com.a6raywa1cher.coursejournalbackend.service.CourseService;
import com.a6raywa1cher.coursejournalbackend.service.StudentService;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.ResultMatcher;

import java.time.ZonedDateTime;
import java.util.function.Function;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
public class AttendanceControllerIntegrationTests extends AbstractIntegrationTests {
    @Autowired
    AttendanceService attendanceService;

    @Autowired
    CourseService courseService;

    @Autowired
    StudentService studentService;

    private long[] getNeededInfo(){
        long[] result;
        result = new long[2];
        result[0] = ef.createStudent();
        result[1] = ef.createCourse();
        return result;
    };

    @Test
    void getAttendanceById__self__valid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                ZonedDateTime attendedAt = ZonedDateTime.now();
                long courseId = ef.createCourse(getIdAsLong());
                long studentId = ef.createStudent(getIdAsLong());
                AttendanceType attendanceType = TestUtils.randomAttendanceType();

                long id = attendanceService.create(AttendanceDto.builder()
                        .student(studentId)
                        .course(courseId)
                        .attendedAt(attendedAt)
                        .attendanceType(attendanceType)
                        .build()).getId();

                ResultMatcher[] matchers = {
                        jsonPath("$.id").value(id),
                        jsonPath("$.student").value(studentId),
                        jsonPath("$.attendedAt").value(new TestUtils.DateMatcher(attendedAt)),
                        jsonPath("$.attendanceType").value(attendanceType)
                };

                securePerform(get("/attendance/{id}", id))
                        .andExpect(status().isOk())
                        .andExpectAll(matchers);
            }
        };
    }

    @Test
    void getAttendanceById__otherAsAdmin__valid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                ZonedDateTime attendedAt = ZonedDateTime.now();
                long courseId = ef.createCourse(getIdAsLong());
                long studentId = ef.createStudent(getIdAsLong());
                AttendanceType attendanceType = TestUtils.randomAttendanceType();

                long id = attendanceService.create(AttendanceDto.builder()
                        .student(studentId)
                        .course(courseId)
                        .attendedAt(attendedAt)
                        .attendanceType(attendanceType)
                        .build()).getId();

                ResultMatcher[] matchers = {
                        jsonPath("$.id").value(id),
                        jsonPath("$.student").value(studentId),
                        jsonPath("$.attendedAt").value(new TestUtils.DateMatcher(attendedAt)),
                        jsonPath("$.attendanceType").value(attendanceType)
                };

                securePerform(get("/attendance/{id}", id))
                        .andExpect(status().isOk())
                        .andExpectAll(matchers);
            }
        };
    }

    @Test
    void getAttendanceById__otherAsTeacher__invalid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws  Exception {
                ZonedDateTime attendedAt = ZonedDateTime.now();
                long courseId = ef.createCourse(getIdAsLong());
                long studentId = ef.createStudent(getIdAsLong());
                AttendanceType attendanceType = TestUtils.randomAttendanceType();

                long id = attendanceService.create(AttendanceDto.builder()
                        .student(studentId)
                        .course(courseId)
                        .attendedAt(attendedAt)
                        .attendanceType(attendanceType)
                        .build()).getId();

                securePerform(get("/attendance/{id}", id))
                        .andExpect(status().isForbidden());
            }
        };
    }

    @Test
    void getAttendanceById__notAuthenticated__invalid() throws Exception {
        ZonedDateTime attendedAt = ZonedDateTime.now();
        long courseId = ef.createCourse();
        long studentId = ef.createStudent();
        AttendanceType attendanceType = TestUtils.randomAttendanceType();

        long id = attendanceService.create(AttendanceDto.builder()
                .student(studentId)
                .course(courseId)
                .attendedAt(attendedAt)
                .attendanceType(attendanceType)
                .build()).getId();

        mvc.perform(get("/attendance/{id}", id))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getAttendanceById__notExists__invalid() {
        ZonedDateTime attendedAt = ZonedDateTime.now();
        long courseId = ef.createCourse();
        long studentId = ef.createStudent();
        AttendanceType attendanceType = TestUtils.randomAttendanceType();

        long id = attendanceService.create(AttendanceDto.builder()
                .student(studentId)
                .course(courseId)
                .attendedAt(attendedAt)
                .attendanceType(attendanceType)
                .build()).getId();

        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                securePerform(get("/attendance/{id}", id + 1000))
                        .andExpect(status().isNotFound());
            }
        };
    }

    // =================================================================================================================

    @Test
    void getAttendanceByStudentId__self__valid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                ZonedDateTime attendedAt1 = ZonedDateTime.now();
                ZonedDateTime attendedAt2 = attendedAt1.plusYears(10);

                AttendanceType attendanceType = TestUtils.randomAttendanceType();

                long studentId1 = ef.createStudent(getIdAsLong());
                long studentId2 = ef.createStudent(getIdAsLong());

                long courseId = ef.createCourse(getIdAsLong());

                attendanceService.create(AttendanceDto.builder()
                        .student(studentId1)
                        .course(courseId)
                        .attendedAt(attendedAt1)
                        .attendanceType(attendanceType)
                        .build());

                attendanceService.create(AttendanceDto.builder()
                        .student(studentId1)
                        .course(courseId)
                        .attendedAt(attendedAt2)
                        .attendanceType(attendanceType)
                        .build());

                attendanceService.create(AttendanceDto.builder()
                        .student(studentId2)
                        .course(courseId)
                        .attendedAt(attendedAt1)
                        .attendanceType(attendanceType)
                        .build());

                securePerform(get("/attendance/student/{id}", studentId1)
                        .queryParam("sort", "attendedAt,asc"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.totalElements").value(2))
                        .andExpect(jsonPath("$.content[0].attendedAt").value(new TestUtils.DateMatcher(attendedAt1)))
                        .andExpect(jsonPath("$.content[1].attendedAt").value(new TestUtils.DateMatcher(attendedAt2)));
            }
        };
    }

    @Test
    void getAttendanceByStudentId__otherAsAdmin__valid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                ZonedDateTime attendedAt1 = ZonedDateTime.now();
                ZonedDateTime attendedAt2 = attendedAt1.plusYears(10);

                AttendanceType attendanceType = TestUtils.randomAttendanceType();

                long studentId1 = ef.createStudent(getIdAsLong());
                long studentId2 = ef.createStudent(getIdAsLong());

                long courseId = ef.createCourse(getIdAsLong());

                attendanceService.create(AttendanceDto.builder()
                        .student(studentId1)
                        .course(courseId)
                        .attendedAt(attendedAt1)
                        .attendanceType(attendanceType)
                        .build());

                attendanceService.create(AttendanceDto.builder()
                        .student(studentId1)
                        .course(courseId)
                        .attendedAt(attendedAt2)
                        .attendanceType(attendanceType)
                        .build());

                attendanceService.create(AttendanceDto.builder()
                        .student(studentId2)
                        .course(courseId)
                        .attendedAt(attendedAt1)
                        .attendanceType(attendanceType)
                        .build());

                securePerform(get("/attendance/student/{id}", studentId1)
                        .queryParam("sort", "attendedAt,asc"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.totalElements").value(2))
                        .andExpect(jsonPath("$.content[0].attendedAt").value(new TestUtils.DateMatcher(attendedAt1)))
                        .andExpect(jsonPath("$.content[1].attendedAt").value(new TestUtils.DateMatcher(attendedAt2)));
            }
        };
    }

    @Test
    void getAttendanceByStudentId__otherAsTeacher__invalid() {
        long id = ef.createStudent();

        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                securePerform(get("/attendance/student/{id}", id))
                    .andExpect(status().isForbidden());
            }
        };
    }

    @Test
    void getAttendanceByStudent__notAuthenticated__invalid() throws Exception {
        long id = ef.createStudent();
        mvc.perform(get("/attendance/student/{id}", id)).andExpect(status().isUnauthorized());
    }

    // =================================================================================================================

    @Test
    void getAttendanceByCourse__self__valid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                ZonedDateTime attendedAt1 = ZonedDateTime.now();
                ZonedDateTime attendedAt2 = attendedAt1.plusYears(10);

                AttendanceType attendanceType = TestUtils.randomAttendanceType();

                long studentId = ef.createStudent(getIdAsLong());

                long courseId1 = ef.createCourse(getIdAsLong());
                long courseId2 = ef.createCourse(getIdAsLong());

                attendanceService.create(AttendanceDto.builder()
                        .student(studentId)
                        .course(courseId1)
                        .attendedAt(attendedAt1)
                        .attendanceType(attendanceType)
                        .build());

                attendanceService.create(AttendanceDto.builder()
                        .student(studentId)
                        .course(courseId1)
                        .attendedAt(attendedAt2)
                        .attendanceType(attendanceType)
                        .build());

                attendanceService.create(AttendanceDto.builder()
                        .student(studentId)
                        .course(courseId2)
                        .attendedAt(attendedAt1)
                        .attendanceType(attendanceType)
                        .build());

                securePerform(get("/attendance/course/{id}", courseId1)
                        .queryParam("sort", "attendedAt,asc"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.totalElements").value(2))
                        .andExpect(jsonPath("$.content[0].attendedAt").value(new TestUtils.DateMatcher(attendedAt1)))
                        .andExpect(jsonPath("$.content[1].attendedAt").value(new TestUtils.DateMatcher(attendedAt2)));
            }
        };
    }

    @Test
    void getAttendanceByCourse__otherAsAdmin__valid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD) {
            @Override
            void run() throws Exception {
                ZonedDateTime attendedAt1 = ZonedDateTime.now();
                ZonedDateTime attendedAt2 = attendedAt1.plusYears(10);

                AttendanceType attendanceType = TestUtils.randomAttendanceType();

                long studentId = ef.createStudent(getIdAsLong());

                long courseId1 = ef.createCourse(getIdAsLong());
                long courseId2 = ef.createCourse(getIdAsLong());

                attendanceService.create(AttendanceDto.builder()
                        .student(studentId)
                        .course(courseId1)
                        .attendedAt(attendedAt1)
                        .attendanceType(attendanceType)
                        .build());

                attendanceService.create(AttendanceDto.builder()
                        .student(studentId)
                        .course(courseId1)
                        .attendedAt(attendedAt2)
                        .attendanceType(attendanceType)
                        .build());

                attendanceService.create(AttendanceDto.builder()
                        .student(studentId)
                        .course(courseId2)
                        .attendedAt(attendedAt1)
                        .attendanceType(attendanceType)
                        .build());

                securePerform(get("/attendance/course/{id}", courseId1)
                        .queryParam("sort", "attendedAt,asc"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.totalElements").value(2))
                        .andExpect(jsonPath("$.content[0].attendedAt").value(new TestUtils.DateMatcher(attendedAt1)))
                        .andExpect(jsonPath("$.content[1].attendedAt").value(new TestUtils.DateMatcher(attendedAt2)));
            }
        };
    }

    @Test
    void getAttendanceByCourse__otherAsTeacher__invalid() {
        long id = ef.createCourse();

        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                securePerform(get("/attendance/course/{id}", id))
                        .andExpect(status().isForbidden());
            }
        };
    }

    @Test
    void getAttendanceByCourse__notAuthenticated__invalid() throws Exception {
        long id = ef.createCourse();

        mvc.perform(get("/attendance/course/{id}", id))
                .andExpect(status().isForbidden());
    }

    // =================================================================================================================

    RequestContext<ObjectNode> getCreateAttendanceRequest(long studentId, long courseId) {
        ZonedDateTime attendedAt = ZonedDateTime.now();

        AttendanceType attendanceType = TestUtils.randomAttendanceType();

        ObjectNode request = objectMapper.createObjectNode()
                .put("student", studentId)
                .put("course", courseId)
                .put("attendedAt", String.valueOf(attendedAt))
                .put("attendanceType", String.valueOf(attendanceType));

        Function<String, ResultMatcher[]> matchers = prefix -> new ResultMatcher[]{
                jsonPath("$.id").isNumber(),
                jsonPath("$.student").value(studentId),
                jsonPath("$.course").value(courseId),
                jsonPath("$.attendedAt").value(new TestUtils.DateMatcher(attendedAt)),
                jsonPath("$.attendanceType").value(attendanceType)
        };

        return new RequestContext<>(request, matchers);
    }

    @Test
    void createAttendance__self__valid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                long studentId = ef.createStudent(getIdAsLong());
                long courseId = ef.createCourse(getIdAsLong());
                var context = getCreateAttendanceRequest(studentId, courseId);

                ObjectNode request = context.getRequest();

                securePerform(post("/attendance/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isCreated())
                        .andExpectAll(context.getMatchers());
            }
        };
    }

    @Test
    void createAttendance__otherAsAdmin__valid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                long studentId = ef.createStudent(getIdAsLong());
                long courseId = ef.createCourse(getIdAsLong());
                var context = getCreateAttendanceRequest(studentId, courseId);

                ObjectNode request = context.getRequest();

                securePerform(post("/attendance/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isCreated())
                        .andExpectAll(context.getMatchers());
            }
        };
    }

    @Test
    void createAttendance__otherAsTeacher__invalid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                long studentId = ef.createStudent();
                long courseId = ef.createCourse();

                var context = getCreateAttendanceRequest(studentId, courseId);

                ObjectNode request = context.getRequest();

                securePerform(post("/attendance/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isForbidden());
            }
        };
    }

    @Test
    void createAttendance__notAuthenticated__invalid() throws Exception {
        long studentId = ef.createStudent();
        long courseId = ef.createCourse();

        var context = getCreateAttendanceRequest(studentId, courseId);

        ObjectNode request = context.getRequest();

        mvc.perform(post("/attendance/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request.toString()))
                .andExpect(status().isUnauthorized());
    }

    // =================================================================================================================

    RequestContext<ObjectNode> getBatchCreateAttendanceRequest(long studentId, long courseId) {
        ZonedDateTime attendedAt1 = ZonedDateTime.now();
        AttendanceType attendanceType1 = TestUtils.randomAttendanceType();

        ZonedDateTime attendedAt2 = attendedAt1.plusYears(10);
        AttendanceType attendanceType2 = TestUtils.randomAttendanceType();

        ObjectNode request = objectMapper.createObjectNode()
                .put("student", studentId)
                .put("course", courseId);

        request.putArray("attendances")
                .add(objectMapper.createObjectNode()
                        .put("attendedAt", String.valueOf(attendedAt1))
                        .put("attendanceType", String.valueOf(attendanceType1)))
                .add(objectMapper.createObjectNode()
                        .put("attendedAt", String.valueOf(attendedAt2))
                        .put("attendanceType", String.valueOf(attendanceType2)));
        return new RequestContext<>(request, prefix -> new ResultMatcher[]{
                jsonPath(prefix + "[0].attendedAt").value(new TestUtils.DateMatcher(attendedAt1)),
                jsonPath(prefix + "[0].attendedAt").value(new TestUtils.DateMatcher(attendedAt1)),
                jsonPath(prefix + "[0].attendedAt").value(new TestUtils.DateMatcher(attendedAt1)),
                jsonPath(prefix + "[1].attendedAt").value(new TestUtils.DateMatcher(attendedAt2)),
                jsonPath(prefix + "[1].attendedAt").value(new TestUtils.DateMatcher(attendedAt2)),
                jsonPath(prefix + "[1].attendedAt").value(new TestUtils.DateMatcher(attendedAt2))
        });
    };

    @Test
    void batchCreateAttendance__self__valid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                long studentId = ef.createStudent(getIdAsLong());
                long courseId = ef.createCourse(getIdAsLong());
                RequestContext<ObjectNode> context = getBatchCreateAttendanceRequest(studentId, courseId);

                ObjectNode request = context.getRequest();

                securePerform(post("/attendanse/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isCreated())
                        .andExpect(jsonPath("$", hasSize(2)))
                        .andExpectAll(context.getMatchers());

                securePerform(get("/attendance/student/{id}", studentId)
                        .queryParam("sort", "attendedAt,asc"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.totalElements").value(2))
                        .andExpectAll(context.getMatchers("$.content"));

                securePerform(get("/attendance/course/{id}", courseId)
                        .queryParam("sort", "attendedAt,asc"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.totalElements").value(2))
                        .andExpectAll(context.getMatchers("$.content"));
            }
        };
    }

    @Test
    void batchCreateAttendance__otherAsAdmin__valid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                long studentId = ef.createStudent(getIdAsLong());
                long courseId = ef.createCourse(getIdAsLong());
                RequestContext<ObjectNode> context = getBatchCreateAttendanceRequest(studentId, courseId);

                ObjectNode request = context.getRequest();

                securePerform(post("/attendanse/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isCreated())
                        .andExpect(jsonPath("$", hasSize(2)))
                        .andExpectAll(context.getMatchers());

                securePerform(get("/attendance/student/{id}", studentId)
                        .queryParam("sort", "attendedAt,asc"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.totalElements").value(2))
                        .andExpectAll(context.getMatchers("$.content"));

                securePerform(get("/attendance/course/{id}", courseId)
                        .queryParam("sort", "attendedAt,asc"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.totalElements").value(2))
                        .andExpectAll(context.getMatchers("$.content"));
            }
        };
    }

    @Test
    void batchCreateAttendance__otherAsTeacher__invalid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                long studentId = ef.createStudent();
                long courseId = ef.createCourse();
                RequestContext<ObjectNode> context = getBatchCreateAttendanceRequest(studentId, courseId);

                ObjectNode request = context.getRequest();

                securePerform(post("/attendance/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isForbidden());
            }
        };
    }

    @Test
    void batchCreateAttendance__notAuthenticated__invalid() throws Exception {
        long studentId = ef.createStudent();
        long courseId = ef.createCourse();
        RequestContext<ObjectNode> context = getBatchCreateAttendanceRequest(studentId, courseId);

        ObjectNode request = context.getRequest();

        mvc.perform(post("/attendance/batch")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request.toString()))
                .andExpect(status().isUnauthorized());
    }

    // =================================================================================================================

    RequestContext<ObjectNode> getPutAttendanceRequest(long studentId, long courseId) {
        ZonedDateTime attendedAt = ZonedDateTime.now();
        AttendanceType attendanceType = TestUtils.randomAttendanceType();

        ObjectNode request = objectMapper.createObjectNode()
                .put("student", studentId)
                .put("course", courseId)
                .put("attendedAt", String.valueOf(attendedAt))
                .put("attendanceType", String.valueOf(attendanceType));

        Function<String, ResultMatcher[]> matchers = prefix -> new ResultMatcher[]{
                jsonPath("$.id").isNumber(),
                jsonPath("$.student").value(studentId),
                jsonPath("$.course").value(courseId),
                jsonPath("$.attendedAt").value(attendanceType),
                jsonPath("$.attendanceType").value(attendanceType)
        };

        return new RequestContext<>(request, matchers);

    };

    @Test
    void putAttendance__self__valid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                long courseId = ef.createCourse(getIdAsLong());
                long studentId = ef.createStudent(getIdAsLong());
                long attendanceId = ef.createAttendance(ef.bag().withCourseId(courseId).withStudentId(studentId));

                RequestContext<ObjectNode> context = getPutAttendanceRequest(studentId, courseId);
                ObjectNode request = context.getRequest();

                securePerform(put("/attendance/{id}", attendanceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isOk())
                        .andExpectAll(context.getMatchers());

                securePerform(get("/attendance/{id}", attendanceId))
                        .andExpect(status().isOk())
                        .andExpectAll(context.getMatchers());
            }
        };
    }

    @Test
    void putAttendance__otherAsAdmin__valid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                long courseId = ef.createCourse(getIdAsLong());
                long studentId = ef.createStudent(getIdAsLong());
                long attendanceId = ef.createAttendance(ef.bag().withCourseId(courseId).withStudentId(studentId));

                RequestContext<ObjectNode> context = getPutAttendanceRequest(studentId, courseId);
                ObjectNode request = context.getRequest();

                securePerform(put("/attendance/{id}", attendanceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isOk())
                        .andExpectAll(context.getMatchers());

                securePerform(get("/attendance/{id}", attendanceId))
                        .andExpect(status().isOk())
                        .andExpectAll(context.getMatchers());
            }
        };
    }

    @Test
    void putAttendance__otherAsTeacher__invalid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                long studentId = ef.createStudent();
                long courseId = ef.createCourse();
                long attendanceId = ef.createAttendance(ef.bag().withStudentId(studentId).withCourseId(courseId));

                RequestContext<ObjectNode> context = getPutAttendanceRequest(studentId, courseId);
                ObjectNode request = context.getRequest();

                securePerform(put("/attendance/{id}", attendanceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isForbidden());
            }
        };
    }

    @Test
    void putAttendance__studentChange__invalid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                long studentId1 = ef.createStudent();
                long studentId2 = ef.createStudent();
                long courseId = ef.createCourse();
                long attendanceId = ef.createAttendance(ef.bag().withStudentId(studentId1).withCourseId(courseId));

                RequestContext<ObjectNode> context = getPutAttendanceRequest(studentId2, courseId);
                ObjectNode request = context.getRequest();

                securePerform(put("attendance/{id}", attendanceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isBadRequest());
            }
        };
    }

    @Test
    void putAttendance__courseChange__invalid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                long studentId = ef.createStudent();
                long courseId1 = ef.createCourse();
                long courseId2 = ef.createCourse();
                long attendanceId = ef.createAttendance(ef.bag().withStudentId(studentId).withCourseId(courseId1));

                RequestContext<ObjectNode> context = getPutAttendanceRequest(studentId, courseId2);
                ObjectNode request = context.getRequest();

                securePerform(put("attendance/{id}", attendanceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isBadRequest());
            }
        };
    }

    @Test
    void putAttendance__notExists__invalid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                long studentId = ef.createStudent();
                long courseId = ef.createCourse();
                long attendanceId = ef.createAttendance(ef.bag().withStudentId(studentId).withCourseId(courseId));

                RequestContext<ObjectNode> context = getPutAttendanceRequest(studentId, courseId);
                ObjectNode request = context.getRequest();

                securePerform(put("/attendance/{id}", attendanceId + 1000)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isNotFound());
            }
        };
    }

    @Test
    void putAttendance__notAuthenticated__invalid() throws Exception {
        long[] info = getNeededInfo();
        long studentId = info[0];
        long courseId = info[1];
        long attendanceId = ef.createCourse(ef.bag().withStudentId(studentId).withCourseId(courseId));

        RequestContext<ObjectNode> context = getPutAttendanceRequest(studentId, courseId);
        ObjectNode request = context.getRequest();

        mvc.perform(put("/attendance/{id}", attendanceId)
                        .contentType(MediaType.APPLICATION_JSON)
                .content(request.toString()))
                .andExpect(status().isUnauthorized());
    }

    // =================================================================================================================

    RequestContext<ObjectNode> getPatchAttendanceRequest(long studentId, long courseId, ZonedDateTime attendedAt) {
        AttendanceType attendanceType = TestUtils.randomAttendanceType();

        ObjectNode request = objectMapper.createObjectNode()
                .put("student", studentId)
                .put("course", courseId)
                .put("attendedAt", String.valueOf(attendedAt));

        Function<String, ResultMatcher[]> matchers = prefix -> new ResultMatcher[]{
                jsonPath("$.id").isNumber(),
                jsonPath("$.student").value(studentId),
                jsonPath("$.course").value(courseId),
                jsonPath("$.attendedAt").value(new TestUtils.DateMatcher(attendedAt)),
                jsonPath("$.attendanceType").value(attendanceType)
        };

        return new RequestContext<>(request, matchers);
    };

    @Test
    void patchAttendance__self__valid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                long studentId = ef.createStudent(getIdAsLong());
                long courseId = ef.createCourse(getIdAsLong());
                ZonedDateTime attendedAt = ZonedDateTime.now();

                long attendanceId = ef.createAttendance(ef.bag()
                        .withStudentId(studentId)
                        .withCourseId(courseId)
                        .withDto(AttendanceDto.builder()
                                .attendedAt(attendedAt)
                                .build()
                        ));
                RequestContext<ObjectNode> context = getPatchAttendanceRequest(studentId, courseId, attendedAt);
                ObjectNode request = context.getRequest();

                securePerform(patch("/attendance/{id}", attendanceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isOk())
                        .andExpectAll(context.getMatchers());

                securePerform(get("/attendance/{id}", attendanceId))
                        .andExpect(status().isOk())
                        .andExpectAll(context.getMatchers());
            }
        };
    }

    @Test
    void patchAttendance__otherAsAdmin__valid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                long studentId = ef.createStudent();
                long courseId = ef.createCourse();
                ZonedDateTime attendedAt = ZonedDateTime.now();

                long attendanceId = ef.createAttendance(ef.bag()
                        .withCourseId(courseId)
                        .withStudentId(studentId)
                        .withDto(AttendanceDto.builder()
                                .attendedAt(attendedAt)
                                .build()
                        ));

                RequestContext<ObjectNode> context = getPatchAttendanceRequest(studentId, courseId, attendedAt);
                ObjectNode request = context.getRequest();

                securePerform(patch("/attendance/{id}", attendanceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isOk())
                        .andExpectAll(context.getMatchers());

                securePerform(get("/attendance/{id}", attendanceId))
                        .andExpect(status().isOk())
                        .andExpectAll(context.getMatchers());
            }
        };
    }

    @Test
    void patchAttendance__otherAsTeacher__invalid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                long studentId = ef.createStudent();
                long courseId = ef.createCourse();
                ZonedDateTime attendedAt = ZonedDateTime.now();

                long attendanceId = ef.createAttendance(ef.bag()
                        .withStudentId(studentId)
                        .withCourseId(courseId)
                        .withDto(AttendanceDto.builder()
                                .attendedAt(attendedAt)
                                .build()
                        ));

                RequestContext<ObjectNode> context = getPatchAttendanceRequest(studentId, courseId, attendedAt);
                ObjectNode request = context.getRequest();

                securePerform(patch("/attendance/{id}", attendanceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isForbidden());
            }
        };
    }

    @Test
    void patchAttendance__studentChange__invalid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                long studentId1 = ef.createStudent();
                long studentId2 = ef.createStudent();
                long courseId = ef.createCourse();
                long attendanceId = ef.createAttendance(ef.bag().withStudentId(studentId1).withCourseId(courseId));
                ZonedDateTime attendedAt = ZonedDateTime.now();

                RequestContext<ObjectNode> context = getPatchAttendanceRequest(studentId2, courseId, attendedAt);
                ObjectNode request = context.getRequest();

                securePerform(put("attendance/{id}", attendanceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isBadRequest());
            }
        };
    }

    @Test
    void patchAttendance__courseChange__invalid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                long studentId = ef.createStudent();
                long courseId1 = ef.createCourse();
                long courseId2 = ef.createCourse();
                long attendanceId = ef.createAttendance(ef.bag().withStudentId(studentId).withCourseId(courseId1));
                ZonedDateTime attendedAt = ZonedDateTime.now();

                RequestContext<ObjectNode> context = getPatchAttendanceRequest(studentId, courseId2, attendedAt);
                ObjectNode request = context.getRequest();

                securePerform(put("attendance/{id}", attendanceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isBadRequest());
            }
        };
    }

    @Test
    void patchAttendance__notExists__invalid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                long studentId = ef.createStudent();
                long courseId = ef.createCourse();
                long attendanceId = ef.createAttendance(ef.bag().withStudentId(studentId).withCourseId(courseId));
                ZonedDateTime attendedAt = ZonedDateTime.now();

                RequestContext<ObjectNode> context = getPatchAttendanceRequest(studentId, courseId, attendedAt);
                ObjectNode request = context.getRequest();

                securePerform(put("/attendance/{id}", attendanceId + 1000)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isNotFound());
            }
        };
    }

    @Test
    void patchAttendance__notAuthenticated__invalid() throws Exception {
        long[] info = getNeededInfo();
        long studentId = info[0];
        long courseId = info[1];
        long attendanceId = ef.createCourse(ef.bag().withStudentId(studentId).withCourseId(courseId));
        ZonedDateTime attendedAt = ZonedDateTime.now();

        RequestContext<ObjectNode> context = getPatchAttendanceRequest(studentId, courseId, attendedAt);
        ObjectNode request = context.getRequest();

        mvc.perform(put("/attendance/{id}", attendanceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                .andExpect(status().isUnauthorized());
    }

    // =================================================================================================================

    @Test
    void deleteAttendance__self__valid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                long attendanceId = ef.createAttendance(getIdAsLong());

                securePerform(delete("/attendance/{id}", attendanceId))
                        .andExpectAll(status().isOk());

                securePerform(get("/attendance/{id}", attendanceId))
                        .andExpectAll(status().isNotFound());
            }
        };
    }

    @Test
    void deleteAttendance__otherAsAdmin__valid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD) {
            @Override
            void run() throws Exception {
                long attendanceId = ef.createAttendance(getIdAsLong());

                securePerform(delete("/attendance/{id}", attendanceId))
                        .andExpectAll(status().isOk());

                securePerform(get("/attendance/{id}", attendanceId))
                        .andExpectAll(status().isNotFound());
            }
        };
    }

    @Test
    void deleteAttendance__otherAsTeacher__invalid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                long attendanceId = ef.createAttendance();

                securePerform(delete("/attendance/{id}", attendanceId))
                        .andExpectAll(status().isForbidden());
            }
        };
    }

    @Test
    void deleteAttendance__notFound__invalid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                long attendanceId = ef.createAttendance(getIdAsLong());

                securePerform(delete("/attendance/{id}", attendanceId + 1000))
                        .andExpectAll(status().isNotFound());
            }
        };
    }

    @Test
    void deleteAttendance__notAuthenticated__invalid() throws Exception {
        long attendanceId = ef.createAttendance();

        mvc.perform(delete("/attendance/{id}", attendanceId))
                .andExpect(status().isUnauthorized());
    }
}
