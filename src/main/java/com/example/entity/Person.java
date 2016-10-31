package com.example.entity;

import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.hateoas.ResourceSupport;

import javax.validation.constraints.Size;

/**
 * Created by rodrigo on 30/10/16.
 */
@Document
public class Person extends ResourceSupport {
    @Id
    private String identification;
    @NotBlank(message = "error.name.notblank")
    @Size(min = 10, max = 30, message = "error.name.size")
    private String name;
    @NotBlank(message = "error.email.notblank")
    @Email(message = "error.email.email")
    private String email;
    @Transient
    private Boolean edit = false;

    public Person() {}

    public Person(String name, String email) {
        this.name = name;
        this.email = email;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getIdentification() {
        return identification;
    }

    public void setEdit(Boolean edit) {
        this.edit = edit;
    }

    public Boolean getEdit() {
        return edit;
    }

    public void setIdentification(String identification) {
        this.identification = identification;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public String toString() {
        return "Person{" +
                "email='" + email + '\'' +
                ", identification='" + identification + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
