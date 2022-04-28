package com.a6raywa1cher.coursejournalbackend.dto.exc;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.xml.transform.TransformerFactory;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class TransferNotAllowedException extends RuntimeException {
    public TransferNotAllowedException(
            Class<?> clazz, String transferredField, Long currentId, Long newId
    ) {
        super(
                "Couldn't transfer object of %s class with field %s = %s to %s: not allowed"
                        .formatted(
                                clazz.getSimpleName(),
                                transferredField,
                                Long.toString(currentId),
                                Long.toString(newId)
                        )
        );
    }

    public TransferNotAllowedException(
            Class<?> clazz, String transferredField, String currentField, String newField
    ) {
        super(
                "Couldn't transfer object of %s class with field%s = %s to %s: not allowed"
                        .formatted(
                                clazz.getSimpleName(),
                                transferredField,
                                currentField,
                                newField
                        )
        );
    }
}
