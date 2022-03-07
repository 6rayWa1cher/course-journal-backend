package com.a6raywa1cher.coursejournalbackend.model.repo;

import com.a6raywa1cher.coursejournalbackend.model.Criteria;
import com.a6raywa1cher.coursejournalbackend.model.Task;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CriteriaRepository extends CrudRepository<Criteria, Long> {
    List<Criteria> getAllByTask(Task task);
}