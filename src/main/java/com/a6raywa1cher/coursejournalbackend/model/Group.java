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
        name = "group",
        uniqueConstraints = @UniqueConstraint(name = "one_name_per_faculty", columnNames = {"name", "faculty"})
)
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

    @JoinColumn(name = "faculty", nullable = false)
    @ManyToOne(optional = false)
    private Faculty faculty;

    @Column(name = "name", nullable = false)
    private String name;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        Group that = (Group) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
