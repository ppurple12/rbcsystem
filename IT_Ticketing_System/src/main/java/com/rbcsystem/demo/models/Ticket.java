package com.rbcsystem.demo.models;

public class Ticket {
    private int ticketID;
    private String comments;
    private Role requiredRoles;
    private Client sender;
    private int priority;
    private int time;


    //constructor made by the form information
    public Ticket(int ticketID, String comments, Role requiredRoles, Client sender, int priority, int time, Agent agent) {
        this.ticketID = ticketID;
        this.comments = comments;
        this.requiredRoles = requiredRoles;
        this.sender = sender;
        this.priority = priority;
        this.time = time;

    }

    // Setters and getters
    public int getTicketID() {
        return ticketID;
    }

    public String getComments() {
        return comments;
    }

    public Role getRequiredRoles() {
        return requiredRoles;
    }

    public Client getSender() {
        return sender;
    }

    public int getPriority() {
        return priority;
    }

    public int getTime() {
        return time;
    }

    public void setTicketID(int ticketID) {
        this.ticketID = ticketID;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public void setRequiredRoles(Role requiredRoles) {
        this.requiredRoles = requiredRoles;
    }

    public void setReceivers(Client sender) {
        this.sender = sender;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public void setTime(int time) {
        this.time = time;
    }
	
	//actual functions
}
