package com.a6raywa1cher.coursejournalbackend.model.embed;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Data
@Embeddable
public class FullName {
    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "middle_name")
    private String middleName;

    @JsonInclude
    public String getFullName() {
        return String.join(" ", lastName, firstName, middleName);
    }
}