package com.a6raywa1cher.coursejournalbackend.model.repo;

import com.a6raywa1cher.coursejournalbackend.model.Criteria;

import java.util.List;

public interface CustomCriteriaRepository {
    List<Criteria> saveAllForTaskWithRename(List<Criteria> criteriaList);
}
