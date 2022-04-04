package com.a6raywa1cher.coursejournalbackend.service.impl;

import com.a6raywa1cher.coursejournalbackend.dto.AttendanceDto;
import com.a6raywa1cher.coursejournalbackend.model.Attendance;
import com.a6raywa1cher.coursejournalbackend.service.AttendanceService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AttendanceServiceImpl implements AttendanceService {
    @Override
    public AttendanceDto getById(long id) {
        return null;
    }

    @Override
    public Optional<Attendance> findRawById(long id) {
        return Optional.empty();
    }

    @Override
    public List<AttendanceDto> getByStudentId(long id) {
        return null;
    }

    @Override
    public List<AttendanceDto> getByCourseId(long id) {
        return null;
    }

    @Override
    public AttendanceDto create(AttendanceDto dto) {
        return null;
    }

    @Override
    public List<AttendanceDto> batchCreate(List<AttendanceDto> dtoList) {
        return null;
    }

    @Override
    public AttendanceDto update(long id, AttendanceDto dto) {
        return null;
    }

    @Override
    public AttendanceDto patch(long id, AttendanceDto dto) {
        return null;
    }

    @Override
    public void delete(long id) {

    }
}
