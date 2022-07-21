package com.a6raywa1cher.coursejournalbackend.integration.models;

import java.time.ZonedDateTime;
import java.util.List;

public record ExtendedSubmissionInfo(long task, List<Long> satisfiedCriteria, long student, ZonedDateTime submittedAt,
                                     double additionalScore) {
}
