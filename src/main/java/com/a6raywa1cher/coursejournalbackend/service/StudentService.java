package com.a6raywa1cher.coursejournalbackend.service;

import com.a6raywa1cher.coursejournalbackend.dto.StudentDto;
import com.a6raywa1cher.coursejournalbackend.model.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface StudentService {
    StudentDto getById(long id);

    Optional<Student> findRawById(long id);

    List<Student> findRawById(List<Long> ids);

    Page<StudentDto> getByCourseId(long courseId, Pageable pageable);

    List<StudentDto> getByCourseId(long courseId);

    List<StudentDto> getByGroupId(long groupId);

    StudentDto create(StudentDto dto);

    List<StudentDto> batchCreate(List<StudentDto> dtoList);

    StudentDto update(long id, StudentDto dto);

    StudentDto patch(long id, StudentDto dto);

    void delete(long id);
}
