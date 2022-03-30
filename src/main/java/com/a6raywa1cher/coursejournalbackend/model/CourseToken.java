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
import java.util.Objects;

@Entity
@Table(name = "course_token")
@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class CourseToken {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false, updatable = false)
    @ReadOnlyProperty
    private Long id;

    @OneToOne(optional = false)
    @JoinColumn(name = "course")
    private Course course;

    @Column(name = "token", nullable = false, unique = true)
    private String token;

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
        CourseToken course = (CourseToken) o;
        return id != null && Objects.equals(id, course.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
