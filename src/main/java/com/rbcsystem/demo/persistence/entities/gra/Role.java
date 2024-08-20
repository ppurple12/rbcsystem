package com.rbcsystem.demo.persistence.entities.gra;

import java.util.List;

import com.rbcsystem.demo.persistence.entities.TicketMap;

import jakarta.persistence.*;

@Entity
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String role_name;
    private Integer upper_role_id;

    @OneToMany(mappedBy = "role", cascade = CascadeType.REMOVE, fetch = FetchType.EAGER)
    private List<TicketMap> ticketMaps;
    // Default constructor
    public Role() {}

    // Constructor with parameters
    public Role(String roleName, int roleID, int upperRoleID) {
        this.role_name = roleName;
        
        this.upper_role_id = upperRoleID;
    }

    // Getters
    public String getRoleName() {
        return role_name;
    }

    public Long getId() {
        return id;
    }

    public int getUpperRoleID() {
        return upper_role_id;
    }

    // Setters
    public void setRoleName(String roleName) {
        this.role_name = roleName;
    }

    public void setRoleID(Long roleID) {
        this.id = roleID;
    }

    public void setUpperRoleID(int upperRoleID) {
        this.upper_role_id = upperRoleID;
    }
}