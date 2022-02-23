package com.a6raywa1cher.coursejournalbackend.dto.exc;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class NotFoundException extends RuntimeException {
    public NotFoundException(Class<?> clazz, Long id) {
        super("Object of the class %s with id %s is not found".formatted(clazz.getSimpleName(), id));
    }

    public NotFoundException(Class<?> clazz, String query, String value) {
        super("Object of the class %s with %s = %s is not found".formatted(clazz.getSimpleName(), query, value));
    }
}
