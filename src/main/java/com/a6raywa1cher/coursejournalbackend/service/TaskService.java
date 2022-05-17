package com.a6raywa1cher.coursejournalbackend.service;

import com.a6raywa1cher.coursejournalbackend.dto.TaskDto;
import com.a6raywa1cher.coursejournalbackend.model.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface TaskService {
    TaskDto getById(long id);

    Optional<Task> findRawById(long id);

    List<Task> findRawByCourseId(long courseId);

    void reorder(long courseId, Map<Long, Integer> idToNumber);

    Page<TaskDto> getByCourseId(long courseId, Pageable pageable);

    List<TaskDto> getByCourseId(long courseId);

    TaskDto create(TaskDto dto);

    TaskDto update(long id, TaskDto dto);

    TaskDto patch(long id, TaskDto dto);

    void delete(long id);
}
