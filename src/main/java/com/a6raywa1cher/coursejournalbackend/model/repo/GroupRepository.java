package com.a6raywa1cher.coursejournalbackend.model.repo;

import com.a6raywa1cher.coursejournalbackend.model.Faculty;
import com.a6raywa1cher.coursejournalbackend.model.Group;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupRepository extends PagingAndSortingRepository<Group, Long> {
    List<Group> getAllByFaculty(Faculty faculty, Sort sort);

    Optional<Group> findByFacultyAndName(Faculty faculty, String name);
}
