package com.a6raywa1cher.coursejournalbackend.model.repo;

import com.a6raywa1cher.coursejournalbackend.model.Faculty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FacultyRepository extends JpaRepository<Faculty, Long> {
    Optional<Faculty> findByName(String name);
}
