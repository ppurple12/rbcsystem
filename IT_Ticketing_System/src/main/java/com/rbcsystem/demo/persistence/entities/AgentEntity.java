package com.rbcsystem.demo.persistence.entities;

import jakarta.persistence.*;
import java.util.List;

import com.rbcsystem.demo.persistence.entities.gra.Qualifications;

@Entity
@Table(name = "agents")
public class AgentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "credit_points")
    private int creditPoints;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false) 
    private ClientEntity user;
    
    @OneToMany(mappedBy = "agent", cascade = CascadeType.REMOVE, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<TicketMap> ticketMaps;

    @OneToMany(mappedBy = "agent", cascade = CascadeType.REMOVE, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<Qualifications> qualifications;;

    @Transient
    private int ticketCount;

    @Transient
    private int totalEstimatedTime;

    // Constructors
    public AgentEntity() {
    }

    public AgentEntity(int creditPoints, ClientEntity user) {
        this.creditPoints = creditPoints;
        this.user = user;
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getCreditPoints() {
        return creditPoints;
    }

    public void setCreditPoints(int creditPoints) {
        this.creditPoints = creditPoints;
    }

    public ClientEntity getUser() {
        return user;
    }

    public void setUser(ClientEntity user) {
        this.user = user;
    }

    public List<TicketMap> getTickets() {
        return ticketMaps;
    }

    public void setTickets(List<TicketMap> tickets) {
        this.ticketMaps = tickets;
    }


     // Calculate the number of tickets
     public int getTicketCount() {
        return ticketMaps != null ? ticketMaps.size() : 0;
    }

    // Calculate the total estimated time of all tickets
    public double getTotalEstimatedTime() {
        if (ticketMaps == null) {
            return 0;
        }

        double sum = 0;
        for (TicketMap ticketMap : ticketMaps) {
            
            TicketEntity ticket = ticketMap.getTicket();
            if (ticket != null) {
                sum += ticket.getEstimatedCompletionTime();
            }
           
        }
        return sum;
    }

    // Calculate agent's busyness
    public double calculateBusyness() {
        double priority = 0;
        int estimatedTimeSum = 0;

        if (ticketMaps == null) {
            return 0;
        }
        
        for (TicketMap ticketMap : ticketMaps) {
            TicketEntity ticket = ticketMap.getTicket();
            if (ticket != null) {
                estimatedTimeSum += (ticket.getEstimatedCompletionTime() * (1 + (priority / 5))); //estimated time is weighed differently depending on priority
            }
        }
        return estimatedTimeSum;
    }
}
