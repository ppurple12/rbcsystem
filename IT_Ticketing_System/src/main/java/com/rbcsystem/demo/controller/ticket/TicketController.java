package com.rbcsystem.demo.controller.ticket;

import com.rbcsystem.demo.config.SchemaContext;
import com.rbcsystem.demo.models.TicketForm;
import com.rbcsystem.demo.persistence.entities.enums.AccessLevel;
import com.rbcsystem.demo.persistence.entities.enums.TicketCondition;
import com.rbcsystem.demo.persistence.entities.AgentEntity;
import com.rbcsystem.demo.persistence.entities.ClientEntity;
import com.rbcsystem.demo.persistence.entities.TicketEntity;
import com.rbcsystem.demo.persistence.entities.TicketMap;
import com.rbcsystem.demo.persistence.entities.enums.TicketStatus;
import com.rbcsystem.demo.persistence.entities.UpdateLog;
import com.rbcsystem.demo.persistence.entities.gra.Role;
import com.rbcsystem.demo.persistence.entities.gra.Task;
import com.rbcsystem.demo.services.AgentService;
import com.rbcsystem.demo.services.RoleService;
import com.rbcsystem.demo.services.StatusTicketService;
import com.rbcsystem.demo.services.TaskService;
import com.rbcsystem.demo.services.TicketMapService;
import com.rbcsystem.demo.services.TicketService;
import com.rbcsystem.demo.services.UpdateLogService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;





@Controller
public class TicketController {

  
    private static final Logger logger = LoggerFactory.getLogger(TicketController.class);

    @Autowired
    private RoleService roleService;

   @Autowired
   private TaskService taskService; 

    @Autowired 
    private AgentService agentService;

    @Autowired 
    private TicketService ticketService;

    @Autowired
    private UpdateLogService updateLogService;

    @Autowired
    private TicketMapService ticketMapService;

    @Autowired
    private StatusTicketService statusTicketService;


    //  returns subcategories
    @Transactional
    @GetMapping("/subcategories")
    @ResponseBody
    public List<String> getSubcategories(@RequestParam String category) {
        Map<String, String[]> subcategoriesMap = taskService.getSubcategoriesMap();
        return subcategoriesMap.containsKey(category) ? Arrays.asList(subcategoriesMap.get(category)) : new ArrayList<>();
    }

    //  gets ticket form and sets categories
    @GetMapping("/write-ticket")
    public String showForm(Model model) {
        Map<String, String[]> subcategoriesMap = taskService.getSubcategoriesMap();
        model.addAttribute("ticket", new TicketForm());
        model.addAttribute("categories", subcategoriesMap.keySet().toArray(new String[0]));
        model.addAttribute("subcategoriesMap", subcategoriesMap);
        return "tickets/write-ticket";
    }

    
    //  gets ticket form and sets categories
    @GetMapping("/manager-ticket")
    public String showManagerForm(Model model) {
        
        model.addAttribute("ticket", new TicketForm()); 
        model.addAttribute("categories", taskService.getSubcategoriesMap().keySet().toArray(new String[0]));
        model.addAttribute("subcategoriesMap", taskService.getSubcategoriesMap());
        return "tickets/manager-ticket";
    }

    //finds ticket in repo and redirects to according page
    @GetMapping("/tickets/{id}")
    public String getTicketDetail(@PathVariable Long id, Model model, HttpSession session) {
        
        TicketEntity ticket = ticketService.getWithId(id);
        ClientEntity client = (ClientEntity) session.getAttribute("loggedInClient");
        if (client == null || SchemaContext.getSchema() == null ) {
            return "functional/login-info";
        }
        if (ticket != null) {
            
            //  gets all necessary component
            AgentEntity agent = agentService.getWithUser(client);
            List<UpdateLog> updateLogs = updateLogService.getUpdateLogsByTicketId(id);
            boolean overdue = statusTicketService.existsByTicketIdAndCondition(id, TicketCondition.OVERDUE);
            boolean unassigned = statusTicketService.existsByTicketIdAndCondition(id, TicketCondition.UNASSIGNED);
            model.addAttribute("agent", agent);
            model.addAttribute("overdue", overdue);
            model.addAttribute("updateLogs", updateLogs);
            model.addAttribute("ticket", ticket);
            

            //checks access level and sends to according ticket view page
            if (client.getAccessLevel() == AccessLevel.ADMIN) {
                
                session.setAttribute("ticket", ticket);
                return "/tickets/detail-manager";
            }
            if (client.getAccessLevel() == AccessLevel.IT) {
                //  changes ticket status accordingly if it member
               ticketMapService.updateStatus(ticketMapService.getTicketMap(ticket, agent).getId(), TicketStatus.INITIATED); 
                return "/tickets/detail-it";
            }
            else{
                return "/tickets/detail-user";
            }
        }
        return "redirect:/";
    }
    
