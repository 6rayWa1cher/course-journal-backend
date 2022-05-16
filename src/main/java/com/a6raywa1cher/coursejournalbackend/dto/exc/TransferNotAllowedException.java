package com.a6raywa1cher.coursejournalbackend.dto.exc;

import com.a6raywa1cher.coursejournalbackend.model.IdEntity;
import com.a6raywa1cher.coursejournalbackend.utils.EntityUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class TransferNotAllowedException extends RuntimeException {
    public TransferNotAllowedException(
            Class<?> clazz, String transferredField, String currentField, String newField
    ) {
        super(
                "Couldn't transfer object of %s class with field %s = %s to %s: not allowed"
                        .formatted(
                                clazz.getSimpleName(),
                                transferredField,
                                currentField,
                                newField
                        )
        );
    }

    @Deprecated
    public TransferNotAllowedException(
            Class<?> clazz, String transferredField, Long currentId, Long newId
    ) {
        this(
                clazz,
                transferredField,
                Long.toString(currentId),
                Long.toString(newId)
        );
    }

    public TransferNotAllowedException(
            Class<?> clazz, String transferredField, IdEntity<Long> currentField, IdEntity<Long> newField
    ) {
        this(
                clazz,
                transferredField,
                EntityUtils.getId(currentField),
                EntityUtils.getId(newField)
        );
    }
}
