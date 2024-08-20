package com.rbcsystem.demo.persistence.entities;

import jakarta.persistence.*;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Entity
@Table(name = "updatelog")
public class UpdateLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "ticket_id", nullable = false)
    private TicketEntity ticket;

    @ManyToOne
    @JoinColumn(name = "updated_by", nullable = false)
    private ClientEntity updatedBy;

    @Column(name = "update_time", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private Timestamp updateTime;

    @Column(name = "update_description")
    private String updateDescription;

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TicketEntity getTicket() {
        return ticket;
    }

    public void setTicket(TicketEntity ticket) {
        this.ticket = ticket;
    }

    public ClientEntity getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(ClientEntity updatedBy) {
        this.updatedBy = updatedBy;
    }

    public Timestamp getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Timestamp updateTime) {
        this.updateTime = updateTime;
    }

      @PreUpdate
    protected void onUpdate() {
        this.updateTime = new Timestamp(System.currentTimeMillis());
    }


    public String getFormattedUpdatedAt() {
        LocalDateTime localDateTime = updateTime.toLocalDateTime();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM dd, HH:mm");
        return localDateTime.format(formatter);
    }


    public String getUpdateDescription() {
        return updateDescription;
    }

    public void setUpdateDescription(String updateDescription) {
        this.updateDescription = updateDescription;
    }
}