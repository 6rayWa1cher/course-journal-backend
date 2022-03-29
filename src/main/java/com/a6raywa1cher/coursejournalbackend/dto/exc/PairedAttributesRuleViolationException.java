package com.a6raywa1cher.coursejournalbackend.dto.exc;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class PairedAttributesRuleViolationException extends RuntimeException {
    public PairedAttributesRuleViolationException(
            Class<?> clazz, String attribute1, Object value1, String attribute2, Object value2
    ) {
        super(
                "Paired attributes of class %s has different states: %s is %s, and %s is %s"
                        .formatted(
                                clazz.getSimpleName(),
                                attribute1,
                                value1,
                                attribute2,
                                value2
                        )
        );
    }
}
