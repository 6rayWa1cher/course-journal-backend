package com.a6raywa1cher.coursejournalbackend.dto.exc;


import com.a6raywa1cher.coursejournalbackend.model.IdEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class MultipleTargetsException extends RuntimeException {
    public MultipleTargetsException(IdEntity<Long>... entities) {
        super("Multiple targets found: %s".formatted(String.join(",", getClassNames(entities))));
    }

    public static List<String> getClassNames(IdEntity<Long>[] entities) {
        return Arrays.stream(entities)
                .filter(Objects::nonNull)
                .map(e -> e.getClass().getSimpleName())
                .toList();
    }
}
