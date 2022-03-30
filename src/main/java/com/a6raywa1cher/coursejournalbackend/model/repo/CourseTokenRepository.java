package com.a6raywa1cher.coursejournalbackend.model.repo;

import com.a6raywa1cher.coursejournalbackend.model.Course;
import com.a6raywa1cher.coursejournalbackend.model.CourseToken;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CourseTokenRepository extends CrudRepository<CourseToken, Long> {
    boolean existsByToken(String token);

    Optional<CourseToken> findByCourse(Course course);

    Optional<CourseToken> findByToken(String token);
}
