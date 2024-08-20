package com.rbcsystem.demo.scheduler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.rbcsystem.demo.services.TicketService;

@Component
public class OverdueTicketScheduler {

    @Autowired
    private TicketService ticketService;

    // checks if tickets are overdue every hour
     //@Scheduled(cron = "0 * * * * ?") // Every minute
    @Scheduled(cron = "0 0 * * * ?") // Every hour
    public void checkAndAddOverdueTickets() {
        ticketService.addOverdueTickets();
    }
}