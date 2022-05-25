package com.a6raywa1cher.coursejournalbackend.service;

import com.a6raywa1cher.coursejournalbackend.dto.AttendanceConflictListDto;
import com.a6raywa1cher.coursejournalbackend.dto.AttendanceDto;
import com.a6raywa1cher.coursejournalbackend.dto.TableDto;
import com.a6raywa1cher.coursejournalbackend.model.Attendance;
import org.springframework.data.domain.Sort;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AttendanceService {
    AttendanceDto getById(long id);

    Optional<Attendance> findRawById(long id);

    List<AttendanceDto> getByCourseAndStudentIds(long courseId, long studentId, Sort sort);

    List<AttendanceDto> getByCourseId(long courseId, Sort sort);

    TableDto getAttendancesTableByDatePeriod(long courseId, LocalDate start, LocalDate end);

    AttendanceConflictListDto getAttendanceConflictsByDatePeriodAndClass(long courseId, LocalDate start, LocalDate end);

    AttendanceDto create(AttendanceDto dto);

    List<AttendanceDto> batchCreate(List<AttendanceDto> dtoList);

    AttendanceDto update(long id, AttendanceDto dto);

    AttendanceDto patch(long id, AttendanceDto dto);

    void delete(long id);
}
