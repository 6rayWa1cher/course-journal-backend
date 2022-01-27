package com.a6raywa1cher.coursejournalbackend.component.datarest;

import com.a6raywa1cher.coursejournalbackend.model.Student;
import com.a6raywa1cher.coursejournalbackend.model.Submission;
import com.a6raywa1cher.coursejournalbackend.model.Task;
import com.a6raywa1cher.coursejournalbackend.model.embed.SubmissionId;
import com.a6raywa1cher.coursejournalbackend.model.repo.StudentRepository;
import com.a6raywa1cher.coursejournalbackend.model.repo.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.spi.BackendIdConverter;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class SubmissionIdBackendIdConverter implements BackendIdConverter {
    private static final Pattern INPUT_PATTERN = Pattern.compile("^(\\d+)_(\\d+)$");
    private static final String OUTPUT_FORMAT = "%s_%s";

    private final TaskRepository taskRepository;
    private final StudentRepository studentRepository;

    @Autowired
    public SubmissionIdBackendIdConverter(StudentRepository studentRepository, TaskRepository taskRepository) {
        this.studentRepository = studentRepository;
        this.taskRepository = taskRepository;
    }

    @Override
    public SubmissionId fromRequestId(String id, Class<?> entityType) {
        Matcher matcher = INPUT_PATTERN.matcher(id);

        if (!matcher.find()) {
            return new SubmissionId();
        }

        long taskId = Long.parseUnsignedLong(matcher.group(1));
        long studentId = Long.parseUnsignedLong(matcher.group(2));

        Task task = taskRepository.findById(taskId).orElse(null);
        Student student = studentRepository.findById(studentId).orElse(null);

        return new SubmissionId(task, student);
    }

    @Override
    public String toRequestId(Serializable id, Class<?> entityType) {
        SubmissionId submissionId = (SubmissionId) id;

        Task task = submissionId.getTask();
        Student student = submissionId.getStudent();

        return String.format(OUTPUT_FORMAT, task.getId(), student.getId());
    }

    @Override
    public boolean supports(Class<?> type) {
        return Submission.class.equals(type);
    }
}
