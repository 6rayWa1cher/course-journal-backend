package com.a6raywa1cher.coursejournalbackend.service.impl;

import com.a6raywa1cher.coursejournalbackend.dto.AttendanceConflictListDto;
import com.a6raywa1cher.coursejournalbackend.dto.AttendanceDto;
import com.a6raywa1cher.coursejournalbackend.dto.StudentDto;
import com.a6raywa1cher.coursejournalbackend.dto.TableDto;
import com.a6raywa1cher.coursejournalbackend.dto.exc.*;
import com.a6raywa1cher.coursejournalbackend.dto.mapper.MapStructMapper;
import com.a6raywa1cher.coursejournalbackend.model.Attendance;
import com.a6raywa1cher.coursejournalbackend.model.Course;
import com.a6raywa1cher.coursejournalbackend.model.Employee;
import com.a6raywa1cher.coursejournalbackend.model.Student;
import com.a6raywa1cher.coursejournalbackend.model.repo.AttendanceRepository;
import com.a6raywa1cher.coursejournalbackend.service.AttendanceService;
import com.a6raywa1cher.coursejournalbackend.service.CourseService;
import com.a6raywa1cher.coursejournalbackend.service.StudentService;
import com.a6raywa1cher.coursejournalbackend.utils.CommonUtils;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class AttendanceServiceImpl implements AttendanceService {
    private final AttendanceRepository repository;

    private final MapStructMapper mapper;

    private StudentService studentService;

    private CourseService courseService;

    public AttendanceServiceImpl(AttendanceRepository attendanceRepository, MapStructMapper mapper) {
        this.repository = attendanceRepository;
        this.mapper = mapper;
    }

    @Override
    public AttendanceDto getById(long id) {
        return mapper.map(getAttendanceById(id));
    }

    @Override
    public Optional<Attendance> findRawById(long id) {
        return repository.findById(id);
    }

    @Override
    public List<AttendanceDto> getByCourseId(long courseId, Sort sort) {
        Course course = getCourseById(courseId);
        return repository.getAllByCourse(course, sort).stream()
                .map(mapper::map)
                .toList();
    }

    @Override
    public List<AttendanceDto> getByCourseAndStudentIds(long courseId, long studentId, Sort sort) {
        Course course = getCourseById(courseId);
        Student student = getStudentById(studentId);
        return repository.getAllByStudentAndCourse(student, course, sort).stream()
                .map(mapper::map)
                .toList();
    }

    @Override
    public TableDto getAttendancesTableByDatePeriod(long courseId, LocalDate fromDate, LocalDate toDate) {
        assertFromDateBeforeToDate(fromDate, toDate);
        Course course = getCourseById(courseId);
        List<Attendance> attendances = repository.getAllByCourseAndAttendedDateBetween(
                course,
                fromDate,
                toDate,
                Sort.by("attendedDate", "attendedClass")
        );
        TableDto tableDto = new TableDto();
        if (attendances.size() == 0) {
            List<StudentDto> studentsDto = studentService.getByCourseId(courseId, Sort.by("id"));
            for (StudentDto studentDto : studentsDto) {
                tableDto.addTableBodyElement(studentDto.getId(), 0);
            }
            return tableDto;
        }
        Map<Student, Integer> studentsToIndexMap = new HashMap<>();
        for (Attendance attendance : attendances) {
            Student currentStudent = attendance.getStudent();
            studentsToIndexMap.put(currentStudent, null);
            tableDto.addTableHeaderElement(attendance.getAttendedDate(), attendance.getAttendedClass());
        }
        for (Student student : studentsToIndexMap.keySet()) {
            tableDto.addTableBodyElement(student.getId(), tableDto.getHeader().size());
            studentsToIndexMap.replace(student, tableDto.getBody().size() - 1);
        }
        int indexOfHeaderElement = 0;
        int classNumber = attendances.get(0).getAttendedClass();
        LocalDate date = attendances.get(0).getAttendedDate();
        for (Attendance attendance : attendances) {
            int currentClassNumber = attendance.getAttendedClass();
            LocalDate currentDate = attendance.getAttendedDate();
            if (currentDate != date || currentClassNumber != classNumber) {
                indexOfHeaderElement++;
                classNumber = currentClassNumber;
                date = currentDate;
            }
            tableDto.addAttendanceToBody(studentsToIndexMap.get(attendance.getStudent()), indexOfHeaderElement,
                    attendance.getAttendanceType());
        }
        return tableDto;
    }

    @Override
    public AttendanceConflictListDto getAttendanceConflictsByDatePeriodAndClass(long courseId, LocalDate fromDate, LocalDate toDate) {
        assertFromDateBeforeToDate(fromDate, toDate);
        Course course = getCourseById(courseId);
        AttendanceConflictListDto conflicts = new AttendanceConflictListDto();
        List<Attendance> attendances = repository.getAllConflictsByCourseAndDatePeriod(
                course,
                fromDate,
                toDate,
                Sort.by("attendedDate", "attendedClass")
        );
        if (attendances.size() == 0) {
            return conflicts;
        }
        Employee teacher = attendances.get(0).getCourse().getOwner();
        String teacherFullName = teacher.getLastName() + ' ' + teacher.getFirstName() + (teacher.getMiddleName() != null ? ' ' + teacher.getMiddleName() : "");
        for (int i = 0; i < attendances.size(); i++) {
            Attendance attendance = attendances.get(i);
            AttendanceConflictListDto.AttendanceConflict newConflict = new AttendanceConflictListDto.AttendanceConflict(
                    teacherFullName,
                    attendance.getCourse().getName(),
                    attendance.getStudent().getId(),
                    attendance.getAttendedDate(),
                    attendance.getAttendedClass(),
                    attendance.getAttendanceType()
            );
            conflicts.addAttendanceConflictToList(newConflict);
        }
        return conflicts;
    }

    @Override
    public AttendanceDto create(AttendanceDto dto) {
        Course course = getCourseById(dto.getCourse());

        return mapper.map(repository.save(getAttendanceByDtoAndCourse(dto, course)));
    }

    @Override
    public List<AttendanceDto> batchCreate(List<AttendanceDto> dtoList) {
        List<Attendance> attendances = new ArrayList<>();
        Course course = extractCourse(dtoList);

        for (AttendanceDto dto : dtoList) {
            attendances.add(getAttendanceByDtoAndCourse(dto, course));
        }

        return StreamSupport.stream(repository.saveAll(attendances).spliterator(), false)
                .map(mapper::map)
                .toList();
    }

    @Override
    public AttendanceDto update(long id, AttendanceDto dto) {
        Attendance attendance = getAttendanceById(id);
        Student student = getStudentById(dto.getStudent());
        Course course = getCourseById(dto.getCourse());
        LocalDate attendedDate = dto.getAttendedDate();
        Integer attendedClass = dto.getAttendedClass();

        assertNoStudentChanged(attendance.getStudent(), student);
        assertNoCourseChanged(attendance.getCourse(), course);
        assertNoAttendanceClassChanged(attendance.getAttendedClass(), attendedClass);
        assertNoAttendanceDateChanged(attendance.getAttendedDate(), attendedDate);
        mapper.put(dto, attendance);

        attendance.setLastModifiedAt(LocalDateTime.now());

        return mapper.map(repository.save(attendance));
    }

    @Override
    public AttendanceDto patch(long id, AttendanceDto dto) {
        Attendance attendance = getAttendanceById(id);
        Student student = dto.getStudent() != null ? getStudentById(dto.getStudent()) : attendance.getStudent();
        Course course = dto.getCourse() != null ? getCourseById(dto.getCourse()) : attendance.getCourse();
        LocalDate attendedDate = dto.getAttendedDate();
        Integer attendedClass = dto.getAttendedClass();

        assertNoStudentChanged(attendance.getStudent(), student);
        assertNoCourseChanged(attendance.getCourse(), course);
        assertNoAttendanceClassChanged(attendance.getAttendedClass(), CommonUtils.coalesce(attendedClass, attendance.getAttendedClass()));
        assertNoAttendanceDateChanged(attendance.getAttendedDate(), CommonUtils.coalesce(attendedDate, attendance.getAttendedDate()));
        mapper.patch(dto, attendance);

        attendance.setLastModifiedAt(LocalDateTime.now());

        return mapper.map(repository.save(attendance));
    }

    @Override
    public void delete(long id) {
        Attendance attendance = getAttendanceById(id);
        repository.delete(attendance);
    }

    private Attendance getAttendanceById(long id) {
        return repository.findById(id).orElseThrow(() -> new NotFoundException(Attendance.class, id));
    }

    private Student getStudentById(long id) {
        return studentService.findRawById(id).orElseThrow(() -> new NotFoundException(Student.class, id));
    }

    private Course getCourseById(long id) {
        return courseService.findRawById(id).orElseThrow(() -> new NotFoundException(Course.class, id));
    }

    private void assertUniqueByStudentAttendedDateAndAttendedClass(Student student, LocalDate date, Integer attendedClass) {
        if (repository.findByStudentAndAttendedDateAndAttendedClass(student, date, attendedClass).isPresent()) {
            throw new ConflictException(Attendance.class,
                    "student", Long.toString(student.getId()),
                    "date", date.toString(),
                    "attendedClass", Integer.toString(attendedClass));
        }
    }

    private void assertFromDateBeforeToDate(LocalDate fromDate, LocalDate toDate) {
        if (fromDate.isAfter(toDate)) {
            throw new WrongDatesException(fromDate.toString(), toDate.toString());
        }
    }

    private void assertNoCourseChanged(Course oldCourse, Course newCourse) {
        if (!Objects.equals(oldCourse, newCourse)) {
            throw new TransferNotAllowedException(Attendance.class, "attendance", oldCourse.getId(), newCourse.getId());
        }
    }

    private void assertNoStudentChanged(Student oldStudent, Student newStudent) {
        if (!Objects.equals(oldStudent, newStudent)) {
            throw new TransferNotAllowedException(Attendance.class, "attendance", oldStudent.getId(), newStudent.getId());
        }
    }

    private void assertNoAttendanceDateChanged(LocalDate oldDate, LocalDate newDate) {
        if (!Objects.equals(oldDate, newDate)) {
            throw new TransferNotAllowedException(Attendance.class, "attendance", oldDate.toString(), newDate.toString());
        }
    }

    private void assertNoAttendanceClassChanged(Integer oldClass, Integer newClass) {
        if (!Objects.equals(oldClass, newClass)) {
            throw new TransferNotAllowedException(Attendance.class, "attendance", oldClass.toString(), newClass.toString());
        }
    }

    private Course extractCourse(List<AttendanceDto> dtoList) {
        Set<Long> courseIds = dtoList.stream()
                .map(AttendanceDto::getCourse)
                .collect(Collectors.toSet());
        if (courseIds.size() != 1) {
            throw new VariousParentEntitiesException(new ArrayList<>(courseIds));
        }
        return getCourseById(courseIds.iterator().next());
    }

    private Attendance getAttendanceByDtoAndCourse(AttendanceDto dto, Course course) {
        Attendance attendance = new Attendance();
        Student student = getStudentById(dto.getStudent());
        Integer attendedClass = dto.getAttendedClass();
        LocalDate attendedDate = dto.getAttendedDate();
        LocalDateTime createAndModifyDateTime = LocalDateTime.now();

        assertUniqueByStudentAttendedDateAndAttendedClass(student, attendedDate, attendedClass);
        mapper.put(dto, attendance);

        attendance.setStudent(student);
        attendance.setCourse(course);
        attendance.setAttendedClass(attendedClass);
        attendance.setAttendedDate(attendedDate);
        attendance.setCreatedAt(createAndModifyDateTime);
        attendance.setLastModifiedAt(createAndModifyDateTime);

        return attendance;
    }

    @Autowired
    public void setStudentService(StudentService studentService) {
        this.studentService = studentService;
    }

    @Autowired
    public void setCourseService(CourseService courseService) {
        this.courseService = courseService;
    }
}
