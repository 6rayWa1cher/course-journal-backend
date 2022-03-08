package com.a6raywa1cher.coursejournalbackend.model.repo;

import com.a6raywa1cher.coursejournalbackend.model.Task;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.Map;

@Repository
public class CustomTaskRepositoryImpl implements CustomTaskRepository {
    private final EntityManager em;

    public CustomTaskRepositoryImpl(EntityManager em) {
        this.em = em;
    }

    @Override
    @Transactional
    public void reorderTasksWithFlush(Map<Task, Integer> remap) {
        for (Task task : remap.keySet()) {
            task.setTaskNumber(null);
            em.merge(task);
        }
        em.flush();
        for (var item : remap.entrySet()) {
            Task task = item.getKey();
            Integer number = item.getValue();
            task.setTaskNumber(number);
            em.merge(task);
        }
        em.flush();
    }
}
