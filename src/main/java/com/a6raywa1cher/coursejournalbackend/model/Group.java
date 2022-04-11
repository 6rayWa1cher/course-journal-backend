package com.a6raywa1cher.coursejournalbackend.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.ReadOnlyProperty;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "group")
@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class Group implements IdEntity<Long> {
    @Id
    @GeneratedValue
    @Column(name = "id", nullable = false)
    @ReadOnlyProperty
    private Long id;

    @Column(name = "faculty", nullable = false)
    private String facultyName;

    @Column(name = "name", nullable = false)
    private String name;

    @ManyToOne(optional = false)
    private Course course;

    @OneToMany(mappedBy = "group", orphanRemoval = true, cascade = CascadeType.ALL)
    @ToString.Exclude
    private List<Student> students;

    @Column(name = "created_at")
    @CreatedDate
    @ReadOnlyProperty
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @LastModifiedDate
    @ReadOnlyProperty
    private LocalDateTime lastModifiedAt;
}
