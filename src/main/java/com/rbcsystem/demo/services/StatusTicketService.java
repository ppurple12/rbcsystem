package com.rbcsystem.demo.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.rbcsystem.demo.persistence.entities.StatusTicket;
import com.rbcsystem.demo.persistence.entities.TicketEntity;
import com.rbcsystem.demo.persistence.entities.enums.TicketCondition;
import com.rbcsystem.demo.persistence.repositories.StatusTicketRepository;

import jakarta.transaction.Transactional;

@Service
public class StatusTicketService {
    @Autowired
    private  StatusTicketRepository statusTicketRepository;
    


    @Transactional
    public List<StatusTicket> findAllByCondition(TicketCondition condition) {
        return statusTicketRepository.findAllByCondition(condition);
    }

    @Transactional
    public boolean existsByTicketAndCondition(TicketEntity ticket, TicketCondition condition) {
        return statusTicketRepository.existsByTicketAndCondition(ticket, condition);
    }

    @Transactional
    public void deleteByTicketAndCondition(TicketEntity ticket, TicketCondition condition) {
        statusTicketRepository.deleteByTicketAndCondition(ticket, condition);
    }

    @Transactional
    public void deleteByTicketIdAndCondition(Long ticketId, TicketCondition condition) {
        statusTicketRepository.deleteByTicketIdAndCondition(ticketId, condition);
    }

    @Transactional
    public List<StatusTicket> findByTicketIdAndCondition(Long ticketId, TicketCondition condition) {
        return statusTicketRepository.findByTicketIdAndCondition(ticketId, condition);
    }

    @Transactional
    public long countByCondition(TicketCondition condition) {
        return statusTicketRepository.countByCondition(condition);
    }

    @Transactional
    public boolean existsByTicketIdAndCondition(Long ticketId, TicketCondition condition) {
        return statusTicketRepository.existsByTicketIdAndCondition(ticketId, condition);
    }

    @Transactional
    public boolean existsByCondition(TicketCondition condition){
        return statusTicketRepository.existsByCondition(condition);
    }

    @Transactional
    public StatusTicket save(StatusTicket statusTicket) {
        return statusTicketRepository.save(statusTicket);
    }
}