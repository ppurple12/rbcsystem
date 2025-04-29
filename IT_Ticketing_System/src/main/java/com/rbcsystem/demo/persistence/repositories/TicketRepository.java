package com.rbcsystem.demo.persistence.repositories;


import com.rbcsystem.demo.models.Ticket;
import com.rbcsystem.demo.persistence.entities.AgentEntity;
import com.rbcsystem.demo.persistence.entities.ClientEntity;
import com.rbcsystem.demo.persistence.entities.TicketEntity;
import com.rbcsystem.demo.persistence.entities.TicketMap;
import com.rbcsystem.demo.persistence.entities.enums.TicketStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


//simple repo class that extends JPA
@Repository
public interface TicketRepository extends JpaRepository<TicketEntity, Long> {

    ArrayList<TicketEntity> findBySender(ClientEntity sender);
    //finds ticket using agent through ticket map 
    ArrayList<TicketEntity> findByTicketMapsAgent(AgentEntity agent);

    boolean existsByTaskId(Long taskId);
    boolean existsBySenderId(Long id);
    
    // find all tickets with new status for agent
    @Query("SELECT te FROM TicketEntity te JOIN TicketMap tm ON te.id = tm.ticket.id WHERE tm.agent.id = :agentId AND tm.ticketStatus = 'NEW'")
    List<TicketEntity> findAllNewTicketsByAgentId(@Param("agentId") long agentId);

    // finds all overdue tickets
    @Query("SELECT t FROM TicketEntity t WHERE t.updatedAt < :cutoffDate")
    List<TicketEntity> findOverdueTickets(@Param("cutoffDate") java.sql.Timestamp cutoffDate);

    TicketEntity save(TicketEntity ticket);
    

}