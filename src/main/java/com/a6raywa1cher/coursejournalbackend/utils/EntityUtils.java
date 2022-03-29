package com.a6raywa1cher.coursejournalbackend.utils;

import com.a6raywa1cher.coursejournalbackend.model.IdEntity;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class EntityUtils {
    public static <T, J extends IdEntity<T>> T getAnyNotFound(List<J> entityList, List<T> ids) {
        Set<T> tSet = new HashSet<>(ids);
        return entityList.stream()
                .map(IdEntity::getId)
                .filter(id -> !tSet.contains(id))
                .findAny()
                .orElse(null);
    }
}
