package com.rbcsystem.demo.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rbcsystem.demo.persistence.entities.UpdateLog;
import com.rbcsystem.demo.persistence.repositories.UpdateLogRepository;

@Service
@Lazy(false)
public class UpdateLogService {

    @Autowired
    private UpdateLogRepository updateLogRepository;

    //  gets update logs for ticket
    @Transactional
    public List<UpdateLog> getUpdateLogsByTicketId(Long ticketId) {
        return updateLogRepository.findByTicketId(ticketId);
    }

    //deletes update logs associated with tickets
    @Transactional
    public void deleteUpdateLogsByTicketId(Long ticketId) {
     
        // Delete update logs associated with the ticket
        updateLogRepository.deleteByTicketId(ticketId);
    }

    // saves log to db
    @Transactional
    public void saveLog(UpdateLog log){
        updateLogRepository.save(log);
    }

    
}