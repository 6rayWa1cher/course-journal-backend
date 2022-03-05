package com.a6raywa1cher.coursejournalbackend.model;

import com.a6raywa1cher.coursejournalbackend.security.Owned;
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
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "task")
@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class Task implements Owned {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    @ReadOnlyProperty
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "course_id")
    private Course course;

    @Column(name = "task_number")
    private Integer taskNumber; // contains unique constraint in init.sql file

    @Column(name = "title")
    private String title;

    @Column(name = "description")
    @Lob
    private String description;

    @Column(name = "max_score")
    private Integer maxScore;

    @Column(name = "max_penalty_percent")
    private Integer maxPenaltyPercent;

    @Column(name = "announced")
    private Boolean announced;

    @Column(name = "announcement_at")
    private ZonedDateTime announcementAt;

    @OneToMany(mappedBy = "primaryKey.task", orphanRemoval = true, cascade = CascadeType.ALL)
    @ToString.Exclude
    private List<Submission> submissions;

    @Column(name = "soft_deadline_at")
    private ZonedDateTime softDeadlineAt;

    @Column(name = "hard_deadline_at")
    private ZonedDateTime hardDeadlineAt;

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
        Task task = (Task) o;
        return id != null && Objects.equals(id, task.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public long getOwnerId() {
        return course.getOwnerId();
    }
}