package com.a6raywa1cher.coursejournalbackend.model;

import com.a6raywa1cher.coursejournalbackend.model.embed.SubmissionId;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.Hibernate;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.ReadOnlyProperty;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "submission")
@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class Submission {
    @EmbeddedId
    private SubmissionId primaryKey;

    @Column(name = "submitted_at", nullable = false)
    private LocalDateTime submittedAt;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "submission_criteria",
            joinColumns = {
                    @JoinColumn(name = "task_id", referencedColumnName = "task_id"),
                    @JoinColumn(name = "student_id", referencedColumnName = "student_id")
            },
            inverseJoinColumns = {
                    @JoinColumn(name = "criteria_id", referencedColumnName = "id")
            }
    )
    @ToString.Exclude
    private List<Criteria> satisfiedCriteria;

    @Column(name = "main_score")
    private Integer mainScore;

    @Column(name = "additional_score")
    private Integer additionalScore;

    @Column(name = "created_at")
    @CreatedDate
    @ReadOnlyProperty
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @LastModifiedDate
    @ReadOnlyProperty
    private LocalDateTime lastModifiedAt;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        Submission that = (Submission) o;
        return primaryKey != null && Objects.equals(primaryKey, that.primaryKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(primaryKey);
    }
}