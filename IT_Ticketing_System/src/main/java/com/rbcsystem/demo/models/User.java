package com.rbcsystem.demo.models;

public class User {
	private int id;
    private String name;
    private String email;
    private String password;
    private String department;
    private String position;

    // No-argument constructor
    public User() {
    }

    // Parameterized constructor
    public User(int id, String name, String email, String password, String department, String position) {
		this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.department = department;
        this.position = position;
    }

    // Getters and setters
	public int getId(){
		return id;
	}
	
	public void setId(int id){
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