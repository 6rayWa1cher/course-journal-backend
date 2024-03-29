package com.a6raywa1cher.coursejournalbackend.integration;

import com.a6raywa1cher.coursejournalbackend.RequestContext;
import com.a6raywa1cher.coursejournalbackend.TestUtils;
import com.a6raywa1cher.coursejournalbackend.dto.*;
import com.a6raywa1cher.coursejournalbackend.model.AttendanceType;
import com.a6raywa1cher.coursejournalbackend.model.UserRole;
import com.a6raywa1cher.coursejournalbackend.service.AttendanceService;
import com.a6raywa1cher.coursejournalbackend.service.CourseService;
import com.a6raywa1cher.coursejournalbackend.service.StudentService;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultMatcher;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.contains;
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

    @Test
    void getAttendanceById__self__valid() {
        new WithUser(USERNAME, PASSWORD, UserRole.TEACHER) {
            @Override
            void run() throws Exception {
                int attendedClass = faker.number().numberBetween(1, 6);
                LocalDate attendedDate = LocalDate.now();
                long courseId = ef.createCourse(getSelfEmployeeIdAsLong());
                long studentId = ef.createStudent(ef.bag().withCourseId(courseId));
                AttendanceType attendanceType = TestUtils.randomAttendanceType();

                long id = attendanceService.create(AttendanceDto.builder()
                        .student(studentId)
                        .course(courseId)
                        .attendedClass(attendedClass)
                        .attendanceType(attendanceType)
                        .attendedDate(attendedDate)
                        .build()).getId();

                ResultMatcher[] matchers = {
                        jsonPath("$.id").value(id),
                        jsonPath("$.student").value(studentId),
                        jsonPath("$.attendedClass").value(attendedClass),
                        jsonPath("$.attendanceType").value(attendanceType.toString()),
                        jsonPath("$.attendedDate").value(attendedDate.toString())
                };

                securePerform(get("/attendances/{id}", id))
                        .andExpectAll(matchers)
                        .andExpect(status().isOk());
            }
        };
    }

    @Test
    void getAttendanceById__headman__valid() {
        new WithUser(USERNAME, PASSWORD, UserRole.HEADMAN) {
            @Override
            void run() throws Exception {
                int attendedClass = faker.number().numberBetween(1, 6);
                LocalDate attendedDate = LocalDate.now();
                long courseId = ef.createCourse();
                long studentId = ef.createStudent(ef.bag().withCourseId(courseId));
                AttendanceType attendanceType = TestUtils.randomAttendanceType();

                long id = attendanceService.create(AttendanceDto.builder()
                        .student(studentId)
                        .course(courseId)
                        .attendedClass(attendedClass)
                        .attendanceType(attendanceType)
                        .attendedDate(attendedDate)
                        .build()).getId();

                ResultMatcher[] matchers = {
                        jsonPath("$.id").value(id),
                        jsonPath("$.student").value(studentId),
                        jsonPath("$.attendedClass").value(attendedClass),
                        jsonPath("$.attendanceType").value(attendanceType.toString()),
                        jsonPath("$.attendedDate").value(attendedDate.toString())
                };

                securePerform(get("/attendances/{id}", id))
                        .andExpectAll(matchers)
                        .andExpect(status().isOk());
            }
        };
    }

    @Test
    void getAttendanceById__otherAsAdmin__valid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                LocalDate attendedDate = LocalDate.now();
                int attendedClass = faker.number().numberBetween(1, 6);
                long courseId = ef.createCourse();
                long studentId = ef.createStudent(ef.bag().withCourseId(courseId));
                AttendanceType attendanceType = TestUtils.randomAttendanceType();

                long id = attendanceService.create(AttendanceDto.builder()
                        .student(studentId)
                        .course(courseId)
                        .attendedClass(attendedClass)
                        .attendanceType(attendanceType)
                        .attendedDate(attendedDate)
                        .build()).getId();

                ResultMatcher[] matchers = {
                        jsonPath("$.id").value(id),
                        jsonPath("$.student").value(studentId),
                        jsonPath("$.attendedClass").value(attendedClass),
                        jsonPath("$.attendanceType").value(attendanceType.toString()),
                        jsonPath("$.attendedDate").value(attendedDate.toString())
                };

                securePerform(get("/attendances/{id}", id))
                        .andExpect(status().isOk())
                        .andExpectAll(matchers);
            }
        };
    }

    @Test
    void getAttendanceById__otherAsTeacher__invalid() {
        new WithUser(USERNAME, PASSWORD, UserRole.TEACHER) {
            @Override
            void run() throws Exception {
                LocalDate attendedDate = LocalDate.now();
                int attendedClass = faker.number().numberBetween(1, 6);
                long courseId = ef.createCourse();
                long studentId = ef.createStudent();
                AttendanceType attendanceType = TestUtils.randomAttendanceType();

                long id = attendanceService.create(AttendanceDto.builder()
                        .student(studentId)
                        .course(courseId)
                        .attendedClass(attendedClass)
                        .attendanceType(attendanceType)
                        .attendedDate(attendedDate)
                        .build()).getId();

                securePerform(get("/attendances/{id}", id))
                        .andExpect(status().isForbidden());
            }
        };
    }

    @Test
    void getAttendanceById__notAuthenticated__invalid() throws Exception {
        int attendedClass = faker.number().numberBetween(1, 6);
        long courseId = ef.createCourse();
        long studentId = ef.createStudent();
        LocalDate attendedDate = LocalDate.now();
        AttendanceType attendanceType = TestUtils.randomAttendanceType();

        long id = attendanceService.create(AttendanceDto.builder()
                .student(studentId)
                .course(courseId)
                .attendedClass(attendedClass)
                .attendanceType(attendanceType)
                .attendedDate(attendedDate)
                .build()).getId();

        mvc.perform(get("/attendances/{id}", id))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getAttendanceById__notExists__invalid() {
        int attendedClass = faker.number().numberBetween(1, 6);
        long courseId = ef.createCourse();
        long studentId = ef.createStudent();
        LocalDate attendedDate = LocalDate.now();
        AttendanceType attendanceType = TestUtils.randomAttendanceType();

        long id = attendanceService.create(AttendanceDto.builder()
                .student(studentId)
                .course(courseId)
                .attendedClass(attendedClass)
                .attendanceType(attendanceType)
                .attendedDate(attendedDate)
                .build()).getId();

        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                securePerform(get("/attendances/{id}", id + 1000))
                        .andExpect(status().isNotFound());
            }
        };
    }

    // =================================================================================================================

    RequestContext<Long> createGetAttendanceByCourseContext(long studentId, long courseId, int attendedClass) {
        LocalDate attendedDate = LocalDate.now().minusDays(1);
        AttendanceType attendanceType = TestUtils.randomAttendanceType();

        long id = attendanceService.create(AttendanceDto.builder()
                .attendedDate(attendedDate)
                .attendanceType(attendanceType)
                .attendedClass(attendedClass)
                .course(courseId)
                .student(studentId)
                .build()).getId();

        Function<String, ResultMatcher[]> matchers = prefix -> new ResultMatcher[]{
                jsonPath(prefix + ".id").value(id),
                jsonPath(prefix + ".student").value(studentId),
                jsonPath(prefix + ".course").value(courseId),
                jsonPath(prefix + ".attendedDate").value(attendedDate.toString()),
                jsonPath(prefix + ".attendedClass").value(attendedClass),
                jsonPath(prefix + ".attendanceType").value(attendanceType.toString()),
        };

        return RequestContext.<Long>builder()
                .request(id)
                .matchersSupplier(matchers)
                .build();
    }

    @Test
    void getAttendanceByCourse__self__valid() {
        new WithUser(USERNAME, PASSWORD, UserRole.TEACHER) {
            @Override
            void run() throws Exception {
                long courseId1 = ef.createCourse(getSelfEmployeeIdAsLong());
                long courseId2 = ef.createCourse(getSelfEmployeeIdAsLong());

                long studentId1 = ef.createStudent(getSelfEmployeeIdAsLong());
                long studentId2 = ef.createStudent(getSelfEmployeeIdAsLong());
                long studentId3 = ef.createStudent(getSelfEmployeeIdAsLong());

                int attendedClass = faker.number().numberBetween(1, 6);

                var context1 = createGetAttendanceByCourseContext(studentId1, courseId1, attendedClass);
                var context2 = createGetAttendanceByCourseContext(studentId2, courseId1, attendedClass + 1);
                createGetAttendanceByCourseContext(studentId3, courseId2, attendedClass + 2);

                securePerform(get("/attendances/course/{id}", courseId1))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$", hasSize(2)))
                        .andExpectAll(context1.getMatchers("$[0]"))
                        .andExpectAll(context2.getMatchers("$[1]"));
            }
        };
    }

    @Test
    void getAttendanceByCourse__otherAsAdmin__valid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                long employeeId = ef.createEmployee();
                long courseId1 = ef.createCourse(employeeId);
                long courseId2 = ef.createCourse(employeeId);

                long studentId1 = ef.createStudent();
                long studentId2 = ef.createStudent();
                long studentId3 = ef.createStudent();

                int attendedClass = faker.number().numberBetween(1, 6);

                var context1 = createGetAttendanceByCourseContext(studentId1, courseId1, attendedClass);
                var context2 = createGetAttendanceByCourseContext(studentId2, courseId1, attendedClass + 1);
                createGetAttendanceByCourseContext(studentId3, courseId2, attendedClass + 2);

                securePerform(get("/attendances/course/{id}", courseId1))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$", hasSize(2)))
                        .andExpectAll(context1.getMatchers("$[0]"))
                        .andExpectAll(context2.getMatchers("$[1]"));
            }
        };
    }

    @Test
    void getAttendanceByCourse__otherAsTeacher__invalid() {
        long id = ef.createCourse();

        new WithUser(USERNAME, PASSWORD, UserRole.TEACHER) {
            @Override
            void run() throws Exception {
                securePerform(get("/attendances/course/{id}", id))
                        .andExpect(status().isForbidden());
            }
        };
    }

    @Test
    void getAttendanceByCourse__notAuthenticated__invalid() throws Exception {
        long id = ef.createCourse();
        mvc.perform(get("/attendances/course/{id}", id)).andExpect(status().isUnauthorized());
    }

    // =================================================================================================================

    RequestContext<Long> createGetAttendanceByCourseAndStudentContext(long courseId, long studentId, int attendedClass) {
        LocalDate attendedDate = LocalDate.now().minusDays(1);
        AttendanceType attendanceType = TestUtils.randomAttendanceType();

        long id = attendanceService.create(AttendanceDto.builder()
                .attendedDate(attendedDate)
                .attendanceType(attendanceType)
                .attendedClass(attendedClass)
                .course(courseId)
                .student(studentId)
                .build()).getId();

        Function<String, ResultMatcher[]> matchers = prefix -> new ResultMatcher[]{
                jsonPath(prefix + ".id").value(id),
                jsonPath(prefix + ".student").value(studentId),
                jsonPath(prefix + ".course").value(courseId),
                jsonPath(prefix + ".attendedDate").value(attendedDate.toString()),
                jsonPath(prefix + ".attendedClass").value(attendedClass),
                jsonPath(prefix + ".attendanceType").value(attendanceType.toString()),
        };

        return RequestContext.<Long>builder()
                .request(id)
                .matchersSupplier(matchers)
                .build();
    }

    @Test
    void getAttendanceByCourseAndStudent__self__valid() {
        new WithUser(USERNAME, PASSWORD, UserRole.TEACHER) {
            @Override
            void run() throws Exception {
                long courseId1 = ef.createCourse(getSelfEmployeeIdAsLong());
                long courseId2 = ef.createCourse(getSelfEmployeeIdAsLong());

                long studentId1 = ef.createStudent(ef.bag().withCourseId(courseId1));
                long studentId2 = ef.createStudent(ef.bag().withCourseId(courseId2));

                int attendedClass = 1;

                var context1 = createGetAttendanceByCourseAndStudentContext(courseId1, studentId1, attendedClass);
                var context2 = createGetAttendanceByCourseAndStudentContext(courseId1, studentId1, attendedClass + 1);
                createGetAttendanceByCourseAndStudentContext(courseId2, studentId1, attendedClass + 2);
                createGetAttendanceByCourseAndStudentContext(courseId1, studentId2, attendedClass + 3);

                securePerform(get("/attendances/course/{courseId}/student/{studentId}", courseId1, studentId1))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$", hasSize(2)))
                        .andExpectAll(context1.getMatchers("$[0]"))
                        .andExpectAll(context2.getMatchers("$[1]"));
            }
        };
    }

    @Test
    void getAttendanceByCourseAndStudent__otherAsAdmin__valid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                long courseId1 = ef.createCourse();
                long courseId2 = ef.createCourse();

                long studentId1 = ef.createStudent(ef.bag().withCourseId(courseId1));
                long studentId2 = ef.createStudent(ef.bag().withCourseId(courseId2));

                int attendedClass = faker.number().numberBetween(1, 6);

                var context1 = createGetAttendanceByCourseContext(studentId1, courseId1, attendedClass);
                var context2 = createGetAttendanceByCourseContext(studentId1, courseId1, attendedClass + 1);
                createGetAttendanceByCourseContext(studentId1, courseId2, attendedClass + 2);
                createGetAttendanceByCourseContext(studentId2, courseId1, attendedClass + 3);

                securePerform(get("/attendances/course/{id}/student/{id}", courseId1, studentId1))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$", hasSize(2)))
                        .andExpectAll(context1.getMatchers("$[0]"))
                        .andExpectAll(context2.getMatchers("$[1]"));
            }
        };
    }

    @Test
    void getAttendanceByCourseAndStudent__otherAsTeacher__invalid() {
        new WithUser(USERNAME, PASSWORD, UserRole.TEACHER) {
            @Override
            void run() throws Exception {
                long courseId1 = ef.createCourse();
                long courseId2 = ef.createCourse();

                long studentId1 = ef.createStudent(ef.bag().withCourseId(courseId1));
                long studentId2 = ef.createStudent(ef.bag().withCourseId(courseId2));

                int attendedClass = faker.number().numberBetween(1, 6);

                createGetAttendanceByCourseContext(studentId1, courseId1, attendedClass);
                createGetAttendanceByCourseContext(studentId1, courseId1, attendedClass + 1);
                createGetAttendanceByCourseContext(studentId1, courseId2, attendedClass + 2);
                createGetAttendanceByCourseContext(studentId2, courseId1, attendedClass + 3);

                securePerform(get("/attendances/course/{id}/student/{id}", courseId1, studentId1))
                        .andExpect(status().isForbidden());
            }
        };
    }

    @Test
    void getAttendanceByCourseAndStudent__notAuthenticated__invalid() throws Exception {
        long id = ef.createCourse();
        mvc.perform(get("/attendances/course/{id}", id)).andExpect(status().isUnauthorized());
    }

    // =================================================================================================================

    RequestContext<ObjectNode> createGetTableByCourseAndDatePeriod(long courseId, List<Long> studentIds, LocalDate fromDate, LocalDate toDate) {
        List<LocalDate> dates = new ArrayList<>();
        fromDate.datesUntil(toDate).forEach(dates::add);
        int classNumber1 = faker.number().numberBetween(1, 14);
        int classNumber2 = classNumber1 + 1;
        int classNumber3 = classNumber2 + 1;
        List<Integer> classes = List.of(classNumber1, classNumber2, classNumber3);
        List<AttendanceType> attendanceTypes = new ArrayList<>();

        List<TableDto.TableHeaderElement> header = new ArrayList<>();
        List<TableDto.TableBodyElement> body = new ArrayList<>();

        for (Long studentId : studentIds) {
            for (LocalDate date : dates) {
                for (Integer classNumber : classes) {
                    AttendanceType attendanceType = TestUtils.randomAttendanceType();
                    attendanceTypes.add(attendanceType);
                    ef.createAttendance(ef.bag().withStudentId(studentId).withCourseId(courseId).withDto(AttendanceDto.builder()
                            .attendedDate(date)
                            .attendanceType(attendanceType)
                            .attendedClass(classNumber)
                            .build()));
                }
            }
        }

        for (LocalDate date : dates) {
            for (Integer classNumber : classes) {
                TableDto.TableHeaderElement headerElement = new TableDto.TableHeaderElement(date, classNumber);
                header.add(headerElement);
            }
        }

        int headerSize = header.size();

        for (int i = 0; i < studentIds.size(); i++) {
            TableDto.TableBodyElement bodyElement = new TableDto.TableBodyElement(studentIds.get(i), attendanceTypes.subList(i * headerSize, (i + 1) * headerSize), "Alexandr", 0L);

            body.add(bodyElement);
        }

        ObjectNode request = objectMapper.createObjectNode()
                .put("fromDate", String.valueOf(fromDate))
                .put("toDate", String.valueOf(toDate));

        Function<String, ResultMatcher[]> matchers = prefix -> Stream.concat(
                IntStream.range(0, headerSize)
                        .boxed()
                        .flatMap(i -> {
                            TableDto.TableHeaderElement headerElement = header.get(i);
                            return Stream.of(
                                    jsonPath(prefix + ".header[%d].classNumber".formatted(i)).value(headerElement.getClassNumber()),
                                    jsonPath(prefix + ".header[%d].date".formatted(i), new TestUtils.LocalDateMatcher(headerElement.getDate()))
                            );
                        }),
                IntStream.range(0, body.size())
                        .boxed()
                        .flatMap(i -> {
                            TableDto.TableBodyElement bodyElement = body.get(i);
                            return Stream.of(
                                    jsonPath(prefix + ".body[%d].studentId".formatted(i)).value(bodyElement.getStudentId()),
                                    jsonPath(prefix + ".body[%d].attendances".formatted(i),
                                            contains(bodyElement.getAttendances().stream()
                                                    .map(AttendanceType::toString)
                                                    .toArray(String[]::new)))
                            );
                        })
        ).toArray(ResultMatcher[]::new);

        return new RequestContext<>(request, matchers);
    }
    RequestContext<ObjectNode> createGetTableWithoutAttendancesByCourse(List<Long> students) {
        ObjectNode request = objectMapper.createObjectNode();

        Function<String, ResultMatcher[]> matchers = prefix -> new ResultMatcher[]{
                jsonPath(prefix + ".header").isEmpty(),
                jsonPath(prefix + ".body[0].studentId").value(students.get(0)),
                jsonPath(prefix + ".body[1].studentId").value(students.get(1))
        };
        return new RequestContext<>(request, matchers);
    }

    @Test
    void getTable__self__valid() {
        new WithUser(USERNAME, PASSWORD, UserRole.TEACHER) {
            @Override
            void run() throws Exception {
                long groupId = ef.createGroup();
                long studentId1 = ef.createStudent(ef.bag().withGroupId(groupId));
                long studentId2 = ef.createStudent(ef.bag().withGroupId(groupId));
                List<Long> studentIds = List.of(studentId1, studentId2);
                long courseId = ef.createCourse(ef.bag().withEmployeeId(getSelfEmployeeIdAsLong()).withDto(CourseFullDto.builder()
                                .students(studentIds)
                        .build()));

                LocalDate fromDate = LocalDate.now().minusDays(3);
                LocalDate toDate = LocalDate.now();
                var context = createGetTableByCourseAndDatePeriod(courseId, studentIds, fromDate, toDate);


                securePerform(get("/attendances/table/{courseId}?fromDate={fromDate}&toDate={toDate}", courseId, fromDate, toDate))
                        .andExpect(status().isOk())
                        .andExpectAll(context.getMatchers());
            }
        };
    }

    @Test
    void getTable__otherAsAdmin__valid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                long studentId1 = ef.createStudent();
                long studentId2 = ef.createStudent();
                List<Long> studentIds = List.of(studentId1, studentId2);
                long courseId = ef.createCourse(ef.bag().withDto(CourseFullDto.builder()
                        .students(studentIds)
                        .build()));

                LocalDate fromDate = LocalDate.now().minusDays(3);
                LocalDate toDate = LocalDate.now();
                var context = createGetTableByCourseAndDatePeriod(courseId, studentIds, fromDate, toDate);

                securePerform(get("/attendances/table/{courseId}?fromDate={fromDate}&toDate={toDate}", courseId, fromDate, toDate))
                        .andExpect(status().isOk())
                        .andExpectAll(context.getMatchers());
            }
        };
    }

    @Test
    void getTable__otherAsTeacher__invalid() {
        new WithUser(USERNAME, PASSWORD, UserRole.TEACHER) {
            @Override
            void run() throws Exception {
                long studentId1 = ef.createStudent();
                long studentId2 = ef.createStudent();
                List<Long> studentIds = List.of(studentId1, studentId2);
                long courseId = ef.createCourse(ef.bag().withDto(CourseFullDto.builder()
                        .students(studentIds)
                        .build()));

                LocalDate fromDate = LocalDate.now().minusDays(3);
                LocalDate toDate = LocalDate.now();
                createGetTableByCourseAndDatePeriod(courseId, studentIds, fromDate, toDate);

                securePerform(get("/attendances/table/{courseId}?fromDate={fromDate}&toDate={toDate}", courseId, fromDate, toDate))
                        .andExpect(status().isForbidden());
            }
        };
    }

    @Test
    void getTable__withoutAttendances__valid() {
        new WithUser(USERNAME, PASSWORD, UserRole.TEACHER) {
            @Override
            void run() throws Exception {
                long studentId1 = ef.createStudent();
                long studentId2 = ef.createStudent();
                List<Long> students = List.of(studentId1, studentId2);

                long courseId = ef.createCourse(ef.bag().withEmployeeId(getSelfEmployeeIdAsLong()).withDto(CourseFullDto.builder()
                                .students(students)
                        .build()));

                LocalDate fromDate = LocalDate.now().minusDays(3);
                LocalDate toDate = LocalDate.now();
                var context = createGetTableWithoutAttendancesByCourse(students);

                securePerform(get("/attendances/table/{courseId}?fromDate={fromDate}&toDate={toDate}", courseId, fromDate, toDate))
                        .andExpect(status().isOk())
                        .andExpectAll(context.getMatchers());
            }
        };
    }

    @Test
    void getTable__fromDateAfterToDate__invalid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                long courseId = ef.createCourse();

                LocalDate fromDate = LocalDate.now().plusDays(3);
                LocalDate toDate = LocalDate.now();

                securePerform(get("/attendances/table/{courseId}?fromDate={fromDate}&toDate={toDate}", courseId, fromDate, toDate))
                        .andExpect(status().isBadRequest());
            }
        };
    }

    @Test
    void getTable__notAuthenticated__invalid() throws Exception {
        long courseId = ef.createCourse(ef.bag());
        LocalDate fromDate = LocalDate.now().plusDays(3);
        LocalDate toDate = LocalDate.now();

        mvc.perform(get("/attendances/table/{courseId}?fromDate={fromDate}&toDate={toDate}", courseId, fromDate, toDate))
                .andExpect(status().isUnauthorized());
    }

    // =================================================================================================================

    RequestContext<ObjectNode> createGetConflictsByCourseAndDatePeriod(List<Long> studentIds, LocalDate fromDate, LocalDate toDate) {
        AttendanceConflictListDto conflicts = new AttendanceConflictListDto();
        List<LocalDate> dates = new ArrayList<>();
        fromDate.datesUntil(toDate).forEach(dates::add);
        int classNumber1 = faker.number().numberBetween(1, 14);
        int classNumber2 = classNumber1 + 1;
        List<Integer> classNumbers = List.of(classNumber1, classNumber2);
        String teacherFirstName = faker.name().firstName();
        String teacherLastName = faker.name().lastName();
        long teacher = ef.createEmployee(ef.bag().withDto(EmployeeDto.builder()
                        .firstName(teacherFirstName)
                        .lastName(teacherLastName)
                .build()));
        String courseName = faker.lorem().sentence(3);
        long course = ef.createCourse(ef.bag().withEmployeeId(teacher).withDto(CourseFullDto.builder()
                        .name(courseName)
                .build()));

        for (LocalDate date : dates) {
            for (int classNumber : classNumbers) {
                for (long studentId : studentIds) {
                    AttendanceType attendanceType = TestUtils.randomAttendanceType();
                    AttendanceConflictListDto.AttendanceConflict conflict = new AttendanceConflictListDto.AttendanceConflict(
                            teacherLastName + ' ' + teacherFirstName,
                            courseName,
                            studentId,
                            date,
                            classNumber,
                            attendanceType
                    );
                    conflicts.addAttendanceConflictToList(conflict);
                    ef.createAttendance(ef.bag().withCourseId(course).withStudentId(studentId).withDto(AttendanceDto.builder()
                                    .attendedClass(classNumber)
                                    .attendedDate(date)
                                    .attendanceType(attendanceType)
                            .build()));
                }
            }
        }

        ObjectNode request = objectMapper.createObjectNode()
                .put("fromDate", String.valueOf(fromDate))
                .put("toDate", String.valueOf(toDate));

        Function<String, ResultMatcher[]> matchers = prefix -> IntStream.range(0, dates.size() * 4)
            .boxed()
            .flatMap(i -> {
                AttendanceConflictListDto.AttendanceConflict conflict = conflicts.get(i);
                return Stream.of(
                        jsonPath(prefix + ".conflicts[%d].attendedClass".formatted(i)).value(conflict.getAttendedClass()),
                        jsonPath(prefix + ".conflicts[%d].attendedDate".formatted(i), new TestUtils.LocalDateMatcher(conflict.getAttendedDate())),
                        jsonPath(prefix + ".conflicts[%d].attendanceType".formatted(i)).value(conflict.getAttendanceType().toString()),
                        jsonPath(prefix + ".conflicts[%d].conflictedTeacherFullName".formatted(i)).value(conflict.getConflictedTeacherFullName()),
                        jsonPath(prefix + ".conflicts[%d].studentId".formatted(i)).value(conflict.getStudentId()),
                        jsonPath(prefix + ".conflicts[%d].conflictedCourseName".formatted(i)).value(conflict.getConflictedCourseName())
                );
            }).toArray(ResultMatcher[]::new);

        return new RequestContext<>(request, matchers);
    }
    RequestContext<ObjectNode> createGetConflictsWithoutConflictsByCourse(List<Long> studentIds) {
        ObjectNode request = objectMapper.createObjectNode();

        Function<String, ResultMatcher[]> matchers = prefix -> new ResultMatcher[]{
                jsonPath(prefix + ".conflicts").isEmpty(),
        };
        return new RequestContext<>(request, matchers);
    }

    @Test
    void getConflicts__self__valid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                LocalDate fromDate = LocalDate.now();
                LocalDate toDate = fromDate.plusDays(3);
                long student1 = ef.createStudent();
                long student2 = ef.createStudent();
                List<Long> studentIds = List.of(student1, student2);
                long courseId = ef.createCourse(ef.bag().withEmployeeId(getSelfEmployeeIdAsLong()).withDto(CourseFullDto.builder()
                                .students(studentIds)
                        .build()));
                var context = createGetConflictsByCourseAndDatePeriod(studentIds, fromDate, toDate);
                securePerform(get("/attendances/conflicts/{courseId}?fromDate={fromDate}&toDate={toDate}", courseId, fromDate, toDate))
                        .andExpect(status().isOk())
                        .andExpectAll(context.getMatchers());
            }
        };
    }

    @Test
    void getConflicts__otherAsAdmin__valid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                LocalDate fromDate = LocalDate.now();
                LocalDate toDate = fromDate.plusDays(3);
                long student1 = ef.createStudent();
                long student2 = ef.createStudent();
                List<Long> studentIds = List.of(student1, student2);
                long courseId = ef.createCourse(ef.bag().withDto(CourseFullDto.builder()
                        .students(studentIds)
                        .build()));
                var context = createGetConflictsByCourseAndDatePeriod(studentIds, fromDate, toDate);
                securePerform(get("/attendances/conflicts/{courseId}?fromDate={fromDate}&toDate={toDate}", courseId, fromDate, toDate))
                        .andExpect(status().isOk())
                        .andExpectAll(context.getMatchers());
            }
        };
    }

    @Test
    void getConflicts__otherAsTeacher__invalid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                LocalDate fromDate = LocalDate.now();
                LocalDate toDate = fromDate.plusDays(3);
                long student1 = ef.createStudent();
                long student2 = ef.createStudent();
                List<Long> studentIds = List.of(student1, student2);
                long courseId = ef.createCourse(ef.bag().withDto(CourseFullDto.builder()
                        .students(studentIds)
                        .build()));
                securePerform(get("/attendances/conflicts/{courseId}?fromDate={fromDate}&toDate={toDate}", courseId, fromDate, toDate))
                        .andExpect(status().isForbidden());
            }
        };
    }

    @Test
    void getConflicts__withoutConflicts__valid() {
        new WithUser(USERNAME, PASSWORD) {
            @Override
            void run() throws Exception {
                LocalDate fromDate = LocalDate.now();
                LocalDate toDate = fromDate.plusDays(3);
                long student1 = ef.createStudent();
                long student2 = ef.createStudent();
                List<Long> studentIds = List.of(student1, student2);
                long courseId = ef.createCourse(ef.bag().withEmployeeId(getSelfEmployeeIdAsLong()).withDto(CourseFullDto.builder()
                        .students(studentIds)
                        .build()));
                var context = createGetConflictsWithoutConflictsByCourse(studentIds);
                securePerform(get("/attendances/conflicts/{courseId}?fromDate={fromDate}&toDate={toDate}", courseId, fromDate, toDate))
                        .andExpect(status().isOk())
                        .andExpectAll(context.getMatchers());
            }
        };
    }

    @Test
    void getConflicts__fromDateAfterToDate__invalid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                LocalDate fromDate = LocalDate.now();
                LocalDate toDate = fromDate.minusDays(3);
                long courseId = ef.createCourse();

                securePerform(get("/attendances/conflicts/{courseId}?fromDate={fromDate}&toDate={toDate}", courseId, fromDate, toDate))
                        .andExpect(status().isBadRequest());
            }
        };
    }

    @Test
    void getConflicts__notAuthenticated__invalid() throws Exception {
        LocalDate fromDate = LocalDate.now();
        LocalDate toDate = fromDate.plusDays(3);
        long student1 = ef.createStudent();
        long student2 = ef.createStudent();
        List<Long> studentIds = List.of(student1, student2);
        long courseId = ef.createCourse(ef.bag().withDto(CourseFullDto.builder()
                .students(studentIds)
                .build()));
        mvc.perform(get("/attendances/conflicts/{courseId}?fromDate={fromDate}&toDate={toDate}", courseId, fromDate, toDate))
                .andExpect(status().isUnauthorized());
    }

    // =================================================================================================================

    RequestContext<ObjectNode> getCreateAttendanceRequest(long studentId, long courseId) {
        int attendedClass = faker.number().numberBetween(1, 6);

        LocalDate attendedDate = LocalDate.now();

        AttendanceType attendanceType = TestUtils.randomAttendanceType();

        ObjectNode request = objectMapper.createObjectNode()
                .put("student", studentId)
                .put("course", courseId)
                .put("attendedClass", String.valueOf(attendedClass))
                .put("attendedDate", String.valueOf(attendedDate))
                .put("attendanceType", String.valueOf(attendanceType));

        Function<String, ResultMatcher[]> matchers = prefix -> new ResultMatcher[]{
                jsonPath("$.id").isNumber(),
                jsonPath("$.student").value(studentId),
                jsonPath("$.course").value(courseId),
                jsonPath("$.attendedClass").value(attendedClass),
                jsonPath("$.attendanceType").value(attendanceType.toString()),
                jsonPath("$.attendedDate").value(attendedDate.toString())
        };

        return new RequestContext<>(request, matchers);
    }

    @Test
    void createAttendance__self__valid() {
        new WithUser(USERNAME, PASSWORD, UserRole.TEACHER) {
            @Override
            void run() throws Exception {
                long courseId = ef.createCourse(getSelfEmployeeIdAsLong());
                long studentId = ef.createStudent(ef.bag().withCourseId(courseId));
                var context = getCreateAttendanceRequest(studentId, courseId);

                ObjectNode request = context.getRequest();

                MvcResult mvcResult = securePerform(post("/attendances/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isCreated())
                        .andExpectAll(context.getMatchers())
                        .andReturn();

                int id = JsonPath.read(mvcResult.getResponse().getContentAsString(), "$.id");

                securePerform(get("/attendances/{id}", id))
                        .andExpect(status().isOk())
                        .andExpectAll(context.getMatchers());
            }
        };
    }

    @Test
    void createAttendance__otherAsAdmin__valid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                long courseId = ef.createCourse();
                long studentId = ef.createStudent(ef.bag().withCourseId(courseId));
                var context = getCreateAttendanceRequest(studentId, courseId);

                ObjectNode request = context.getRequest();

                MvcResult mvcResult = securePerform(post("/attendances/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isCreated())
                        .andExpectAll(context.getMatchers())
                        .andReturn();

                int id = JsonPath.read(mvcResult.getResponse().getContentAsString(), "$.id");

                securePerform(get("/attendances/{id}", id))
                        .andExpect(status().isOk())
                        .andExpectAll(context.getMatchers());
            }
        };
    }

    @Test
    void createAttendance__otherAsTeacher__invalid() {
        new WithUser(USERNAME, PASSWORD, UserRole.TEACHER) {
            @Override
            void run() throws Exception {
                long courseId = ef.createCourse();
                long studentId = ef.createStudent(ef.bag().withCourseId(courseId));

                var context = getCreateAttendanceRequest(studentId, courseId);

                ObjectNode request = context.getRequest();

                securePerform(post("/attendances/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isForbidden());
            }
        };
    }

    @Test
    void createAttendance__studentAndAttendedDateAndAttendedClassNotUnique__invalid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            void run() throws Exception {
                long courseId = ef.createCourse();
                long studentId = ef.createStudent(ef.bag().withCourseId(courseId));
                LocalDate attendedDate = LocalDate.now();
                int attendedClass = faker.number().numberBetween(1, 6);
                AttendanceType attendanceType = TestUtils.randomAttendanceType();

                attendanceService.create(AttendanceDto.builder()
                        .student(studentId)
                        .course(courseId)
                        .attendedDate(attendedDate)
                        .attendedClass(attendedClass)
                        .attendanceType(attendanceType)
                        .build());

                ObjectNode request = objectMapper.createObjectNode()
                        .put("student", studentId)
                        .put("course", courseId)
                        .put("attendedClass", String.valueOf(attendedClass))
                        .put("attendedDate", String.valueOf(attendedDate))
                        .put("attendanceType", String.valueOf(attendanceType));

                securePerform(post("/attendances/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isConflict());

            }
        };
    }

    @Test
    void createAttendance__notAuthenticated__invalid() throws Exception {
        long courseId = ef.createCourse();
        long studentId = ef.createStudent(ef.bag().withCourseId(courseId));

        var context = getCreateAttendanceRequest(studentId, courseId);

        ObjectNode request = context.getRequest();

        mvc.perform(post("/attendances/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                .andExpect(status().isUnauthorized());
    }

    // =================================================================================================================

    RequestContext<ObjectNode> getBatchCreateAttendanceRequest(long studentId, long courseId) {
        int attendedClass1 = faker.number().numberBetween(1, 5);
        AttendanceType attendanceType1 = TestUtils.randomAttendanceType();

        LocalDate attendedDate = LocalDate.now();

        int attendedClass2 = attendedClass1 + 1;
        AttendanceType attendanceType2 = TestUtils.randomAttendanceType();

        ObjectNode request = objectMapper.createObjectNode()
                .put("course", courseId);

        request.putArray("attendances")
                .add(objectMapper.createObjectNode()
                        .put("student", studentId)
                        .put("attendedDate", String.valueOf(attendedDate))
                        .put("attendedClass", String.valueOf(attendedClass1))
                        .put("attendanceType", String.valueOf(attendanceType1)))
                .add(objectMapper.createObjectNode()
                        .put("student", studentId)
                        .put("attendedDate", String.valueOf(attendedDate))
                        .put("attendedClass", String.valueOf(attendedClass2))
                        .put("attendanceType", String.valueOf(attendanceType2)));
        return new RequestContext<>(request, prefix -> new ResultMatcher[]{
                jsonPath("[0].attendedClass").value(attendedClass1),
                jsonPath("[0].attendedDate").value(attendedDate.toString()),
                jsonPath("[0].attendanceType").value(attendanceType1.toString()),
                jsonPath("[1].attendedClass").value(attendedClass2),
                jsonPath("[1].attendedDate").value(attendedDate.toString()),
                jsonPath("[1].attendanceType").value(attendanceType2.toString())
        });
    }

    @Test
    void batchCreateAttendance__self__valid() {
        new WithUser(USERNAME, PASSWORD, UserRole.TEACHER) {
            @Override
            void run() throws Exception {
                long courseId = ef.createCourse(getSelfEmployeeIdAsLong());
                long studentId = ef.createStudent(ef.bag().withCourseId(courseId));
                RequestContext<ObjectNode> context = getBatchCreateAttendanceRequest(studentId, courseId);

                ObjectNode request = context.getRequest();

                securePerform(post("/attendances/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isCreated())
                        .andExpect(jsonPath("$", hasSize(2)))
                        .andExpectAll(context.getMatchers());

                securePerform(get("/attendances/course/{id}", courseId))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$", hasSize(2)))
                        .andExpectAll(context.getMatchers("$.content"));

                securePerform(get("/attendances/course/{id}", courseId))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$", hasSize(2)))
                        .andExpectAll(context.getMatchers("$.content"));
            }
        };
    }

    @Test
    void batchCreateAttendance__otherAsAdmin__valid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                long userId = ef.createEmployee();
                long courseId = ef.createCourse(userId);
                long studentId = ef.createStudent(ef.bag().withCourseId(courseId));
                RequestContext<ObjectNode> context = getBatchCreateAttendanceRequest(studentId, courseId);

                ObjectNode request = context.getRequest();

                securePerform(post("/attendances/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isCreated())
                        .andExpect(jsonPath("$", hasSize(2)))
                        .andExpectAll(context.getMatchers());

                securePerform(get("/attendances/course/{id}", courseId))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$", hasSize(2)))
                        .andExpectAll(context.getMatchers("$.content"));

                securePerform(get("/attendances/course/{id}", courseId))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$", hasSize(2)))
                        .andExpectAll(context.getMatchers("$.content"));
            }
        };
    }

    @Test
    void batchCreateAttendance__otherAsTeacher__invalid() {
        new WithUser(USERNAME, PASSWORD, UserRole.TEACHER) {
            @Override
            void run() throws Exception {
                long userId = ef.createEmployee();
                long courseId = ef.createCourse(userId);
                long studentId = ef.createStudent(ef.bag().withCourseId(courseId));
                RequestContext<ObjectNode> context = getBatchCreateAttendanceRequest(studentId, courseId);

                ObjectNode request = context.getRequest();

                securePerform(post("/attendances/batch")
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

        mvc.perform(post("/attendances/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                .andExpect(status().isUnauthorized());
    }

    // =================================================================================================================

    RequestContext<ObjectNode> getPutAttendanceRequest(long studentId, long courseId,
                                                       LocalDate attendedDate, int attendedClass) {
        AttendanceType attendanceType = TestUtils.randomAttendanceType();

        ObjectNode request = objectMapper.createObjectNode()
                .put("student", studentId)
                .put("course", courseId)
                .put("attendedClass", String.valueOf(attendedClass))
                .put("attendanceType", String.valueOf(attendanceType))
                .put("attendedDate", String.valueOf(attendedDate));

        Function<String, ResultMatcher[]> matchers = prefix -> new ResultMatcher[]{
                jsonPath("$.id").isNumber(),
                jsonPath("$.student").value(studentId),
                jsonPath("$.course").value(courseId),
                jsonPath("$.attendedClass").value(attendedClass),
                jsonPath("$.attendanceType").value(attendanceType.toString()),
                jsonPath("$.attendedDate").value(attendedDate.toString())
        };

        return new RequestContext<>(request, matchers);

    }

    @Test
    void putAttendance__self__valid() {
        new WithUser(USERNAME, PASSWORD, UserRole.TEACHER) {
            @Override
            void run() throws Exception {
                long courseId = ef.createCourse(getSelfEmployeeIdAsLong());
                long studentId = ef.createStudent(ef.bag().withCourseId(courseId));
                LocalDate attendedDate = LocalDate.now();
                AttendanceType attendanceType = TestUtils.randomAttendanceType();
                int attendedClass = faker.number().numberBetween(1, 6);
                long attendanceId = attendanceService.create(AttendanceDto.builder()
                        .course(courseId)
                        .student(studentId)
                        .attendedDate(attendedDate)
                        .attendedClass(attendedClass)
                        .attendanceType(attendanceType)
                        .build()).getId();

                RequestContext<ObjectNode> context = getPutAttendanceRequest(studentId, courseId, attendedDate, attendedClass);
                ObjectNode request = context.getRequest();

                securePerform(put("/attendances/{id}", attendanceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isOk())
                        .andExpectAll(context.getMatchers());

                securePerform(get("/attendances/{id}", attendanceId))
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
                long userId = ef.createEmployee();
                long courseId = ef.createCourse(userId);
                long studentId = ef.createStudent(ef.bag().withCourseId(courseId));
                LocalDate attendedDate = LocalDate.now();
                AttendanceType attendanceType = TestUtils.randomAttendanceType();
                int attendedClass = faker.number().numberBetween(1, 6);
                long attendanceId = attendanceService.create(AttendanceDto.builder()
                        .course(courseId)
                        .student(studentId)
                        .attendedDate(attendedDate)
                        .attendedClass(attendedClass)
                        .attendanceType(attendanceType)
                        .build()).getId();

                RequestContext<ObjectNode> context = getPutAttendanceRequest(studentId, courseId, attendedDate, attendedClass);
                ObjectNode request = context.getRequest();

                securePerform(put("/attendances/{id}", attendanceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isOk())
                        .andExpectAll(context.getMatchers());

                securePerform(get("/attendances/{id}", attendanceId))
                        .andExpect(status().isOk())
                        .andExpectAll(context.getMatchers());
            }
        };
    }

    @Test
    void putAttendance__otherAsTeacher__invalid() {
        new WithUser(USERNAME, PASSWORD, UserRole.TEACHER) {
            @Override
            void run() throws Exception {
                long courseId = ef.createCourse();
                long studentId = ef.createStudent(ef.bag().withCourseId(courseId));
                LocalDate attendedDate = LocalDate.now();
                AttendanceType attendanceType = TestUtils.randomAttendanceType();
                int attendedClass = faker.number().numberBetween(1, 6);

                long attendanceId = attendanceService.create(AttendanceDto.builder()
                        .course(courseId)
                        .student(studentId)
                        .attendedDate(attendedDate)
                        .attendedClass(attendedClass)
                        .attendanceType(attendanceType)
                        .build()).getId();

                RequestContext<ObjectNode> context = getPutAttendanceRequest(studentId, courseId, attendedDate, attendedClass);
                ObjectNode request = context.getRequest();

                securePerform(put("/attendances/{id}", attendanceId)
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
                long courseId = ef.createCourse();
                long studentId1 = ef.createStudent(ef.bag().withCourseId(courseId));
                long studentId2 = ef.createStudent(ef.bag().withCourseId(courseId));
                LocalDate attendedDate = LocalDate.now();
                AttendanceType attendanceType = TestUtils.randomAttendanceType();
                int attendedClass = faker.number().numberBetween(1, 6);
                long attendanceId = attendanceService.create(AttendanceDto.builder()
                        .course(courseId)
                        .student(studentId1)
                        .attendedDate(attendedDate)
                        .attendedClass(attendedClass)
                        .attendanceType(attendanceType)
                        .build()).getId();

                RequestContext<ObjectNode> context = getPutAttendanceRequest(studentId2, courseId, attendedDate, attendedClass);
                ObjectNode request = context.getRequest();

                securePerform(put("/attendances/{id}", attendanceId)
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
                long courseId1 = ef.createCourse();
                long courseId2 = ef.createCourse();
                long studentId = ef.createStudent(ef.bag().withCourseId(courseId1));
                LocalDate attendedDate = LocalDate.now();
                AttendanceType attendanceType = TestUtils.randomAttendanceType();
                int attendedClass = faker.number().numberBetween(1, 6);
                long attendanceId = attendanceService.create(AttendanceDto.builder()
                        .course(courseId1)
                        .student(studentId)
                        .attendedDate(attendedDate)
                        .attendedClass(attendedClass)
                        .attendanceType(attendanceType)
                        .build()).getId();

                RequestContext<ObjectNode> context = getPutAttendanceRequest(studentId, courseId2, attendedDate, attendedClass);
                ObjectNode request = context.getRequest();

                securePerform(put("/attendances/{id}", attendanceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isBadRequest());
            }
        };
    }

    @Test
    void putAttendance__dateChange__invalid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                long courseId = ef.createCourse();
                long studentId = ef.createStudent(ef.bag().withCourseId(courseId));
                LocalDate attendedDate1 = LocalDate.now();
                AttendanceType attendanceType = TestUtils.randomAttendanceType();
                int attendedClass = faker.number().numberBetween(1, 6);
                LocalDate attendedDate2 = attendedDate1.plusDays(1);

                long attendanceId = ef.createAttendance(ef.bag()
                        .withCourseId(courseId)
                        .withStudentId(studentId)
                        .withDto(AttendanceDto.builder()
                                .attendedDate(attendedDate1)
                                .attendanceType(attendanceType)
                                .attendedClass(attendedClass)
                                .build()));

                RequestContext<ObjectNode> context = getPutAttendanceRequest(studentId, courseId, attendedDate2, attendedClass);
                ObjectNode request = context.getRequest();

                securePerform(put("/attendances/{id}", attendanceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isBadRequest());
            }
        };
    }

    @Test
    void putAttendance__classChange__invalid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                long courseId = ef.createCourse();
                long studentId = ef.createStudent(ef.bag().withCourseId(courseId));
                LocalDate attendedDate = LocalDate.now();
                AttendanceType attendanceType = TestUtils.randomAttendanceType();
                int attendedClass1 = faker.number().numberBetween(1, 5);
                int attendedClass2 = attendedClass1 + 1;

                long attendanceId = ef.createAttendance(ef.bag()
                        .withCourseId(courseId)
                        .withStudentId(studentId)
                        .withDto(AttendanceDto.builder()
                                .attendedDate(attendedDate)
                                .attendanceType(attendanceType)
                                .attendedClass(attendedClass1)
                                .build()));

                RequestContext<ObjectNode> context = getPutAttendanceRequest(studentId, courseId, attendedDate, attendedClass2);
                ObjectNode request = context.getRequest();

                securePerform(put("/attendances/{id}", attendanceId)
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
                long courseId = ef.createCourse();
                long studentId = ef.createStudent(ef.bag().withCourseId(courseId));
                LocalDate attendedDate = LocalDate.now();
                AttendanceType attendanceType = TestUtils.randomAttendanceType();
                int attendedClass = faker.number().numberBetween(1, 6);

                long attendanceId = attendanceService.create(AttendanceDto.builder()
                        .course(courseId)
                        .student(studentId)
                        .attendedDate(attendedDate)
                        .attendedClass(attendedClass)
                        .attendanceType(attendanceType)
                        .build()).getId();

                RequestContext<ObjectNode> context = getPutAttendanceRequest(studentId, courseId, attendedDate, attendedClass);
                ObjectNode request = context.getRequest();

                securePerform(put("/attendances/{id}", attendanceId + 1000)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isNotFound());
            }
        };
    }

    @Test
    void putAttendance__notAuthenticated__invalid() throws Exception {
        long courseId = ef.createCourse();
        long studentId = ef.createStudent(ef.bag().withCourseId(courseId));
        LocalDate attendedDate = LocalDate.now();
        AttendanceType attendanceType = TestUtils.randomAttendanceType();
        int attendedClass = faker.number().numberBetween(1, 6);

        long attendanceId = attendanceService.create(AttendanceDto.builder()
                .course(courseId)
                .student(studentId)
                .attendedDate(attendedDate)
                .attendedClass(attendedClass)
                .attendanceType(attendanceType)
                .build()).getId();

        RequestContext<ObjectNode> context = getPutAttendanceRequest(studentId, courseId, attendedDate, attendedClass);
        ObjectNode request = context.getRequest();

        mvc.perform(put("/attendances/{id}", attendanceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                .andExpect(status().isUnauthorized());
    }

    // =================================================================================================================

    RequestContext<ObjectNode> getPatchAttendanceRequest(long studentId, long courseId) {
        AttendanceType attendanceType = AttendanceType.SERIOUS_REASON;

        ObjectNode request = objectMapper.createObjectNode()
                .put("student", studentId)
                .put("course", courseId)
                .put("attendanceType", String.valueOf(attendanceType));

        Function<String, ResultMatcher[]> matchers = prefix -> new ResultMatcher[]{
                jsonPath("$.id").isNumber(),
                jsonPath("$.student").value(studentId),
                jsonPath("$.course").value(courseId),
                jsonPath("$.attendedClass").isNotEmpty(),
                jsonPath("$.attendanceType").value(attendanceType.toString()),
                jsonPath("$.attendedDate").isNotEmpty()
        };

        return new RequestContext<>(request, matchers);
    }

    RequestContext<ObjectNode> getPatchAttendanceRequest(long studentId, long courseId, Integer attendedClass, LocalDate attendedDate) {
        AttendanceType attendanceType = AttendanceType.SERIOUS_REASON;

        ObjectNode request = objectMapper.createObjectNode()
                .put("student", studentId)
                .put("course", courseId)
                .put("attendedDate", attendedDate.toString())
                .put("attendedClass", attendedClass)
                .put("attendanceType", String.valueOf(attendanceType));

        Function<String, ResultMatcher[]> matchers = prefix -> new ResultMatcher[]{
                jsonPath("$.id").isNumber(),
                jsonPath("$.student").value(studentId),
                jsonPath("$.course").value(courseId),
                jsonPath("$.attendedClass").value(attendedClass),
                jsonPath("$.attendanceType").value(attendanceType.toString()),
                jsonPath("$.attendedDate").value(attendedDate.toString())
        };

        return new RequestContext<>(request, matchers);
    }

    @Test
    void patchAttendance__self__valid() {
        new WithUser(USERNAME, PASSWORD, UserRole.TEACHER) {
            @Override
            void run() throws Exception {
                long courseId = ef.createCourse(getSelfEmployeeIdAsLong());
                long studentId = ef.createStudent(ef.bag().withCourseId(courseId));
                int attendedClass = faker.number().numberBetween(1, 6);
                LocalDate attendedDate = LocalDate.now();
                AttendanceType attendanceType = AttendanceType.ATTENDED;

                long attendanceId = ef.createAttendance(ef.bag()
                        .withStudentId(studentId)
                        .withCourseId(courseId)
                        .withDto(AttendanceDto.builder()
                                .attendedClass(attendedClass)
                                .attendedDate(attendedDate)
                                .attendanceType(attendanceType)
                                .build()
                        )
                );

                RequestContext<ObjectNode> context = getPatchAttendanceRequest(studentId, courseId);
                ObjectNode request = context.getRequest();

                securePerform(patch("/attendances/{id}", attendanceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isOk())
                        .andExpectAll(context.getMatchers());

                securePerform(get("/attendances/{id}", attendanceId))
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
                long userId = ef.createEmployee();
                long courseId = ef.createCourse(userId);
                long studentId = ef.createStudent(ef.bag().withCourseId(courseId));
                int attendedClass = faker.number().numberBetween(1, 6);
                LocalDate attendedDate = LocalDate.now();
                AttendanceType attendanceType = AttendanceType.ATTENDED;

                long attendanceId = ef.createAttendance(ef.bag()
                        .withStudentId(studentId)
                        .withCourseId(courseId)
                        .withDto(AttendanceDto.builder()
                                .attendedClass(attendedClass)
                                .attendedDate(attendedDate)
                                .attendanceType(attendanceType)
                                .build()
                        )
                );

                RequestContext<ObjectNode> context = getPatchAttendanceRequest(studentId, courseId);
                ObjectNode request = context.getRequest();

                securePerform(patch("/attendances/{id}", attendanceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isOk())
                        .andExpectAll(context.getMatchers());

                securePerform(get("/attendances/{id}", attendanceId))
                        .andExpect(status().isOk())
                        .andExpectAll(context.getMatchers());
            }
        };
    }

    @Test
    void patchAttendance__otherAsTeacher__invalid() {
        new WithUser(USERNAME, PASSWORD, UserRole.TEACHER) {
            @Override
            void run() throws Exception {
                long courseId = ef.createCourse();
                long studentId = ef.createStudent(ef.bag().withCourseId(courseId));
                int attendedClass = faker.number().numberBetween(1, 6);
                LocalDate attendedDate = LocalDate.now();
                AttendanceType attendanceType = AttendanceType.ATTENDED;

                long attendanceId = ef.createAttendance(ef.bag()
                        .withStudentId(studentId)
                        .withCourseId(courseId)
                        .withDto(AttendanceDto.builder()
                                .attendedClass(attendedClass)
                                .attendedDate(attendedDate)
                                .attendanceType(attendanceType)
                                .build()
                        )
                );

                RequestContext<ObjectNode> context = getPatchAttendanceRequest(studentId, courseId);
                ObjectNode request = context.getRequest();

                securePerform(patch("/attendances/{id}", attendanceId)
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
                long courseId = ef.createCourse();
                long studentId1 = ef.createStudent(ef.bag().withCourseId(courseId));
                long studentId2 = ef.createStudent(ef.bag().withCourseId(courseId));
                int attendedClass = faker.number().numberBetween(1, 6);
                LocalDate attendedDate = LocalDate.now();
                AttendanceType attendanceType = AttendanceType.ATTENDED;

                long attendanceId = ef.createAttendance(ef.bag()
                        .withStudentId(studentId1)
                        .withCourseId(courseId)
                        .withDto(AttendanceDto.builder()
                                .attendedClass(attendedClass)
                                .attendedDate(attendedDate)
                                .attendanceType(attendanceType)
                                .build()
                        )
                );

                RequestContext<ObjectNode> context = getPatchAttendanceRequest(studentId2, courseId);
                ObjectNode request = context.getRequest();

                securePerform(put("/attendances/{id}", attendanceId)
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
                long courseId1 = ef.createCourse();
                long courseId2 = ef.createCourse();
                long studentId = ef.createStudent(ef.bag().withCourseId(courseId1));
                LocalDate attendedDate = LocalDate.now();
                int attendedClass = faker.number().numberBetween(1, 6);
                AttendanceType attendanceType = AttendanceType.ATTENDED;

                long attendanceId = ef.createAttendance(ef.bag()
                        .withStudentId(studentId)
                        .withCourseId(courseId1)
                        .withDto(AttendanceDto.builder()
                                .attendedClass(attendedClass)
                                .attendedDate(attendedDate)
                                .attendanceType(attendanceType)
                                .build()
                        )
                );

                RequestContext<ObjectNode> context = getPatchAttendanceRequest(studentId, courseId2);
                ObjectNode request = context.getRequest();

                securePerform(put("/attendances/{id}", attendanceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isBadRequest());
            }
        };
    }

    @Test
    void patchAttendance__dateChange__invalid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                long courseId = ef.createCourse();
                long studentId = ef.createStudent(ef.bag().withCourseId(courseId));
                LocalDate attendedDate1 = LocalDate.now();
                LocalDate attendedDate2 = attendedDate1.plusDays(1);
                int attendedClass = faker.number().numberBetween(1, 6);
                AttendanceType attendanceType = AttendanceType.ATTENDED;

                long attendanceId = ef.createAttendance(ef.bag()
                        .withStudentId(studentId)
                        .withCourseId(courseId)
                        .withDto(AttendanceDto.builder()
                                .attendedClass(attendedClass)
                                .attendedDate(attendedDate1)
                                .attendanceType(attendanceType)
                                .build()
                        )
                );

                RequestContext<ObjectNode> context = getPatchAttendanceRequest(studentId, courseId, attendedClass, attendedDate2);
                ObjectNode request = context.getRequest();

                securePerform(put("/attendances/{id}", attendanceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isBadRequest());
            }
        };
    }

    @Test
    void patchAttendance__classChange__invalid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                long courseId = ef.createCourse();
                long studentId = ef.createStudent(ef.bag().withCourseId(courseId));
                LocalDate attendedDate = LocalDate.now();
                int attendedClass1 = faker.number().numberBetween(1, 5);
                int attendedClass2 = attendedClass1 + 1;
                AttendanceType attendanceType = AttendanceType.ATTENDED;

                long attendanceId = ef.createAttendance(ef.bag()
                        .withStudentId(studentId)
                        .withCourseId(courseId)
                        .withDto(AttendanceDto.builder()
                                .attendedClass(attendedClass1)
                                .attendedDate(attendedDate)
                                .attendanceType(attendanceType)
                                .build()
                        )
                );

                RequestContext<ObjectNode> context = getPatchAttendanceRequest(studentId, courseId, attendedClass2, attendedDate);
                ObjectNode request = context.getRequest();

                securePerform(put("/attendances/{id}", attendanceId)
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
                long courseId = ef.createCourse();
                long studentId = ef.createStudent(ef.bag().withCourseId(courseId));
                LocalDate attendedDate = LocalDate.now();
                int attendedClass = faker.number().numberBetween(1, 6);
                AttendanceType attendanceType = AttendanceType.ATTENDED;

                long attendanceId = ef.createAttendance(ef.bag()
                        .withStudentId(studentId)
                        .withCourseId(courseId)
                        .withDto(AttendanceDto.builder()
                                .attendedClass(attendedClass)
                                .attendedDate(attendedDate)
                                .attendanceType(attendanceType)
                                .build()
                        )
                );

                RequestContext<ObjectNode> context = getPatchAttendanceRequest(studentId, courseId);
                ObjectNode request = context.getRequest();

                securePerform(put("/attendances/{id}", attendanceId + 1000)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                        .andExpect(status().isNotFound());
            }
        };
    }

    @Test
    void patchAttendance__notAuthenticated__invalid() throws Exception {
        long studentId = ef.createStudent();
        long courseId = ef.createCourse();
        LocalDate attendedDate = LocalDate.now();
        int attendedClass = faker.number().numberBetween(1, 6);
        AttendanceType attendanceType = AttendanceType.ATTENDED;

        long attendanceId = ef.createAttendance(ef.bag()
                .withStudentId(studentId)
                .withCourseId(courseId)
                .withDto(AttendanceDto.builder()
                        .attendedClass(attendedClass)
                        .attendedDate(attendedDate)
                        .attendanceType(attendanceType)
                        .build()
                )
        );

        RequestContext<ObjectNode> context = getPatchAttendanceRequest(studentId, courseId);
        ObjectNode request = context.getRequest();

        mvc.perform(put("/attendances/{id}", attendanceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.toString()))
                .andExpect(status().isUnauthorized());
    }

    // =================================================================================================================

    @Test
    void deleteAttendance__self__valid() {
        new WithUser(USERNAME, PASSWORD, UserRole.TEACHER) {
            @Override
            void run() throws Exception {
                long attendanceId = ef.createAttendance(getSelfEmployeeIdAsLong());

                securePerform(delete("/attendances/{id}", attendanceId))
                        .andExpectAll(status().isOk());

                securePerform(get("/attendances/{id}", attendanceId))
                        .andExpectAll(status().isNotFound());
            }
        };
    }

    @Test
    void deleteAttendance__otherAsAdmin__valid() {
        new WithUser(ADMIN_USERNAME, ADMIN_PASSWORD, false) {
            @Override
            void run() throws Exception {
                long attendanceId = ef.createAttendance();

                securePerform(delete("/attendances/{id}", attendanceId))
                        .andExpectAll(status().isOk());

                securePerform(get("/attendances/{id}", attendanceId))
                        .andExpectAll(status().isNotFound());
            }
        };
    }

    @Test
    void deleteAttendance__otherAsTeacher__invalid() {
        new WithUser(USERNAME, PASSWORD, UserRole.TEACHER) {
            @Override
            void run() throws Exception {
                long attendanceId = ef.createAttendance();

                securePerform(delete("/attendances/{id}", attendanceId))
                        .andExpectAll(status().isForbidden());
            }
        };
    }

    @Test
    void deleteAttendance__notFound__invalid() {
        new WithUser(USERNAME, PASSWORD, UserRole.TEACHER) {
            @Override
            void run() throws Exception {
                long attendanceId = ef.createAttendance(getSelfEmployeeIdAsLong());

                securePerform(delete("/attendances/{id}", attendanceId + 1000))
                        .andExpectAll(status().isNotFound());
            }
        };
    }

    @Test
    void deleteAttendance__notAuthenticated__invalid() throws Exception {
        long attendanceId = ef.createAttendance();

        mvc.perform(delete("/attendances/{id}", attendanceId))
                .andExpect(status().isUnauthorized());
    }
}
