package com.a6raywa1cher.coursejournalbackend.service;

import com.a6raywa1cher.coursejournalbackend.dto.CriteriaDto;
import com.a6raywa1cher.coursejournalbackend.dto.SubmissionDto;
import com.a6raywa1cher.coursejournalbackend.dto.TaskDto;

import java.util.List;

public interface SubmissionScoringService {
    double getMainScore(SubmissionDto submission, TaskDto task, List<CriteriaDto> allCriteria);
}
