package com.a6raywa1cher.coursejournalbackend.service.impl;

import com.a6raywa1cher.coursejournalbackend.dto.CriteriaDto;
import com.a6raywa1cher.coursejournalbackend.dto.exc.ConflictException;
import com.a6raywa1cher.coursejournalbackend.dto.exc.NotFoundException;
import com.a6raywa1cher.coursejournalbackend.dto.exc.TransferNotAllowedException;
import com.a6raywa1cher.coursejournalbackend.dto.mapper.MapStructMapper;
import com.a6raywa1cher.coursejournalbackend.model.Criteria;
import com.a6raywa1cher.coursejournalbackend.model.Task;
import com.a6raywa1cher.coursejournalbackend.model.repo.CriteriaRepository;
import com.a6raywa1cher.coursejournalbackend.service.CriteriaService;
import com.a6raywa1cher.coursejournalbackend.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@Transactional
public class CriteriaServiceImpl implements CriteriaService {
    private final CriteriaRepository repository;
    private final MapStructMapper mapper;
    private final TaskService taskService;

    @Autowired
    public CriteriaServiceImpl(CriteriaRepository repository, MapStructMapper mapper, TaskService taskService) {
        this.repository = repository;
        this.mapper = mapper;
        this.taskService = taskService;
    }

    @Override
    public CriteriaDto getById(long id) {
        return mapper.map(getCriteriaById(id));
    }

    @Override
    public Optional<Criteria> findRawById(long id) {
        return repository.findById(id);
    }

    @Override
    public List<CriteriaDto> getByTaskId(long taskId) {
        return repository.getAllByTask(getTaskById(taskId)).stream().map(mapper::map).toList();
    }

    @Override
    public CriteriaDto create(CriteriaDto dto) {
        Criteria criteria = new Criteria();
        Task task = getTaskById(dto.getTask());

        assertUniqueForTaskCriteriaName(task, dto.getName());
        mapper.put(dto, criteria);

        criteria.setTask(task);
        criteria.setCreatedAt(LocalDateTime.now());
        criteria.setLastModifiedAt(LocalDateTime.now());
        return mapper.map(repository.save(criteria));
    }

    @Override
    public CriteriaDto update(long id, CriteriaDto dto) {
        Criteria criteria = getCriteriaById(id);
        Task task = getTaskById(dto.getTask());

        assertUniqueForTaskCriteriaName(task, criteria.getId(), dto.getName());
        assertNoTaskChange(criteria, task);
        mapper.put(dto, criteria);

        criteria.setTask(task);
        criteria.setLastModifiedAt(LocalDateTime.now());
        return mapper.map(repository.save(criteria));
    }

    @Override
    public CriteriaDto patch(long id, CriteriaDto dto) {
        Criteria criteria = getCriteriaById(id);
        Task task = dto.getTask() != null ? getTaskById(dto.getTask()) : criteria.getTask();

        if (dto.getName() != null) assertUniqueForTaskCriteriaName(task, criteria.getId(), dto.getName());
        assertNoTaskChange(criteria, task);
        mapper.patch(dto, criteria);

        criteria.setTask(task);
        criteria.setLastModifiedAt(LocalDateTime.now());
        return mapper.map(repository.save(criteria));
    }

    @Override
    public void delete(long id) {
        Criteria criteria = getCriteriaById(id);
        repository.delete(criteria);
    }

    private Criteria getCriteriaById(long id) {
        return repository.findById(id).orElseThrow(() -> new NotFoundException(Criteria.class, id));
    }

    private void assertUniqueForTaskCriteriaName(Task task, String name) {
        List<Criteria> allByTask = repository.getAllByTask(task);
        boolean match = allByTask.stream()
                .anyMatch(c -> name.equals(c.getName()));
        if (match) {
            throw new ConflictException(Criteria.class, "taskId", Long.toString(task.getId()), "name", name);
        }
    }

    private void assertUniqueForTaskCriteriaName(Task task, long id, String name) {
        List<Criteria> allByTask = repository.getAllByTask(task);
        boolean match = allByTask.stream()
                .anyMatch(c -> name.equals(c.getName()) && c.getId() != id);
        if (match) {
            throw new ConflictException(Criteria.class, "taskId", Long.toString(task.getId()), "name", name);
        }
    }

    private Task getTaskById(long id) {
        return taskService.findRawById(id).orElseThrow(() -> new NotFoundException(Task.class, id));
    }

    private void assertNoTaskChange(Criteria criteria, Task newTask) {
        if (!Objects.equals(criteria.getTask(), newTask)) {
            throw new TransferNotAllowedException(Criteria.class, "task", criteria.getTask().getId(), newTask.getId());
        }
    }
}
