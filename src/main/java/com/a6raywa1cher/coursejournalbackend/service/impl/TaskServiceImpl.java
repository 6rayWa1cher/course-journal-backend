package com.a6raywa1cher.coursejournalbackend.service.impl;

import com.a6raywa1cher.coursejournalbackend.dto.TaskDto;
import com.a6raywa1cher.coursejournalbackend.dto.exc.ConflictException;
import com.a6raywa1cher.coursejournalbackend.dto.exc.NotFoundException;
import com.a6raywa1cher.coursejournalbackend.dto.exc.TransferNotAllowedException;
import com.a6raywa1cher.coursejournalbackend.dto.exc.VariousParentEntitiesException;
import com.a6raywa1cher.coursejournalbackend.dto.mapper.MapStructMapper;
import com.a6raywa1cher.coursejournalbackend.model.Course;
import com.a6raywa1cher.coursejournalbackend.model.Task;
import com.a6raywa1cher.coursejournalbackend.model.repo.TaskRepository;
import com.a6raywa1cher.coursejournalbackend.service.CourseService;
import com.a6raywa1cher.coursejournalbackend.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class TaskServiceImpl implements TaskService {
    private final MapStructMapper mapper;
    private final TaskRepository repository;
    private final CourseService courseService;

    @Autowired
    public TaskServiceImpl(MapStructMapper mapper, TaskRepository repository, CourseService courseService) {
        this.mapper = mapper;
        this.repository = repository;
        this.courseService = courseService;
    }

    @Override
    public TaskDto getById(long id) {
        return repository.findById(id).map(mapper::map).orElseThrow(() -> new NotFoundException(Task.class, id));
    }

    @Override
    public Optional<Task> findRawById(long id) {
        return repository.findById(id);
    }

    @Override
    public void reorder(long courseId, Map<Long, Integer> idToNumber) {
        List<Task> allRawById = StreamSupport.stream(
                repository.findAllById(idToNumber.keySet()).spliterator(),
                false
        ).toList();

        if (allRawById.size() != idToNumber.size()) {
            Set<Long> requested = idToNumber.keySet();
            Set<Long> presented = allRawById.stream().map(Task::getId).collect(Collectors.toSet());
            throw new NotFoundException(Task.class, "ids", String.join(",", requested.stream()
                    .filter(req -> !presented.contains(req))
                    .map(id -> Long.toString(id))
                    .toList()
            ));
        }

        Map<Long, List<Long>> courses = splitByCourses(allRawById);
        if (courses.size() != 1) {
            throw new VariousParentEntitiesException(courses);
        }
        Course course = allRawById.get(0).getCourse();

        for (Task task : allRawById) {
            task.setTaskNumber(idToNumber.get(task.getId()));
        }

        assertNoConflictsInTaskNumbers(course, allRawById.toArray(new Task[0]));

        repository.saveAll(allRawById);
    }

    @Override
    public Page<TaskDto> getByCourseId(long courseId, Pageable pageable) {
        Course course = getCourseById(courseId);
        return repository.getAllByCourse(course, pageable).map(mapper::map);
    }

    @Override
    public List<TaskDto> getByCourseId(long courseId) {
        Course course = getCourseById(courseId);
        return repository.getAllByCourse(course)
                .stream()
                .map(mapper::map)
                .toList();
    }

    @Override
    public TaskDto create(TaskDto dto) {
        Task task = new Task();
        Course course = getCourseById(dto.getCourse());

        mapper.put(dto, task);
        assertNoConflictsInTaskNumbers(course, task);

        task.setCourse(course);
        task.setCreatedAt(LocalDateTime.now());
        task.setLastModifiedAt(LocalDateTime.now());
        return mapper.map(repository.save(task));
    }

    @Override
    public TaskDto update(long id, TaskDto dto) {
        Task task = getTaskById(id);
        Course course = getCourseById(dto.getCourse());

        mapper.put(dto, task);
        assertNoConflictsInTaskNumbers(course, task);
        assertNoCourseChange(task.getCourse(), course);

        task.setCourse(course);
        task.setLastModifiedAt(LocalDateTime.now());
        return mapper.map(repository.save(task));
    }

    @Override
    public TaskDto patch(long id, TaskDto dto) {
        Task task = getTaskById(id);
        Course course = dto.getCourse() != null ? getCourseById(dto.getCourse()) : task.getCourse();

        mapper.patch(dto, task);
        assertNoConflictsInTaskNumbers(course, task);
        assertNoCourseChange(task.getCourse(), course);

        task.setCourse(course);
        task.setLastModifiedAt(LocalDateTime.now());
        return mapper.map(repository.save(task));
    }

    @Override
    public void delete(long id) {
        Task task = getTaskById(id);
        repository.delete(task);
    }

    private void assertNoConflictsInTaskNumbers(Course course, Task... changed) {
        Map<Long, Integer> inDb = repository.getAllByCourse(course)
                .stream()
                .collect(Collectors.toMap(Task::getId, Task::getTaskNumber));

        for (Task task : changed) {
            inDb.replace(task.getId(), task.getTaskNumber());
        }

        Set<Integer> booked = new HashSet<>();
        for (var item : inDb.entrySet()) {
            int number = item.getValue();
            if (booked.contains(number)) {
                throw new ConflictException(
                        Task.class,
                        "taskNumber", Integer.toString(number),
                        "course", Long.toString(course.getId())
                );
            }
            booked.add(item.getValue());
        }
    }

    private void assertNoCourseChange(Course left, Course right) {
        if (!Objects.equals(left, right)) {
            throw new TransferNotAllowedException(Task.class, "course", left.getId(), right.getId());
        }
    }

    private Map<Long, List<Long>> splitByCourses(List<Task> tasks) {
        Map<Long, List<Long>> out = new HashMap<>();
        for (Task task : tasks) {
            long course = task.getCourse().getId();
            List<Long> list = out.get(course);
            if (list == null) {
                list = out.put(course, new ArrayList<>());
            }
            list.add(task.getId());
        }
        return out;
    }

    private Course getCourseById(long courseId) {
        return courseService.findRawById(courseId).orElseThrow(() -> new NotFoundException(Course.class, courseId));
    }

    private Task getTaskById(long id) {
        return repository.findById(id).orElseThrow(() -> new NotFoundException(Task.class, id));
    }
}
