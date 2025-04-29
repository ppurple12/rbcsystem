package com.rbcsystem.demo.models;


public class Role {
    private String roleName;
    private int roleID;
    private int upperRoleID;

    // Constructor with parameters
    public Role(String roleName, int roleID, int upperRoleID) {
        this.roleName = roleName;
        this.roleID = roleID;
        this.upperRoleID = upperRoleID;
    }
    
    //setters and getters
    public String getRoleName() {
        return roleName;
    }

    public int getRoleID() {
        return roleID;
    }

    public int getUpperRoleID() {
        return upperRoleID;
    }

    // Setter methods
    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public void setRoleID(int roleID) {
        this.roleID = roleID;
    }

    public void setUpperRoleID(int upperRoleID) {
        this.upperRoleID = upperRoleID;
    }

}
