package com.rbcsystem.demo.persistence.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.rbcsystem.demo.persistence.entities.StatusTicket;
import com.rbcsystem.demo.persistence.entities.TicketEntity;
import com.rbcsystem.demo.persistence.entities.enums.TicketCondition;

@Repository
public interface StatusTicketRepository extends JpaRepository<StatusTicket, Long>{
    List<StatusTicket> findAllByCondition(TicketCondition condition);

    boolean existsByTicketAndCondition(TicketEntity ticket, TicketCondition condition);

    void deleteByTicketAndCondition(TicketEntity ticket, TicketCondition condition);

    void deleteByTicketIdAndCondition(Long ticketId, TicketCondition condition);

    List<StatusTicket> findByTicketIdAndCondition(Long ticketId, TicketCondition condition);

    // Count all overdue tickets
    long countByCondition(TicketCondition condition);

    // Check if a ticket with a specific ID and "OVERDUE" status exists
    boolean existsByTicketIdAndCondition(Long ticketId, TicketCondition condition);

    boolean existsByCondition(TicketCondition condition);
}
