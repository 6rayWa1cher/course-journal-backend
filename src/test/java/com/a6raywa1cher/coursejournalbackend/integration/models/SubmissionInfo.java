package com.a6raywa1cher.coursejournalbackend.integration.models;

import java.time.ZonedDateTime;
import java.util.List;

public record SubmissionInfo(long task, List<Long> satisfiedCriteria, ZonedDateTime submittedAt,
                             double additionalScore) {
}
