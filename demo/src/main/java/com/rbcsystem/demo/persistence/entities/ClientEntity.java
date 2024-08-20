package com.rbcsystem.demo.persistence.entities;

import java.util.List;
import com.rbcsystem.demo.persistence.entities.enums.AccessLevel;


import jakarta.persistence.*;
//entity class for clients mapped to clients table
@Entity
@Table(name = "users")
public class ClientEntity extends UserEntity {

    //instantiations
    private int importance;

    @Column(name = "access_level")
    @Enumerated(EnumType.STRING)
    private AccessLevel accessLevel;

    @OneToMany(mappedBy = "sender", cascade = CascadeType.ALL)
    private List<TicketEntity> tickets; 

    @Transient
    private int ticketCount;

    private String enterprise;

    

    //constructors - param n non-param
    public ClientEntity() {
    }

    public ClientEntity(String name, String email, String password, String department, String position, int importance, AccessLevel accessLevel) {
        super(name, email, password, department, position);
        this.importance = importance;
        this.accessLevel = accessLevel;
    }

    //setter n getters
    public int getImportance() {
        return importance;
    }

    public void setImportance(int importance) {
        this.importance = importance;
    }

    public AccessLevel getAccessLevel() {
        return accessLevel;
    }

    public void setAccessLevel(AccessLevel accessLevel) {
        this.accessLevel = accessLevel;
    }

    // Calculate the number of tickets
    public int getTicketCount() {
        return tickets != null ? tickets.size() : 0;
    }

    public String getEnterprise(){
        return this.enterprise;
    }

    public void setEnterprise(String enterprise){
        this.enterprise = enterprise;
    }
}