package com.a6raywa1cher.coursejournalbackend.service;

import com.a6raywa1cher.coursejournalbackend.dto.CriteriaDto;
import com.a6raywa1cher.coursejournalbackend.model.Criteria;

import java.util.List;
import java.util.Optional;

public interface CriteriaService {
    CriteriaDto getById(long id);

    Optional<Criteria> findRawById(long id);

    List<CriteriaDto> getByTaskId(long taskId);

    CriteriaDto create(CriteriaDto dto);

    CriteriaDto update(long id, CriteriaDto dto);

    CriteriaDto patch(long id, CriteriaDto dto);

    void delete(long id);
}
