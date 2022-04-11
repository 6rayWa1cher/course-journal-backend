package com.a6raywa1cher.coursejournalbackend.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.Hibernate;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.ReadOnlyProperty;

// уточнить формулу рассчёта пропусков

// состояние прогула на выбор:
// 1. Нету - пропуск, флаг уважительной причины
// 2. Int состояний
// 3. 1 с дополнением - добавить Int уважительных состояний ++

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(
        name = "attendance",
        uniqueConstraints = @UniqueConstraint(name = "one_attendance_per_class_date_student",
                columnNames = {"attended_class", "attended_date", "student_id"})
)

@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class Attendance {
    @Id
    @GeneratedValue
    @Column(name = "id", nullable = false)
    @ReadOnlyProperty
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "course_id")
    private Course course;

    @ManyToOne(optional = false)
    @JoinColumn(name = "student_id")
    private Student student;

    @Column(name = "attended_class")
    @ReadOnlyProperty
    private Integer attendedClass;

    @Column(name = "attended_date")
    @ReadOnlyProperty
    private LocalDate attendedDate;

    @Column(name = "created_at")
    @CreatedDate
    @ReadOnlyProperty
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @LastModifiedDate
    @ReadOnlyProperty
    private LocalDateTime lastModifiedAt;

    @Column(name = "attendance_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private AttendanceType attendanceType;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        Attendance that = (Attendance) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() { return getClass().hashCode(); }
}
