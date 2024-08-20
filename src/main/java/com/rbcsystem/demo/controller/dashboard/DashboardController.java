package com.rbcsystem.demo.controller.dashboard;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import com.rbcsystem.demo.persistence.entities.ClientEntity;
import com.rbcsystem.demo.services.AgentService;
import com.rbcsystem.demo.services.ClientService;

import jakarta.servlet.http.HttpSession;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import com.rbcsystem.demo.persistence.entities.enums.AccessLevel;
import com.rbcsystem.demo.config.SchemaContext;
import com.rbcsystem.demo.persistence.entities.AgentEntity;

@Controller
public class DashboardController {

    @Autowired 
    private AgentService agentService;

    @Autowired
    private ClientService clientService;

    @GetMapping("/dashboard")
    public String getDashboard(Model model, HttpSession session) {
        // Get user information from session
        ClientEntity client = (ClientEntity) session.getAttribute("loggedInClient");;
        
        
        // Check if client is null
        if (client == null || SchemaContext.getSchema() == null ) {
            return "functional/login-info";
        }
    
        // Pass user information to the dashboard template
        model.addAttribute("client", client);
        // Render different dashboard templates based on the user's access level
        if (client.getAccessLevel() == AccessLevel.USER) {
           
            return "redirect:/dashboard/default";
        }
        else {
            
            clientService.saveUser(client);
            AgentEntity agent = agentService.getWithUser(client);
            session.setAttribute("loggedInAgent", agent);
            if (client.getAccessLevel() == AccessLevel.IT) {
                return "redirect:/dashboard/it";
            } 
            else{
                return "redirect:/dashboard/manager";
            }
        }

        
    }
}