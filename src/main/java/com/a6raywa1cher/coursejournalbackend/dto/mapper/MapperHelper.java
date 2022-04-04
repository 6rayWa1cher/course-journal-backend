package com.a6raywa1cher.coursejournalbackend.dto.mapper;

import com.a6raywa1cher.coursejournalbackend.model.IdEntity;
import org.mapstruct.Named;
import org.mapstruct.TargetType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

@Component
@Named("MapperHelper")
public class MapperHelper {
    private final EntityManager entityManager;
    private final PasswordEncoder passwordEncoder;

    public MapperHelper(EntityManager entityManager, PasswordEncoder passwordEncoder) {
        this.entityManager = entityManager;
        this.passwordEncoder = passwordEncoder;
    }

    @Named("Resolve")
    public <T> T resolve(long id, @TargetType Class<T> tClass) {
        T t = entityManager.find(tClass, id);
        if (t == null) throw new EntityNotFoundException();
        return t;
    }

    @Named("ResolveBoxed")
    public <T> T resolve(Long id, @TargetType Class<T> tClass) {
        if (id == null) return null;
        return resolve((long) id, tClass);
    }

    @Named("FromLocalDateTime")
    public ZonedDateTime fromLocalDateTime(LocalDateTime dateTime) {
        if (dateTime == null) return null;
        return ZonedDateTime.of(dateTime, ZoneId.systemDefault());
    }

    @Named("ToLocalDateTime")
    public LocalDateTime toLocalDateTime(ZonedDateTime dateTime) {
        if (dateTime == null) return null;
        return dateTime.withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();
    }

    @Named("EncodePassword")
    public String encodePassword(String rawPassword) {
        if (rawPassword == null) return null;
        return passwordEncoder.encode(rawPassword);
    }

    @Named("ExtractIds")
    public <T, J extends IdEntity<T>> List<T> extractIds(List<J> entities) {
        if (entities == null) return null;
        return entities.stream().map(IdEntity::getId).toList();
    }
}