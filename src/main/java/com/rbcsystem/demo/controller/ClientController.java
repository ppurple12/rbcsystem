package com.rbcsystem.demo.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.rbcsystem.demo.persistence.entities.AgentEntity;
import com.rbcsystem.demo.persistence.entities.ClientEntity;
import com.rbcsystem.demo.persistence.entities.TicketEntity;
import com.rbcsystem.demo.persistence.entities.TicketMap;
import com.rbcsystem.demo.persistence.entities.gra.Role;
import com.rbcsystem.demo.services.AgentService;
import com.rbcsystem.demo.services.ClientService;
import com.rbcsystem.demo.services.RoleService;
import com.rbcsystem.demo.services.TicketMapService;
import com.rbcsystem.demo.services.TicketService;

@Controller
public class ClientController {
   
    @Autowired
    private AgentService agentService;

    @Autowired
    private ClientService clientService;

    @Autowired
    private RoleService roleService;

    @Autowired
    private TicketService ticketService;

    @Autowired
    private TicketMapService ticketMapService;

    //maps specific client id to a page
    @GetMapping("/client-details/{id}")
    public String getClientDetail(@PathVariable Long id, Model model) {
        
        ClientEntity client =  clientService.getWithId(id);
        if (client != null) {
            model.addAttribute("client", client);
            AgentEntity agent = agentService.getWithUser(client);
            if (agent != null) { // if an agent map to agent page

                return "redirect:/agents/" + agent.getId();
            }
             else { //  else go to client page
                return "tickets/client-view"; 
            }
            
        }
        return "redirect:/";
    }

    // allows to chnage importance of clients
    @PostMapping("/client-view")
    public String updateClient(@RequestParam Long clientId, @RequestParam int importance, Model model) {

        ClientEntity client =   clientService.getWithId(clientId);   
        if (client.getImportance() != importance){
            client.setImportance(importance); 
            clientService.saveUser(client);
        }
        return "redirect:/enterprise-view"; 
    }

    //  gets roles and client to create an agent
    @GetMapping("/make-agent/{clientId}")
    public String makeAgent(@PathVariable Long clientId, Model model) {

        ClientEntity client = clientService.getWithId(clientId);
        List<Role> roles = roleService.getAll();
          
         model.addAttribute("client", client);
         model.addAttribute("roles", roles);
         return "agents/make-agent"; 
    }

    @GetMapping("/all-tickets-per-client/{id}")
    public String getAllTicketsForAgent(@PathVariable Long id, Model model) {
        ClientEntity client = clientService.getWithId(id);
        if (client != null) {
            // gets tickets
            List<TicketEntity> tickets = ticketService.getTicketsBySender(client);
            tickets = ticketService.sortTickets(tickets);
            
            // gets ticket id and specific ticket map for agent-ticket combo
          
            model.addAttribute("client", client);
            model.addAttribute("activeTickets", tickets);
            return "tickets/all-tickets-per-client";
        }
        return "redirect:/";
    }

    @PostMapping("/delete-client")
    public ResponseEntity<String> deleteClient(@RequestParam("clientId") Long clientId) {
        
        if (clientService.deleteClientById(clientId)) {
            return ResponseEntity.status(HttpStatus.OK).body("Client deleted successfully.");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to delete client. Tickets are still assigned to this client.");
        }      
    }
}
