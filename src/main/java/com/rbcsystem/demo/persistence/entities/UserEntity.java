package com.rbcsystem.demo.persistence.entities;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;

@MappedSuperclass
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    //intialization
    private Long id;
    private String name;
    private String email;
    private String password;
    private String department;
    private String position;

    // No-arg constructor
    public UserEntity() {
    }

    // Constructor with parameters
    public UserEntity(String name, String email, String password, String department, String position) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.department = department;
        this.position = position;
    }

    // Getter and setter methods
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }
}