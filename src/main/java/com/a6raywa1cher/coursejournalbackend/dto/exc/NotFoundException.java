package com.a6raywa1cher.coursejournalbackend.dto.exc;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class NotFoundException extends RuntimeException {
    public NotFoundException(Long id, Class<?> clazz) {
        super("Object of the class %s with id %s is not found".formatted(clazz.getSimpleName(), id));
    }

    public NotFoundException(String query, String value, Class<?> clazz) {
        super("Object of the class %s with %s = %s is not found".formatted(clazz.getSimpleName(), query, value));
    }
}
