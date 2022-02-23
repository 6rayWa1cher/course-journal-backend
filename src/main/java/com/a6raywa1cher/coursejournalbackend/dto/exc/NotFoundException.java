package com.a6raywa1cher.coursejournalbackend.dto.exc;

public class NotFoundException extends RuntimeException {
    public NotFoundException(Long id, Class<?> clazz) {
        super("Object of the class %s with id %s is not found".formatted(clazz.getSimpleName(), id));
    }
}
