package com.rbcsystem.demo.persistence.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.rbcsystem.demo.persistence.entities.UpdateLog;

import java.util.List;

public interface UpdateLogRepository extends JpaRepository<UpdateLog, Long> {

    List<UpdateLog> findByTicketId(Long ticketId);
    
    void deleteByTicketId(Long ticketId);
}