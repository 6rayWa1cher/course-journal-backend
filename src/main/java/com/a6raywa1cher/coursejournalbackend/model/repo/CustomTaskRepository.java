package com.a6raywa1cher.coursejournalbackend.model.repo;

import com.a6raywa1cher.coursejournalbackend.model.Task;

import java.util.Map;

public interface CustomTaskRepository {
    void reorderTasksWithFlush(Map<Task, Integer> remap);
}
