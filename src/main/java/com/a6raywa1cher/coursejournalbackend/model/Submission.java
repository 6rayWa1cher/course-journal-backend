package com.a6raywa1cher.coursejournalbackend.model;

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
@Table(
        name = "submission",
        uniqueConstraints = @UniqueConstraint(name = "one_sub_per_task_student", columnNames = {"task_id", "student_id"})
)
@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class Submission implements IdEntity<Long> {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    @ReadOnlyProperty
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "task_id", nullable = false, updatable = false)
    private Task task;

    @ManyToOne(optional = false)
    @JoinColumn(name = "student_id", nullable = false, updatable = false)
    private Student student;

    @Column(name = "submitted_at", nullable = false)
    private LocalDateTime submittedAt;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "submission_criteria",
            joinColumns = {
                    @JoinColumn(name = "submission_id", referencedColumnName = "id"),
            },
            inverseJoinColumns = {
                    @JoinColumn(name = "criteria_id", referencedColumnName = "id")
            }
    )
    @ToString.Exclude
    private List<Criteria> satisfiedCriteria;

    @Column(name = "main_score", precision = 3, scale = 2)
    private Double mainScore;

    @Column(name = "additional_score", precision = 3, scale = 2)
    private Double additionalScore;

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
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}