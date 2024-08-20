package com.rbcsystem.demo.controller.dashboard;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.rbcsystem.demo.config.SchemaContext;
import com.rbcsystem.demo.persistence.entities.ClientEntity;
import com.rbcsystem.demo.persistence.entities.TicketEntity;
import com.rbcsystem.demo.services.TicketService;

import jakarta.servlet.http.HttpSession;
import java.util.List;

@Controller
public class DefaultDashboardController {

    @Autowired
    private TicketService ticketService;

    //creates session client and finds all relating tickets
    @GetMapping("/dashboard/default")
    public String defaultDashboard(Model model, HttpSession session) {
        ClientEntity client = (ClientEntity) session.getAttribute("loggedInClient");
        if (client == null || SchemaContext.getSchema() == null ) {
            return "redirect:/login-info";
        }

        List<TicketEntity> activeTickets = ticketService.getTicketsBySender(client);
        model.addAttribute("client", client);
        model.addAttribute("activeTickets", activeTickets);

        return "dashboard/default";
    }
}