    //to remove completed tickets
    @PostMapping("/complete-ticket")
    public String completeTicket(@RequestParam("ticketId") Long ticketId) {
        logger.info("Deleting ticket with ID: {}", ticketId);
        try {
            TicketEntity ticket = ticketService.getWithId(ticketId);
            
            ticketService.deleteTicket(ticket);
            updateLogService.deleteUpdateLogsByTicketId(ticketId);
            logger.info("Ticket with ID {} deleted successfully", ticketId);
        } catch (Exception e) {
            logger.error("Error occurred while deleting ticket with ID: {}", ticketId, e);
            // Handle the exception or rethrow it if necessary
        }
        return "redirect:/dashboard";
    }

    // gets mapping to ticket in order to change it
    @GetMapping("tickets/adjust-tickets")
    public String getModifyTickets(Model model) {
        model.addAttribute("categories", taskService.getSubcategoriesMap().keySet().toArray(new String[0]));
        model.addAttribute("subcategoriesMap", taskService.getSubcategoriesMap());
        return "tickets/adjust-tickets";
    }

    @PostMapping("/process-subcategory")
    public String processSubcategory(@RequestParam String category, @RequestParam String subcategory, HttpSession session) {
        // Process the category and subcategory strings as needed
        Task task = taskService.findByCategoryAndSubcategory(category, subcategory);
        
           
        Long taskId = task.getId();
        
        // Redirect with the task ID as a parameter
        session.setAttribute("taskId", taskId);
        session.setAttribute("rolesExceedAgents", false);
        return "redirect:/change-roles";
    
    }

    //  gets necessary info to adjust the roles for specific task
    @GetMapping("/change-roles")
    public String getChangeRoles(Model model, HttpSession session) {
        Long taskId = (Long) session.getAttribute("taskId");
        
        if (taskId == null) {
            return "redirect:/error";
        }
        Task task = taskService.getById(taskId);
        Map<Long, Integer> roleEngine = roleService.getRolesAndAmountsForTask(taskId);
        int agentAmt = agentService.getRoleAmount();
        List<Role> roles = roleService.getAll(); 
        boolean rolesExceedAgents = (boolean) session.getAttribute("rolesExceedAgents"); // true if theres an issue with amount of agents, false otherwise
        // Add the list of roles and the submitted ticket to the model
        model.addAttribute("agentAmt", agentAmt);
        model.addAttribute("roles", roles);
        model.addAttribute("task", task);
        model.addAttribute("roleEngine", roleEngine);
        model.addAttribute("rolesExceedAgents", rolesExceedAgents);
        return "tickets/change-roles"; 
    }

    // changes role mapping with results from form
    @PostMapping("/change-roles")
    @ResponseBody
    public String handleRoleSpecification(@RequestBody Map<String, Object> formData, HttpSession session) {
        Long taskId = (Long) session.getAttribute("taskId");
        List<Integer> roleAmounts = (List<Integer>) formData.get("roleAmounts");
        Object rawEstimatedTime =  formData.get("estimatedTime");
        double estimatedTime = ((Number) rawEstimatedTime).doubleValue();
        taskService.updateEstimatedTime(taskId, estimatedTime);
        roleService.updateRoles(taskId, roleAmounts);
    
        return "tickets/adjust-tickets";
    }
    
    // models all tickets that are "NEW" to specific agent
    @GetMapping("new-tickets")
    public String getNewTickets(HttpSession session, Model model) {

        AgentEntity loggedInAgent = (AgentEntity) session.getAttribute("loggedInAgent");
        if (loggedInAgent == null || SchemaContext.getSchema() == null) {
            return "redirect:/login-info";
        }
        
        List<TicketEntity> tickets = ticketService.getNewTickets(loggedInAgent.getId()); //  gets all new tickets for agent
        tickets = ticketService.sortTickets(tickets);
        
        Map<Long, TicketMap> ticketMaps = new HashMap<>();
        for (TicketEntity ticket : tickets) {
            TicketMap ticketMap = ticketMapService.getTicketMap(ticket, loggedInAgent);
            ticketMaps.put(ticket.getId(), ticketMap);
        }
      
        model.addAttribute("loggedInAgent", loggedInAgent);
        model.addAttribute("activeTickets", tickets);
        model.addAttribute("ticketMaps", ticketMaps);
        return "tickets/new-tickets";
    }
    
}
