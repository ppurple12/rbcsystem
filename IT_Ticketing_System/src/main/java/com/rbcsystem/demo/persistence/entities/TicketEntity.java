package com.rbcsystem.demo.persistence.entities;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import java.sql.Timestamp;

import jakarta.persistence.*;

@Entity
@Table(name = "tickets")
public class TicketEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "subject", nullable = false)
    private String subject;

    @Column(name = "comments", nullable = true)
    private String comments;

    @ManyToOne
    @JoinColumn(name = "sender_id", referencedColumnName = "id", nullable = false)
    private ClientEntity sender;

    @OneToMany(mappedBy = "ticket", cascade = CascadeType.REMOVE, fetch = FetchType.EAGER)
    private List<TicketMap> ticketMaps;

    @Column(name = "priority", nullable = false)
    private int priority;

    @Column(name = "time", nullable = false)
    private Double estimatedCompletionTime;
    
    @Column(name = "task_id", nullable = true) // Added column for task ID
    private Long taskId; // Task ID reference

    @Column(name = "created_at", nullable = false)
    private Timestamp createdAt;

    @Column(name = "updated_at", nullable = false)
    private Timestamp updatedAt; 

    

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public ClientEntity getSender() {
        return sender;
    }

    public void setSender(ClientEntity sender) {
        this.sender = sender;
    }


    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public Double getEstimatedCompletionTime() {
        return estimatedCompletionTime;
    }

    public void setEstimatedCompletionTime(Double time) {
        this.estimatedCompletionTime = time;
    }

    public List<TicketMap> getTicketMaps(){
        return this.ticketMaps;
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = new Timestamp(System.currentTimeMillis());
        this.updatedAt = new Timestamp(System.currentTimeMillis());
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = new Timestamp(System.currentTimeMillis());
    }

    // Method to format the timestamp
    public String getFormattedCreatedAt() {
        LocalDateTime localDateTime = createdAt.toLocalDateTime();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM dd, HH:mm");
        return localDateTime.format(formatter);
    }

    public String getFormattedUpdatedAt() {
        LocalDateTime localDateTime = updatedAt.toLocalDateTime();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM dd, HH:mm");
        return localDateTime.format(formatter);
    }
    public String getFormattedOverdueAt() {
        LocalDateTime localDateTime = updatedAt.toLocalDateTime();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM dd");
        return localDateTime.format(formatter);
    }

    // Getters and setters for createdAt and updatedAt
    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    //formats time for specific tickets
    public String formatTime(){
        double time = this.estimatedCompletionTime.doubleValue();
        String formattedTime;
        if (time < 1) {
            int hours = (int) time;
            double fractional = time - hours;
            if (fractional == 0.25) {
                formattedTime = "Fifteen minutes";
            }
            else if (fractional == 0.5) {
                formattedTime = "Half an hour";
            } 
            else{
                formattedTime = "Forty-five minutes";
            }
        } else if (time == 1) {
            formattedTime = "1 hour";
        } else {
            int hours = (int) time;
            double fractional = time - hours;
            if (fractional == 0.25) {
                formattedTime = hours + " hours and 1/4";
            } 
            else if (fractional == 0.5) {
                formattedTime = hours + " hours and 1/2";
            } 
            else if (fractional == 0.75) {
                formattedTime = hours + " hours and 3/4";
            } 
            else {
                formattedTime = hours + " hours";
            }
        }
        return formattedTime;
    }
}
