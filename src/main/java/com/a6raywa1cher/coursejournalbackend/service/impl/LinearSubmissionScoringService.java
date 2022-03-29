package com.a6raywa1cher.coursejournalbackend.service.impl;

import com.a6raywa1cher.coursejournalbackend.model.Submission;
import com.a6raywa1cher.coursejournalbackend.service.SubmissionScoringService;
import org.springframework.stereotype.Component;

@Component
public class LinearSubmissionScoringService implements SubmissionScoringService {
    @Override
    public int getMainScore(Submission submission) {
        return 0;
    }
}
