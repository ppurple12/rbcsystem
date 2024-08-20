package com.rbcsystem.demo.services;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.rbcsystem.demo.persistence.entities.AgentEntity;
import com.rbcsystem.demo.persistence.entities.ClientEntity;
import com.rbcsystem.demo.persistence.entities.StatusTicket;
import com.rbcsystem.demo.persistence.entities.TicketEntity;
import com.rbcsystem.demo.persistence.entities.enums.TicketCondition;
import com.rbcsystem.demo.persistence.repositories.StatusTicketRepository;
import com.rbcsystem.demo.persistence.repositories.TicketRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@Lazy(false)
public class TicketService{
    private static final Logger logger = LoggerFactory.getLogger(TicketService.class);

    @Autowired
    private TicketRepository ticketRepository;



    @Autowired
    private StatusTicketRepository statusTicketRepository;

    @PersistenceContext
    private EntityManager entityManager;

    // gets ticket with id
    @Transactional
    public TicketEntity getWithId(Long id){
        return ticketRepository.findById(id).orElse(null);
    }

    //  gets a list of tickets based off agent
    @Transactional
    public List<TicketEntity> getWithAgent(AgentEntity agent){
        return ticketRepository.findByTicketMapsAgent(agent);
    }

    //  gets all tickets for agent that have NEW status
    @Transactional
    public List<TicketEntity> getNewTickets(long agentId){
        return ticketRepository.findAllNewTicketsByAgentId(agentId);
    }

    // gets tickets by sender
    @Transactional
    public List<TicketEntity> getTicketsBySender(ClientEntity client){
        return ticketRepository.findBySender(client);
    }

    //  sorts tickets in order of priority
    @Transactional
    public List<TicketEntity> sortTickets(List<TicketEntity> tickets){
        int n = tickets.size();
        for (int i = 0; i < n - 1; i++) {
            for (int j = 0; j < n - i - 1; j++) {
                if (tickets.get(j).getPriority() < tickets.get(j + 1).getPriority()) {
                    // Swap tickets[j] with tickets[j+1]
                    TicketEntity temp = tickets.get(j);
                    tickets.set(j, tickets.get(j + 1));
                    tickets.set(j + 1, temp);
                }
            }
        }
        return tickets;
    }

    // updates time for the ticket
    @Transactional
    public void updateTime(TicketEntity ticket) {
        // Update the updatedAt timestamp
        ticket.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
        
        // Save the updated ticket
        ticketRepository.save(ticket);
    }

     
    // adds tickets to overdue list iif theyre more than a week old
    @Transactional
    public void addOverdueTickets() {
        // Calculate the cutoff date for overdue tickets
        LocalDateTime oneWeekAgo = LocalDateTime.now().minusWeeks(1);
        Timestamp cutoffDate = Timestamp.valueOf(oneWeekAgo);

        // every minute to test
        //LocalDateTime oneMinuteAgo = LocalDateTime.now().minusMinutes(1);
        //Timestamp cutoffDate = Timestamp.valueOf(oneMinuteAgo);
        
        List<TicketEntity> overdueTickets = ticketRepository.findOverdueTickets(cutoffDate);
        for (TicketEntity ticket : overdueTickets) {
            if (!statusTicketRepository.existsByTicketAndCondition(ticket, TicketCondition.OVERDUE)){
                StatusTicket overdueTicket = new StatusTicket();
                overdueTicket.setTicket(ticket);
                overdueTicket.setCondition(TicketCondition.OVERDUE);
                statusTicketRepository.save(overdueTicket);
            }
        }
    }

    //  deletes a ticket
    @Transactional
    public void deleteTicket(TicketEntity ticket) {
        try {
            if (!entityManager.contains(ticket)) {
                ticket = entityManager.merge(ticket);
            }
            if (statusTicketRepository.existsByTicketAndCondition(ticket, TicketCondition.OVERDUE)){
                statusTicketRepository.deleteByTicketAndCondition(ticket, TicketCondition.OVERDUE);               
            }
            if (statusTicketRepository.existsByTicketAndCondition(ticket, TicketCondition.NEW)){
                statusTicketRepository.deleteByTicketAndCondition(ticket, TicketCondition.NEW);               
            }
            ticketRepository.delete(ticket);
            logger.info("Ticket with ID {} deleted successfully.", ticket.getId());
        } catch (Exception e) {
            logger.error("Error occurred while deleting ticket with ID: {}", ticket.getId(), e);
        }
    }

    //  save tickets
    @Transactional
    public void saveTicket(TicketEntity ticket){
        ticketRepository.save(ticket);
    }

    //returns a string to show time properly
    @Transactional
    public String formatTime(double time) {
        StringBuilder formattedTime = new StringBuilder();
        
        int hours = (int) time;
        double fractional = time - hours;
        
        // Convert double fractional part to string for precise comparison
        String fractionalString = Double.toString(fractional);
        
        if (fractionalString.equals("0.25")) {
            formattedTime.append(hours).append(" \u00BC"); // 1/4 Unicode character
        } else if (fractionalString.equals("0.5")) {
            formattedTime.append(hours).append(" \u00BD"); // 1/2 Unicode character
        } else if (fractionalString.equals("0.75")) {
            formattedTime.append(hours).append(" \u00BE"); // 3/4 Unicode character
        } else {
            formattedTime.append(hours);
        }
        
        return formattedTime.toString();
    }
}
