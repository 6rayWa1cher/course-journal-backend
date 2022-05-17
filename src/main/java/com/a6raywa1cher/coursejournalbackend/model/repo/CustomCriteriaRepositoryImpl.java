package com.a6raywa1cher.coursejournalbackend.model.repo;

import com.a6raywa1cher.coursejournalbackend.model.Criteria;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class CustomCriteriaRepositoryImpl implements CustomCriteriaRepository {
    private final EntityManager em;

    public CustomCriteriaRepositoryImpl(EntityManager em) {
        this.em = em;
    }

    @Override
    public List<Criteria> saveAllForTaskWithRename(List<Criteria> criteriaList) {
        Map<Criteria, String> names = new HashMap<>();
        for (int i = 0; i < criteriaList.size(); i++) {
            Criteria criteria = criteriaList.get(i);
            names.put(criteria, criteria.getName());
            criteria.setName("tmp" + i);
        }
        criteriaList.forEach(em::merge);
        em.flush();
        return criteriaList
                .stream()
                .peek(c -> c.setName(names.get(c)))
                .map(em::merge)
                .toList();
    }
}
