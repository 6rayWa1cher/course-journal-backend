package com.a6raywa1cher.coursejournalbackend.dto.exc;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class NoDataPresentedException extends RuntimeException {
    public NoDataPresentedException(Class<?> clazz, String... attributes) {
        super("No data presented for properties %s of the class %s"
                .formatted(String.join(",", attributes), clazz.getSimpleName()));
    }
}
