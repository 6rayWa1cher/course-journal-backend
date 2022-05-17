package com.a6raywa1cher.coursejournalbackend.integration.models;

import java.util.List;

public record GetSetForTaskData(long submissionId, long taskId, List<Long> criteria) {
}
