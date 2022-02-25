package com.a6raywa1cher.coursejournalbackend.dto.exc;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class ConflictException extends RuntimeException {
    public ConflictException(Class<?> clazz, String query, String value) {
        super("Object of the class %s with %s = %s exists".formatted(clazz.getSimpleName(), query, value));
    }

    public ConflictException(Class<?> clazz, String... values) {
        super("Object of the class %s with %s exists".formatted(
                clazz.getSimpleName(), zip(values)
        ));
    }

    private static String zip(String... values) {
        if (values.length == 0) throw new IllegalArgumentException("Values array is empty");
        if (values.length % 2 != 0)
            throw new IllegalArgumentException("Values aren't paired: " + String.join(",", values));
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < values.length; i += 2) {
            String key = values[i];
            String value = values[i + 1];
            if (i != 0) sb.append(", ");
            sb.append(key).append(" = ").append(value);
        }
        return sb.toString();
    }
}
