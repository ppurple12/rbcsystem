package com.rbcsystem.demo.controller.ticket;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import com.rbcsystem.demo.config.SchemaContext;
import com.rbcsystem.demo.models.TicketForm;

import com.rbcsystem.demo.persistence.entities.ClientEntity;
import com.rbcsystem.demo.persistence.entities.StatusTicket;
import com.rbcsystem.demo.persistence.entities.TicketEntity;
import com.rbcsystem.demo.persistence.entities.enums.TicketCondition;
import com.rbcsystem.demo.persistence.entities.gra.Role;
import com.rbcsystem.demo.persistence.entities.gra.Task;
import com.rbcsystem.demo.services.RoleService;
import com.rbcsystem.demo.services.StatusTicketService;
import com.rbcsystem.demo.services.TaskService;
import com.rbcsystem.demo.services.TicketService;

import jakarta.servlet.http.HttpSession;

import java.util.List;




@Controller
public class AssignmentController {
    
    //important intializations;
    @Autowired
    private StatusTicketService statusTicketService;

   @Autowired
   private RoleService roleService;

   @Autowired
   private TaskService taskService;

   @Autowired
   private TicketService ticketService;
    
    //sets ticket entity for clients using ticket form info
    @PostMapping("/write-ticket")
    public String submitForm(@ModelAttribute TicketForm ticketForm, Model model, HttpSession session) {
        ClientEntity client = (ClientEntity) session.getAttribute("loggedInClient");
        if (client == null || SchemaContext.getSchema() == null ) {
            return "redirect:/login-info";
        }


        // create a Ticket entity from the form data
        TicketEntity ticket = new TicketEntity();
        ticket.setSubject(ticketForm.getSubject());
        ticket.setComments(ticketForm.getIssueDescription());
        ticket.setSender(client);
        ticket.setPriority(client.getImportance()); 
        Task task = taskService.findByCategoryAndSubcategory(ticketForm.getCategory(), ticketForm.getSubcategory());
        ticket.setEstimatedCompletionTime(task.getEstimatedTime()); // sets estimated time to the one assigned to task
        Long taskId = task.getId();
        ticket.setTaskId(taskId);
        ticketService.saveTicket(ticket);  // save the ticket to the database 
        model.addAttribute("submittedTicket", ticket);

        //for manager assign process
        StatusTicket ticketQueue = new StatusTicket();
        ticketQueue.setTicket(ticket);
        ticketQueue.setCondition(TicketCondition.NEW);
        statusTicketService.save(ticketQueue);
        /*
        AUTO ASSIGN PROCESS 

        int[] L = findRoles(ticketForm.getCategory(), ticketForm.getSubcategory());  //gets required roles for the task
        setAgentsToTask(L, ticket); //  implements GRA algo and sets agents
        */
        return "redirect:/dashboard";
        
    }

    //  for managers to create ticket 
    @PostMapping("/manager-ticket")
    public String submitManagerForm(@ModelAttribute TicketForm ticketForm, @RequestParam int importance,   @RequestParam double estimatedCompletionTime, Model model, HttpSession session) {

        ClientEntity client = (ClientEntity) session.getAttribute("loggedInClient");
        if (client == null || SchemaContext.getSchema() == null ) {
            return "redirect:/login-info";
        }

        // Create a Ticket entity from the form data
        TicketEntity ticket = new TicketEntity();
        ticket.setSubject(ticketForm.getSubject());
        ticket.setComments(ticketForm.getIssueDescription());
        ticket.setSender(client);
        ticket.setPriority(importance);
        ticket.setEstimatedCompletionTime(estimatedCompletionTime);
        session.setAttribute("submittedTicket", ticket);
        return "redirect:/role-specification"; //redirects to page where manager can pick roles
    }

    //  for manager to pick roles
    @GetMapping("/role-specification")
    public String roleSpecification(Model model, HttpSession session) {

        TicketEntity submittedTicket = (TicketEntity) session.getAttribute("submittedTicket");

        if (submittedTicket == null  || SchemaContext.getSchema() == null) {
            return "redirect:/"; 
        }

        List<Role> roles = roleService.getAll();

        //  Add the list of roles and the submitted ticket to the model
        model.addAttribute("roles", roles);
        model.addAttribute("submittedTicket", submittedTicket);

        return "tickets/role-specification";
    }
    
    //  ask manager for roles and ammount to create L array
    @PostMapping("/role-specification")
    @ResponseBody
    public ResponseEntity<String> submitRoleAmounts(@RequestBody Integer[] roleAmounts,  HttpSession session) {
        
        TicketEntity submittedTicket = (TicketEntity) session.getAttribute("submittedTicket");
        if (submittedTicket == null || SchemaContext.getSchema() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No ticket information found in the session.");
        }

        //sets role specification array
        session.setAttribute("L", roleAmounts);
        return ResponseEntity.ok("Role amounts submitted successfully.");
    }

    

}
