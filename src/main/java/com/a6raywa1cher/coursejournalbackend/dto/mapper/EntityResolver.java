package com.a6raywa1cher.coursejournalbackend.dto.mapper;

import org.mapstruct.TargetType;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;

@Component
public class EntityResolver {
    private final EntityManager entityManager;

    public EntityResolver(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public <T> T resolve(long id, @TargetType Class<T> tClass) {
        T t = entityManager.find(tClass, id);
        if (t == null) throw new EntityNotFoundException();
        return t;
    }

    public <T> T resolve(Long id, @TargetType Class<T> tClass) {
        if (id == null) return null;
        return resolve((long) id, tClass);
    }
}