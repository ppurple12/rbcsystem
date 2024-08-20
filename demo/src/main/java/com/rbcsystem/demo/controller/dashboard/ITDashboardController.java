package com.rbcsystem.demo.controller.dashboard;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import jakarta.servlet.http.HttpSession;

import com.rbcsystem.demo.config.SchemaContext;
import com.rbcsystem.demo.persistence.entities.AgentEntity;
import com.rbcsystem.demo.persistence.entities.TicketEntity;
import com.rbcsystem.demo.persistence.entities.TicketMap;
import com.rbcsystem.demo.services.AgentService;
import com.rbcsystem.demo.services.QualificationService;
import com.rbcsystem.demo.services.RoleService;
import com.rbcsystem.demo.services.TicketMapService;
import com.rbcsystem.demo.services.TicketService;

import org.springframework.ui.Model;

@Controller
public class ITDashboardController {

      //makes ticket repo

    @Autowired 
    private QualificationService qualificationService;
    
    @Autowired
    private TicketService ticketService;

     
    @Autowired
    private TicketMapService ticketMapService;


    @Autowired
    private AgentService agentService;
    @Autowired
    private RoleService roleService;

      //creates session agent and finds all relating tickets
    @GetMapping("/dashboard/it")
    public String itDashboard(Model model, HttpSession session){
        AgentEntity loggedInAgent = (AgentEntity) session.getAttribute("loggedInAgent");
        if (loggedInAgent == null || SchemaContext.getSchema() == null) {
          return "redirect:/login-info";
        }
        
        //USED AS DEBUG 
        // if i fetch eagerly the ticketMap entity, i cant delete it from databse because its being used, 
        // so i use these workarounds to get values i need without getting all the ticketmap instances
        String estimatedCompletionTime = ticketService.formatTime(ticketMapService.getTotalEstimatedCompletionTimeByAgentId(loggedInAgent.getId()));
        long ticketAmt = ticketMapService.returnCount(loggedInAgent.getId());


        List<TicketEntity> newTickets = ticketService.getNewTickets(loggedInAgent.getId());
        List<TicketEntity> tickets = agentService.getTicketsByAgentId(loggedInAgent.getId());
        tickets = ticketService.sortTickets(tickets);
        
        Map<Long, TicketMap> ticketMaps = new HashMap<>();
        for (TicketEntity ticket : tickets) {
            TicketMap ticketMap = ticketMapService.getTicketMap(ticket, loggedInAgent);
            ticketMaps.put(ticket.getId(), ticketMap);
        }
        model.addAttribute("estimatedCompletionTime", estimatedCompletionTime);
        model.addAttribute("ticketAmt", ticketAmt);
        model.addAttribute("loggedInAgent", loggedInAgent);
        model.addAttribute("newTickets", newTickets);
        model.addAttribute("activeTickets", tickets);
        model.addAttribute("ticketMaps", ticketMaps);
        

        return "dashboard/it";
    }
    
    //  mapping for visualization of agents' ablities
    @GetMapping("/statistics")
    public String getStatistics(Model model, HttpSession session){
      AgentEntity loggedInAgent = (AgentEntity) session.getAttribute("loggedInAgent");
      if (loggedInAgent == null || SchemaContext.getSchema() == null) {
        return "redirect:/login-info";
      }
      double[] abilities = qualificationService.getAbilitiesByAgentId(loggedInAgent.getId());
      List<String> roles = roleService.getAllRoleNames();
      model.addAttribute("roles", roles);
      
      model.addAttribute("loggedInAgent", loggedInAgent);
      model.addAttribute("abilities", abilities);
      return "agents/statistics";
    }
}

