package com.rbcsystem.demo.persistence.entities;

import com.rbcsystem.demo.persistence.entities.gra.Role;
import com.rbcsystem.demo.persistence.entities.enums.TicketStatus;

import jakarta.persistence.*;

@Entity
@Table(name = "ticketmap")
public class TicketMap {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "agent_id")
    private AgentEntity agent;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ticket_id", referencedColumnName = "id", nullable = false)
    private TicketEntity ticket;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id")
    private Role role;
   
    @Enumerated(EnumType.STRING)
    @Column(name = "ticket_status") // Map to the corresponding database column
    private TicketStatus ticketStatus;

    // getters and setters
    public Long getId(){
        return id;
    }
    public AgentEntity getAgent() {
        return agent;
    }

    public void setAgent(AgentEntity agent) {
        this.agent = agent;
    }

    public TicketEntity getTicket() {
        return ticket;
    }

    public void setTicket(TicketEntity ticket) {
        this.ticket = ticket;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public TicketStatus getTicketStatus() {
        return ticketStatus;
    }

    public void setTicketStatus(TicketStatus ticketStatus) {
        this.ticketStatus = ticketStatus;
    }
}
