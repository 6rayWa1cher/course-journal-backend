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
import com.a6raywa1cher.coursejournalbackend.service.SubmissionService;
import com.a6raywa1cher.coursejournalbackend.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

@Service
@Transactional
public class CriteriaServiceImpl implements CriteriaService {
    private final CriteriaRepository repository;
    private final MapStructMapper mapper;
    private final TaskService taskService;
    private SubmissionService submissionService;

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
    public List<Criteria> findRawById(List<Long> ids) {
        return repository.findAllById(ids).stream().toList();
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

        Criteria saved = repository.save(criteria);
        submissionService.recalculateMainScoreForTask(task.getId());

        return mapper.map(saved);
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

        Criteria saved = repository.save(criteria);
        submissionService.recalculateMainScoreForTask(task.getId());

        return mapper.map(saved);
    }

    @Override
    public List<CriteriaDto> setForTask(long taskId, List<CriteriaDto> criteriaDtoList) {
        Task task = getTaskById(taskId);
        List<Criteria> existingCriteria = repository.getAllByTask(task, Sort.by(Sort.Order.asc("id")));
        int inputSize = criteriaDtoList.size();
        int dbSize = existingCriteria.size();
        List<Criteria> toCreate = new ArrayList<>();
        List<Criteria> toSave = new ArrayList<>();
        List<Criteria> toDelete = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        for (int i = 0; i < Math.max(inputSize, dbSize); i++) {
            if (i < inputSize && i < dbSize) {
                CriteriaDto input = criteriaDtoList.get(i);
                Criteria db = existingCriteria.get(i);
                mapper.put(input, db);
                db.setLastModifiedAt(now);
                toSave.add(db);
            } else if (i < inputSize) {
                CriteriaDto input = criteriaDtoList.get(i);
                Criteria criteria = new Criteria();
                mapper.put(input, criteria);
                criteria.setTask(task);
                criteria.setCreatedAt(now);
                criteria.setLastModifiedAt(now);
                toCreate.add(criteria);
            } else {
                Criteria db = existingCriteria.get(i);
                db.getSubmissionList().forEach(s -> s.getSatisfiedCriteria().remove(db));
                db.getSubmissionList().clear();
                toDelete.add(db);
            }
        }
        repository.deleteAll(toDelete);
        return Stream.concat(
                        repository.saveAllForTaskWithRename(toSave).stream(),
                        repository.saveAll(toCreate).stream()
                )
                .map(mapper::map)
                .toList();
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

        Criteria saved = repository.save(criteria);
        submissionService.recalculateMainScoreForTask(task.getId());

        return mapper.map(saved);
    }

    @Override
    public void delete(long id) {
        Criteria criteria = getCriteriaById(id);
        criteria.getSubmissionList().forEach(s -> s.getSatisfiedCriteria().remove(criteria));
        criteria.getSubmissionList().clear();
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
            throw new TransferNotAllowedException(Criteria.class, "task", criteria.getTask(), newTask);
        }
    }

    @Autowired
    @Lazy
    public void setSubmissionService(SubmissionService submissionService) {
        this.submissionService = submissionService;
    }
}
