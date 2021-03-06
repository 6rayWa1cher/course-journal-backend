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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "criteria", uniqueConstraints = @UniqueConstraint(name = "criteria_task_name_uniq", columnNames = {"task_id", "name"}))
@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class Criteria implements IdEntity<Long> {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    @ReadOnlyProperty
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "task_id")
    private Task task;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "criteria_percent")
    private Integer criteriaPercent;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, mappedBy = "satisfiedCriteria")
    @ToString.Exclude
    private List<Submission> submissionList = new ArrayList<>();

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
        Criteria criteria = (Criteria) o;
        return id != null && Objects.equals(id, criteria.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}