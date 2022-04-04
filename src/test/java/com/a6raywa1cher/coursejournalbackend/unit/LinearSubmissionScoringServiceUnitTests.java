package com.a6raywa1cher.coursejournalbackend.unit;

import com.a6raywa1cher.coursejournalbackend.dto.CriteriaDto;
import com.a6raywa1cher.coursejournalbackend.dto.SubmissionDto;
import com.a6raywa1cher.coursejournalbackend.dto.TaskDto;
import com.a6raywa1cher.coursejournalbackend.service.impl.LinearSubmissionScoringService;
import org.assertj.core.data.Offset;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class LinearSubmissionScoringServiceUnitTests {
    @Test
    void calculateDeadlineFactor__submissionBeforeSoft() {
        var scoringService = new LinearSubmissionScoringService();
        ZonedDateTime now = ZonedDateTime.now();
        SubmissionDto submissionDto = SubmissionDto.builder()
                .submittedAt(now)
                .build();
        TaskDto taskDto = TaskDto.builder()
                .maxPenaltyPercent(50)
                .softDeadlineAt(now.plusMinutes(3))
                .hardDeadlineAt(now.plusMinutes(63))
                .build();

        double actual = scoringService.calculateDeadlineFactor(submissionDto, taskDto);
        assertThat(actual).isCloseTo(1d, Offset.offset(0.001d));
    }

    @Test
    void calculateDeadlineFactor__submissionAtSoft() {
        var scoringService = new LinearSubmissionScoringService();
        ZonedDateTime now = ZonedDateTime.now();
        SubmissionDto submissionDto = SubmissionDto.builder()
                .submittedAt(now)
                .build();
        TaskDto taskDto = TaskDto.builder()
                .maxPenaltyPercent(50)
                .softDeadlineAt(now)
                .hardDeadlineAt(now.plusMinutes(60))
                .build();

        double actual = scoringService.calculateDeadlineFactor(submissionDto, taskDto);
        assertThat(actual).isCloseTo(1d, Offset.offset(0.001d));
    }

    @Test
    void calculateDeadlineFactor__submissionInTheMiddleOfDeadlines() {
        var scoringService = new LinearSubmissionScoringService();
        ZonedDateTime now = ZonedDateTime.now();
        SubmissionDto submissionDto = SubmissionDto.builder()
                .submittedAt(now.plusMinutes(30))
                .build();
        TaskDto taskDto = TaskDto.builder()
                .maxPenaltyPercent(50)
                .softDeadlineAt(now)
                .hardDeadlineAt(now.plusMinutes(60))
                .build();

        double actual = scoringService.calculateDeadlineFactor(submissionDto, taskDto);
        assertThat(actual).isCloseTo(0.75d, Offset.offset(0.001d));
    }

    @Test
    void calculateDeadlineFactor__submissionAtHard() {
        var scoringService = new LinearSubmissionScoringService();
        ZonedDateTime now = ZonedDateTime.now();
        SubmissionDto submissionDto = SubmissionDto.builder()
                .submittedAt(now.plusMinutes(60))
                .build();
        TaskDto taskDto = TaskDto.builder()
                .maxPenaltyPercent(50)
                .softDeadlineAt(now)
                .hardDeadlineAt(now.plusMinutes(60))
                .build();

        double actual = scoringService.calculateDeadlineFactor(submissionDto, taskDto);
        assertThat(actual).isCloseTo(0.5d, Offset.offset(0.001d));
    }

    @Test
    void calculateDeadlineFactor__submissionAfterHard() {
        var scoringService = new LinearSubmissionScoringService();
        ZonedDateTime now = ZonedDateTime.now();
        SubmissionDto submissionDto = SubmissionDto.builder()
                .submittedAt(now.plusMinutes(120))
                .build();
        TaskDto taskDto = TaskDto.builder()
                .maxPenaltyPercent(50)
                .softDeadlineAt(now)
                .hardDeadlineAt(now.plusMinutes(60))
                .build();

        double actual = scoringService.calculateDeadlineFactor(submissionDto, taskDto);
        assertThat(actual).isCloseTo(0.5d, Offset.offset(0.001d));
    }

    @Test
    void calculateDeadlineFactor__submissionBeforePoint() {
        var scoringService = new LinearSubmissionScoringService();
        ZonedDateTime now = ZonedDateTime.now();
        SubmissionDto submissionDto = SubmissionDto.builder()
                .submittedAt(now)
                .build();
        TaskDto taskDto = TaskDto.builder()
                .maxPenaltyPercent(50)
                .softDeadlineAt(now.plusMinutes(42))
                .hardDeadlineAt(now.plusMinutes(42))
                .build();

        double actual = scoringService.calculateDeadlineFactor(submissionDto, taskDto);
        assertThat(actual).isCloseTo(1d, Offset.offset(0.001d));
    }

    @Test
    void calculateDeadlineFactor__submissionAtPoint() {
        var scoringService = new LinearSubmissionScoringService();
        ZonedDateTime now = ZonedDateTime.now();
        SubmissionDto submissionDto = SubmissionDto.builder()
                .submittedAt(now)
                .build();
        TaskDto taskDto = TaskDto.builder()
                .maxPenaltyPercent(50)
                .softDeadlineAt(now)
                .hardDeadlineAt(now)
                .build();

        double actual = scoringService.calculateDeadlineFactor(submissionDto, taskDto);
        assertThat(actual).isCloseTo(1d, Offset.offset(0.001d));
    }

    @Test
    void calculateDeadlineFactor__submissionAfterPoint() {
        var scoringService = new LinearSubmissionScoringService();
        ZonedDateTime now = ZonedDateTime.now();
        SubmissionDto submissionDto = SubmissionDto.builder()
                .submittedAt(now.plusMinutes(42))
                .build();
        TaskDto taskDto = TaskDto.builder()
                .maxPenaltyPercent(50)
                .softDeadlineAt(now)
                .hardDeadlineAt(now)
                .build();

        double actual = scoringService.calculateDeadlineFactor(submissionDto, taskDto);
        assertThat(actual).isCloseTo(0.5d, Offset.offset(0.001d));
    }

    @Test
    void calculateCriteriaFactor__noneSatisfied() {
        var scoringService = new LinearSubmissionScoringService();
        CriteriaDto criteria1 = CriteriaDto.builder()
                .id(1L)
                .criteriaPercent(20)
                .build();
        CriteriaDto criteria2 = CriteriaDto.builder()
                .id(2L)
                .criteriaPercent(30)
                .build();
        List<CriteriaDto> allCriteria = List.of(criteria1, criteria2);
        SubmissionDto submissionDto = SubmissionDto.builder()
                .satisfiedCriteria(Collections.emptyList())
                .build();

        double actual = scoringService.calculateCriteriaFactor(submissionDto, allCriteria);
        assertThat(actual).isCloseTo(0d, Offset.offset(0.001d));
    }

    @Test
    void calculateCriteriaFactor__partiallySatisfied() {
        var scoringService = new LinearSubmissionScoringService();
        CriteriaDto criteria1 = CriteriaDto.builder()
                .id(1L)
                .criteriaPercent(20)
                .build();
        CriteriaDto criteria2 = CriteriaDto.builder()
                .id(2L)
                .criteriaPercent(30)
                .build();
        List<CriteriaDto> allCriteria = List.of(criteria1, criteria2);
        SubmissionDto submissionDto = SubmissionDto.builder()
                .satisfiedCriteria(List.of(criteria1.getId()))
                .build();

        double actual = scoringService.calculateCriteriaFactor(submissionDto, allCriteria);
        assertThat(actual).isCloseTo(0.4d, Offset.offset(0.001d));
    }

    @Test
    void calculateCriteriaFactor__fullySatisfied() {
        var scoringService = new LinearSubmissionScoringService();
        CriteriaDto criteria1 = CriteriaDto.builder()
                .id(1L)
                .criteriaPercent(20)
                .build();
        CriteriaDto criteria2 = CriteriaDto.builder()
                .id(2L)
                .criteriaPercent(30)
                .build();
        List<CriteriaDto> allCriteria = List.of(criteria1, criteria2);
        SubmissionDto submissionDto = SubmissionDto.builder()
                .satisfiedCriteria(List.of(criteria1.getId(), criteria2.getId()))
                .build();

        double actual = scoringService.calculateCriteriaFactor(submissionDto, allCriteria);
        assertThat(actual).isCloseTo(1d, Offset.offset(0.001d));
    }

    @Test
    void calculateCriteriaFactor__noCriteria() {
        var scoringService = new LinearSubmissionScoringService();
        List<CriteriaDto> allCriteria = Collections.emptyList();
        SubmissionDto submissionDto = SubmissionDto.builder()
                .satisfiedCriteria(Collections.emptyList())
                .build();

        double actual = scoringService.calculateCriteriaFactor(submissionDto, allCriteria);
        assertThat(actual).isCloseTo(1d, Offset.offset(0.001d));
    }

    @Test
    void getMainScore__fullScore() {
        var scoringService = new LinearSubmissionScoringService();
        ZonedDateTime now = ZonedDateTime.now();
        CriteriaDto criteria1 = CriteriaDto.builder()
                .id(1L)
                .criteriaPercent(20)
                .build();
        CriteriaDto criteria2 = CriteriaDto.builder()
                .id(2L)
                .criteriaPercent(30)
                .build();
        List<CriteriaDto> allCriteria = List.of(criteria1, criteria2);
        SubmissionDto submissionDto = SubmissionDto.builder()
                .satisfiedCriteria(List.of(criteria1.getId(), criteria2.getId()))
                .submittedAt(now)
                .build();
        TaskDto taskDto = TaskDto.builder()
                .maxScore(15)
                .maxPenaltyPercent(50)
                .softDeadlineAt(now.plusMinutes(30))
                .hardDeadlineAt(now.plusMinutes(60))
                .deadlinesEnabled(true)
                .build();

        double actual = scoringService.getMainScore(submissionDto, taskDto, allCriteria);
        assertThat(actual).isCloseTo(15d, Offset.offset(0.001d));
    }

    @Test
    void getMainScore__zeroScore() {
        var scoringService = new LinearSubmissionScoringService();
        ZonedDateTime now = ZonedDateTime.now();
        CriteriaDto criteria1 = CriteriaDto.builder()
                .id(1L)
                .criteriaPercent(20)
                .build();
        CriteriaDto criteria2 = CriteriaDto.builder()
                .id(2L)
                .criteriaPercent(30)
                .build();
        List<CriteriaDto> allCriteria = List.of(criteria1, criteria2);
        SubmissionDto submissionDto = SubmissionDto.builder()
                .satisfiedCriteria(Collections.emptyList())
                .submittedAt(now)
                .build();
        TaskDto taskDto = TaskDto.builder()
                .maxScore(15)
                .maxPenaltyPercent(50)
                .softDeadlineAt(now.plusMinutes(30))
                .hardDeadlineAt(now.plusMinutes(60))
                .deadlinesEnabled(true)
                .build();

        double actual = scoringService.getMainScore(submissionDto, taskDto, allCriteria);
        assertThat(actual).isCloseTo(0d, Offset.offset(0.001d));
    }

    @Test
    void getMainScore__lateScore() {
        var scoringService = new LinearSubmissionScoringService();
        ZonedDateTime now = ZonedDateTime.now();
        CriteriaDto criteria1 = CriteriaDto.builder()
                .id(1L)
                .criteriaPercent(20)
                .build();
        CriteriaDto criteria2 = CriteriaDto.builder()
                .id(2L)
                .criteriaPercent(30)
                .build();
        List<CriteriaDto> allCriteria = List.of(criteria1, criteria2);
        SubmissionDto submissionDto = SubmissionDto.builder()
                .satisfiedCriteria(List.of(criteria1.getId(), criteria2.getId()))
                .submittedAt(now.plusMinutes(120))
                .build();
        TaskDto taskDto = TaskDto.builder()
                .maxScore(15)
                .maxPenaltyPercent(50)
                .softDeadlineAt(now.plusMinutes(30))
                .hardDeadlineAt(now.plusMinutes(60))
                .deadlinesEnabled(true)
                .build();

        double actual = scoringService.getMainScore(submissionDto, taskDto, allCriteria);
        assertThat(actual).isCloseTo(7.5d, Offset.offset(0.001d));
    }

    @Test
    void getMainScore__averageScore() {
        var scoringService = new LinearSubmissionScoringService();
        ZonedDateTime now = ZonedDateTime.now();
        CriteriaDto criteria1 = CriteriaDto.builder()
                .id(1L)
                .criteriaPercent(20)
                .build();
        CriteriaDto criteria2 = CriteriaDto.builder()
                .id(2L)
                .criteriaPercent(30)
                .build();
        List<CriteriaDto> allCriteria = List.of(criteria1, criteria2);
        SubmissionDto submissionDto = SubmissionDto.builder()
                .satisfiedCriteria(List.of(criteria1.getId()))
                .submittedAt(now.plusMinutes(45))
                .build();
        TaskDto taskDto = TaskDto.builder()
                .maxScore(15)
                .maxPenaltyPercent(50)
                .softDeadlineAt(now.plusMinutes(30))
                .hardDeadlineAt(now.plusMinutes(60))
                .deadlinesEnabled(true)
                .build();

        double actual = scoringService.getMainScore(submissionDto, taskDto, allCriteria);
        assertThat(actual).isCloseTo(4.5d, Offset.offset(0.001d));
    }
}
