package com.a6raywa1cher.coursejournalbackend.service.impl;

import com.a6raywa1cher.coursejournalbackend.dto.CriteriaDto;
import com.a6raywa1cher.coursejournalbackend.dto.SubmissionDto;
import com.a6raywa1cher.coursejournalbackend.dto.TaskDto;
import com.a6raywa1cher.coursejournalbackend.service.SubmissionScoringService;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.a6raywa1cher.coursejournalbackend.utils.CommonUtils.coalesce;

@Component
public class LinearSubmissionScoringService implements SubmissionScoringService {
    private final static int SCALE = 2;

    public double calculateDeadlineFactor(SubmissionDto submission, TaskDto task) {
        ZonedDateTime hardDeadlineAt = task.getHardDeadlineAt();
        ZonedDateTime softDeadlineAt = task.getSoftDeadlineAt();
        ZonedDateTime submittedAt = submission.getSubmittedAt();
        ZonedDateTime zero = getOldest(hardDeadlineAt, softDeadlineAt, submittedAt).minusDays(1);
        long hardDeadline = getMinutes(hardDeadlineAt, zero);
        long softDeadline = getMinutes(softDeadlineAt, zero);
        long submitted = getMinutes(submittedAt, zero);
        double mpd = coalesce(task.getMaxPenaltyPercent(), 0) / 100d;
        if (hardDeadline == softDeadline) {
            return submitted <= softDeadline ? 1 : mpd;
        } else {
            return Math.min(1d, Math.max(
                    1d - mpd,
                    1d - mpd * (submitted - softDeadline) / ((double) (hardDeadline - softDeadline))
            ));
        }
    }

    public double calculateCriteriaFactor(SubmissionDto submission, List<CriteriaDto> allCriteria) {
        Set<Long> satisfiedCriteriaIds = new HashSet<>(submission.getSatisfiedCriteria());
        int satisfiedScore = 0, allScore = 0;
        for (CriteriaDto criteria : allCriteria) {
            int criteriaPercent = coalesce(criteria.getCriteriaPercent(), 0);
            allScore += criteriaPercent;
            if (satisfiedCriteriaIds.contains(criteria.getId())) {
                satisfiedScore += criteriaPercent;
            }
        }
        return allScore == 0 ? 1 : satisfiedScore / ((double) allScore);
    }

    @Override
    public double getMainScore(SubmissionDto submission, TaskDto task, List<CriteriaDto> allCriteria) {
        if (task.getMaxScore() == null || task.getMaxScore() == 0) return 0d;
        double p = task.getDeadlinesEnabled() != null && task.getDeadlinesEnabled() ?
                calculateDeadlineFactor(submission, task) : 1d;
        double c = calculateCriteriaFactor(submission, allCriteria);

        double scaleFactor = Math.pow(10, SCALE);
        return Math.round(p * c * task.getMaxScore() * scaleFactor) / scaleFactor;
    }

    private long getMinutes(ZonedDateTime date, ZonedDateTime zeroTime) {
        return zeroTime.until(date, ChronoUnit.MINUTES);
    }

    private ZonedDateTime getOldest(ZonedDateTime... times) {
        ZonedDateTime out = ZonedDateTime.now();
        for (ZonedDateTime time : times) {
            out = time.isBefore(out) ? time : out;
        }
        return out;
    }
}
