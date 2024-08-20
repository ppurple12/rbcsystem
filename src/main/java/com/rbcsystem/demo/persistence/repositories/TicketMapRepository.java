package com.rbcsystem.demo.persistence.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.rbcsystem.demo.persistence.entities.AgentEntity;
import com.rbcsystem.demo.persistence.entities.TicketEntity;
import com.rbcsystem.demo.persistence.entities.TicketMap;
import com.rbcsystem.demo.persistence.entities.enums.TicketStatus;

import java.util.List;

//simple repo class that extends JPARepo
@Repository
public interface TicketMapRepository extends JpaRepository<TicketMap, Long> {

    List<TicketMap> findByAgent_Id(Long agentId);

    List<TicketMap> findByAgent(AgentEntity agent);

    List<TicketMap> findByTicket(TicketEntity ticket);

    TicketMap findByAgentAndTicket(AgentEntity agent, TicketEntity ticket);

    TicketMap save(TicketMap ticketMap);
   
    void deleteByTicket(TicketEntity ticketId);

    void deleteByAgentAndTicket(AgentEntity agent, TicketEntity ticket);

    TicketMap findByTicketAndAgent(TicketEntity ticket, AgentEntity agent);

    boolean existsByAgentId(Long agentId);

    //returns new tickets for each agent
    @Query("SELECT t FROM TicketMap t WHERE t.agent.id = :agentId AND t.ticketStatus = 'NEW'")
    List<TicketMap> findAllNewTicketsByAgentId(@Param("agentId") long agentId);

    // returns the amount of time it takes for agent to complete all tasks
    @Query("SELECT SUM(t.estimatedCompletionTime) FROM TicketMap tm JOIN tm.ticket t WHERE tm.agent.id = :agentId")
    Double sumEstimatedCompletionTimeByAgentId(@Param("agentId") Long agentId);
    
    // returns amount of tickets
    @Query("SELECT COUNT(tm) FROM TicketMap tm WHERE tm.agent.id = :agentId")
    long countByAgentId(@Param("agentId") Long agentId);

    // updates status of specific ticket
    @Modifying
    @Transactional
    @Query("UPDATE TicketMap t SET t.ticketStatus = :status WHERE t.id = :ticketMapId")
    void updateTicketMapStatus(Long ticketMapId, TicketStatus status);

}