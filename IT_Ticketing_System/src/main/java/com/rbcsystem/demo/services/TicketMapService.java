package com.rbcsystem.demo.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rbcsystem.demo.persistence.entities.AgentEntity;
import com.rbcsystem.demo.persistence.entities.TicketEntity;
import com.rbcsystem.demo.persistence.entities.TicketMap;
import com.rbcsystem.demo.persistence.entities.enums.TicketStatus;
import com.rbcsystem.demo.persistence.repositories.TicketMapRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Service
@Lazy(false)
public class TicketMapService {

    @Autowired
    private TicketMapRepository ticketMapRepository;

    



    @PersistenceContext
    private EntityManager entityManager;

    //  returns amount of tickets per agent
    @Transactional
    public Long returnCount(Long agentId){
        return ticketMapRepository.countByAgentId(agentId);
    }

    //  removes agent and ticket
    @Transactional
    public void removeAgentAndTicket(TicketMap ticketMap) {
        try {
           
            if (ticketMap != null) {
                ticketMapRepository.delete(ticketMap);
                System.out.println(ticketMap.getId());
            } else {
                System.out.println("TicketMap entry not found for agent and ticket.");
            }
            
        } catch (Exception e) {
            System.err.println("Error occurred while deleting TicketMap entry: " + e.getMessage());
        }
    }

    
    //removes all ticketmap entries by ticket
    @Transactional
    public void removeByTicket(TicketEntity ticket){
        List<TicketMap> ticketMaps = ticketMapRepository.findByTicket(ticket);
        if (ticketMaps != null){
            for (TicketMap ticketM : ticketMaps){
                ticketMapRepository.delete(ticketM);
            }
        }
    }

    //  updates status of specific agent and ticket
    @Transactional
    public void updateStatus(Long ticketMapId, TicketStatus status){
        ticketMapRepository.updateTicketMapStatus(ticketMapId, status);
    }

    //  returns ticketMap instance using agent and ticket
    @Transactional
    public TicketMap getTicketMap(TicketEntity ticket, AgentEntity agent){
        return ticketMapRepository.findByAgentAndTicket(agent, ticket);
    }

    //  returns all tickets that have the status "NEW"
    @Transactional
    public List<TicketMap> getAllNewTicketsByAgentId(long agentId) {
        return ticketMapRepository.findAllNewTicketsByAgentId(agentId);
    }

    //   returns sum of estimated time for agent
    @Transactional
    public Double getTotalEstimatedCompletionTimeByAgentId(Long agentId) {
        Double totalTime = ticketMapRepository.sumEstimatedCompletionTimeByAgentId(agentId);
        return totalTime != null ? totalTime : 0;
    }

    //  saves map
    @Transactional
    public void saveMap(TicketMap ticketMap){
        ticketMapRepository.save(ticketMap);
    }

    // gets ticktemap with its id
    @Transactional
    public List<TicketMap> getWithId(TicketEntity ticket){
        return ticketMapRepository.findByTicket(ticket);
    }
}