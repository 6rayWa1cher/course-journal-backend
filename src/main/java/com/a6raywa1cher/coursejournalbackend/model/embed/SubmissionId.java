package com.a6raywa1cher.coursejournalbackend.model.embed;

import com.a6raywa1cher.coursejournalbackend.model.Student;
import com.a6raywa1cher.coursejournalbackend.model.Task;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Embeddable;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import java.io.Serializable;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubmissionId implements Serializable {
    @ManyToOne(optional = false)
    @JoinColumn(name = "task_id")
    private Task task;

    @ManyToOne(optional = false)
    @JoinColumn(name = "student_id")
    private Student student;
}
