package com.a6raywa1cher.coursejournalbackend.service;

import com.a6raywa1cher.coursejournalbackend.model.Submission;

public interface SubmissionScoringService {
    int getMainScore(Submission submission);
}
