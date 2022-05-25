package com.a6raywa1cher.coursejournalbackend.dto.exc;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class WrongDatesException extends RuntimeException {
    public WrongDatesException(String date) {
        super(
                "Unable to parse date with value = %s"
                        .formatted(
                                date
                        )
        );
    }

    public WrongDatesException(String fromDate, String toDate) {
        super(
                "FromDate is after toDate: fromDate = %s, toDate = %s"
                        .formatted(
                                fromDate,
                                toDate
                        )
        );
    }
}
