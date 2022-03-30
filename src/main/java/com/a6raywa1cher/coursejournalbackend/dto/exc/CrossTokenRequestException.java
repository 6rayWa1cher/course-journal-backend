package com.a6raywa1cher.coursejournalbackend.dto.exc;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class CrossTokenRequestException extends RuntimeException {
    public CrossTokenRequestException(Throwable cause) {
        super(cause);
    }
}
