package com.rbcsystem.demo.controller.dashboard;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.rbcsystem.demo.persistence.entities.AgentEntity;
import com.rbcsystem.demo.persistence.entities.StatusTicket;
import com.rbcsystem.demo.persistence.entities.enums.TicketCondition;
import com.rbcsystem.demo.services.AgentService;
import com.rbcsystem.demo.services.QualificationService;
import com.rbcsystem.demo.services.RoleService;
import com.rbcsystem.demo.services.StatusTicketService;

import jakarta.servlet.http.HttpSession;

@Controller
public class ManagerDashboardController {

  
   @Autowired
   private QualificationService qualificationService;

   @Autowired
   private StatusTicketService statusTicketService;

   @Autowired
   private AgentService agentService;

   @Autowired
   private RoleService roleService;

   
   //finds all agents to display them and have access
    @GetMapping("/dashboard/manager")
    public String getAllAgents(Model model) {
       boolean ticketQueue = statusTicketService.existsByCondition(TicketCondition.NEW);
        Iterable<AgentEntity> agents = agentService.getAll(); 
        boolean areThereOverdueTickets = statusTicketService.existsByCondition(TicketCondition.OVERDUE);
        boolean areThereUnassignedTickets = statusTicketService.existsByCondition(TicketCondition.UNASSIGNED);
        model.addAttribute("agents", agents);
        model.addAttribute("areThereOverdueTickets", areThereOverdueTickets);
        model.addAttribute("areThereUnassignedTickets", areThereUnassignedTickets);
        model.addAttribute("ticketQueue", ticketQueue);
        return "dashboard/manager";

    }

    // gets agent ids, displays them and will allow to update them
    @GetMapping("/manage-stats/{id}")
    public String manageStatistics(@PathVariable Long id, Model model, HttpSession session){
        AgentEntity loggedInAgent = agentService.getWithId(id);
        double[] abilities = qualificationService.getAbilitiesByAgentId(id);
        List<String> roles = roleService.getAllRoleNames();
        model.addAttribute("roles", roles);
        model.addAttribute("loggedInAgent", loggedInAgent);
        model.addAttribute("abilities", abilities);
        return "agents/manage-stats";
    }

    //  post method to update the abilities of the agent
    @PostMapping("/update-abilities/{agentId}")
    public ResponseEntity<Map<String, String>> updateAbilities(@PathVariable("agentId") long agentId, @RequestBody Map<String, double[]> requestBody) {
         //  post method to update the abilities of the agent
        double[] updatedAbilities = requestBody.get("updatedAbilities");
        double[] skills = qualificationService.getAbilitiesByAgentId(agentId);
        double[] realUpdatedAbilities = new double[skills.length]; 
        int j = 0;
        for (int i = 0; i < realUpdatedAbilities.length; i++){
            if (skills[i] > 0){
                realUpdatedAbilities[i] = updatedAbilities[j];
                j++;
            }
        }
        // Process updatedAbilities through qualificationservice 
        qualificationService.updateQualificationsByAgentId(agentId, realUpdatedAbilities);

        Map<String, String> response = new HashMap<>();
        response.put("redirectUrl", "/dashboard");
        
        return ResponseEntity.ok(response);
    }

    //  increments all roles that arent 0
    @PostMapping("/increment-all-stats/{agentId}")
    public String incrementAllStats(@PathVariable("agentId") Long agentId) {
        double[] incrementedAbilities = qualificationService.getAbilitiesByAgentId(agentId);
        for(int i = 0; i < incrementedAbilities.length; i++){
            if (incrementedAbilities[i] > 0){ 
                incrementedAbilities[i] += 0.05; //goes up by 0.05 at each
            }
        }

        qualificationService.updateQualificationsByAgentId(agentId, incrementedAbilities);
        return "redirect:/manage-stats/" + agentId;
    }

    //  returns all overdue tickets
    @GetMapping("/overdue-tickets")
    public String getOverdueTickets(Model model) {
        List<StatusTicket> overdueTickets = statusTicketService.findAllByCondition(TicketCondition.OVERDUE);
        model.addAttribute("overdueTickets", overdueTickets);
        return "tickets/overdue-tickets";
    }

    //returns all unassigned tickets
    @GetMapping("/unassigned-tickets")
    public String getUnassignedTickets(Model model) {
        List<StatusTicket> unassignedTickets = statusTicketService.findAllByCondition(TicketCondition.UNASSIGNED);
        model.addAttribute("unassignedTickets", unassignedTickets);
        return "tickets/unassigned-tickets";
    }
}