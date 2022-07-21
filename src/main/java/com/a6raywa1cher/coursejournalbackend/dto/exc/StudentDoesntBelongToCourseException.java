package com.a6raywa1cher.coursejournalbackend.dto.exc;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class StudentDoesntBelongToCourseException extends RuntimeException {
    public StudentDoesntBelongToCourseException(Long studentId, Long courseId) {
        super("The student with id %d doesn't belong to course with id %d".formatted(studentId, courseId));
    }
}
