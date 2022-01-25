package com.a6raywa1cher.coursejournalbackend.model;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import javax.persistence.*;
import java.time.ZonedDateTime;

@Entity
@Table(name = "task", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"course_id", "task_number"})
})
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "course_id")
    private Course course;

    @Column(name = "task_number", nullable = false)
    private Integer taskNumber;

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

    @Column(name = "soft_deadline_at")
    private ZonedDateTime softDeadlineAt;

    @Column(name = "hard_deadline_at")
    private ZonedDateTime hardDeadlineAt;

    @Column(name = "created_at")
    @CreatedDate
    private ZonedDateTime createdAt;

    @Column(name = "updated_at")
    @LastModifiedDate
    private ZonedDateTime lastModifiedAt;
}