package com.a6raywa1cher.coursejournalbackend.service.impl;

import com.a6raywa1cher.coursejournalbackend.component.SecureRandomStringGenerator;
import com.a6raywa1cher.coursejournalbackend.dto.CourseTokenDto;
import com.a6raywa1cher.coursejournalbackend.dto.exc.ConflictException;
import com.a6raywa1cher.coursejournalbackend.dto.exc.NotFoundException;
import com.a6raywa1cher.coursejournalbackend.dto.mapper.MapStructMapper;
import com.a6raywa1cher.coursejournalbackend.model.Course;
import com.a6raywa1cher.coursejournalbackend.model.CourseToken;
import com.a6raywa1cher.coursejournalbackend.model.repo.CourseTokenRepository;
import com.a6raywa1cher.coursejournalbackend.service.CourseService;
import com.a6raywa1cher.coursejournalbackend.service.CourseTokenService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class CourseTokenServiceImpl implements CourseTokenService {
    private final CourseTokenRepository repository;
    private final MapStructMapper mapper;
    private final CourseService courseService;
    private final SecureRandomStringGenerator generator;

    public CourseTokenServiceImpl(CourseTokenRepository repository, MapStructMapper mapper, CourseService courseService, SecureRandomStringGenerator generator) {
        this.repository = repository;
        this.mapper = mapper;
        this.courseService = courseService;
        this.generator = generator;
    }

    @Override
    public CourseTokenDto getById(long id) {
        return mapper.map(getCourseTokenById(id));
    }

    @Override
    public Optional<CourseToken> findRawById(long id) {
        return repository.findById(id);
    }

    @Override
    public Optional<CourseTokenDto> findByToken(String token) {
        return repository.findByToken(token).map(mapper::map);
    }

    @Override
    public CourseTokenDto getByCourseId(long courseId) {
        Course courseById = getCourseById(courseId);
        return repository.findByCourse(courseById)
                .map(mapper::map)
                .orElseThrow(() -> new NotFoundException(CourseToken.class, "course", Long.toString(courseId)));
    }

    @Override
    public CourseTokenDto create(CourseTokenDto dto) {
        CourseToken courseToken = new CourseToken();
        Course course = getCourseById(dto.getCourse());

        assertUniqueTokenForCourse(course);

        courseToken.setCourse(course);
        courseToken.setToken(generateUniqueRandomToken());
        courseToken.setCreatedAt(LocalDateTime.now());
        courseToken.setLastModifiedAt(LocalDateTime.now());
        return mapper.map(repository.save(courseToken));
    }

    @Override
    public void delete(long id) {
        CourseToken courseToken = getCourseTokenById(id);
        repository.delete(courseToken);
    }

    private Course getCourseById(long courseId) {
        return courseService.findRawById(courseId).orElseThrow(() -> new NotFoundException(Course.class, courseId));
    }

    private CourseToken getCourseTokenById(long courseTokenId) {
        return repository.findById(courseTokenId).orElseThrow(() -> new NotFoundException(CourseToken.class, courseTokenId));
    }

    private String generateUniqueRandomToken() {
        String out;
        do {
            out = generator.generate();
        } while (repository.existsByToken(out));
        return out;
    }

    private void assertUniqueTokenForCourse(Course course) {
        if (repository.findByCourse(course).isPresent()) {
            throw new ConflictException(CourseToken.class, "course", Long.toString(course.getId()));
        }
    }
}
