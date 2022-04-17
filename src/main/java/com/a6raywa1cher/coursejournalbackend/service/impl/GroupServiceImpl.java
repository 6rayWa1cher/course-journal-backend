package com.a6raywa1cher.coursejournalbackend.service.impl;

import com.a6raywa1cher.coursejournalbackend.dto.GroupDto;
import com.a6raywa1cher.coursejournalbackend.model.Group;
import com.a6raywa1cher.coursejournalbackend.service.GroupService;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class GroupServiceImpl implements GroupService {
    @Override
    public GroupDto getById(long id) {
        return null;
    }

    @Override
    public Optional<Group> findRawById(long id) {
        return Optional.empty();
    }

    @Override
    public List<GroupDto> getByFacultyName(String faculty, Sort sort) {
        return null;
    }

    @Override
    public List<GroupDto> getByCourse(long courseId, Sort sort) {
        return null;
    }

    @Override
    public GroupDto create(GroupDto dto) {
        return null;
    }

    @Override
    public GroupDto update(long id, GroupDto dto) {
        return null;
    }

    @Override
    public GroupDto patch(long id, GroupDto dto) {
        return null;
    }

    @Override
    public void delete(long id) {

    }
}
