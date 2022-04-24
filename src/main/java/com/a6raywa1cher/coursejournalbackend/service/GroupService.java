package com.a6raywa1cher.coursejournalbackend.service;

import com.a6raywa1cher.coursejournalbackend.dto.GroupDto;
import com.a6raywa1cher.coursejournalbackend.model.Group;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;

public interface GroupService {
    GroupDto getById(long id);

    Optional<Group> findRawById(long id);

    List<GroupDto> getByFaculty(Long facultyId, Sort sort);

    List<GroupDto> getByCourse(long courseId, Sort sort);

    List<GroupDto> getByFacultyAndCourse(Long facultyId, long courseId, Sort sort);

    GroupDto create(GroupDto dto);

    GroupDto update(long id, GroupDto dto);

    GroupDto patch(long id, GroupDto dto);

    void delete(long id);
}
