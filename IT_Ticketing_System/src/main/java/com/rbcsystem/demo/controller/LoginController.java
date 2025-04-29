package com.rbcsystem.demo.controller;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.ui.Model;

import com.rbcsystem.demo.config.SchemaContext;
import com.rbcsystem.demo.persistence.entities.ClientEntity;
import com.rbcsystem.demo.services.ClientService;
import com.rbcsystem.demo.services.TaskService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Controller
public class LoginController {
    
    //inits
    private final ClientService authService;
    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    @Autowired
    private DataSource dataSource;

    @Autowired
    private TaskService taskService;

    public LoginController(ClientService authService) {
        this.authService = authService;
        
    }

    @GetMapping("/login-info")
    public String login() {
        SchemaContext.clear();
        SchemaContext.setSchema("all_users");
        return "functional/login-info"; 
    }

    @PostMapping("/login")
    public String login(
            @RequestParam String email,
            @RequestParam String password,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {

        // Authenticate the user against the all_users schema
        boolean isAuthenticated = authService.authenticateUser(email, password);
        if (isAuthenticated) {
            // Retrieve the user information from all_users schema
            ClientEntity client = authService.getWithEmail(email);
            System.out.println(client.getEnterprise());
            // Determine the enterprise (schema) to use from the user information
            String enterprise = client.getEnterprise();
            
            if (enterprise != null) {
                // Set the schema context for the current thread
                SchemaContext.setSchema(enterprise);
                session.setAttribute("userEnterprise", enterprise);
                taskService.initializeSubcategories();
                logger.info("Schema set to: {}", enterprise);

                // Optionally, verify the schema by logging the active schema
                logger.info("Active schema after setting: {}", SchemaContext.getSchema());

                // Explicitly set the schema for the current connection
                
                try {
                    setSchemaForCurrentConnection(enterprise);
                } catch (SQLException e) {
                    logger.error("Failed to set schema for the current connection", e);
                    redirectAttributes.addAttribute("error", true);
                    return "redirect:/login-info";
                }
                
            }

            // Set the logged-in client in the session
            session.setAttribute("loggedInClient", client);

            // Add client details to the model
            model.addAttribute("client", client);
            logger.info("Access level of user {}: {}", email, client.getAccessLevel());

            // Redirect to the appropriate dashboard
            return "redirect:/dashboard";
        } else {
            logger.warn("Authentication failed for user: {}", email);

            // Redirect back to the login page with an error message
            redirectAttributes.addAttribute("error", true);
            return "redirect:/login-info";
        }
    }
    
    private void setSchemaForCurrentConnection(String schema) throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            conn.createStatement().execute("USE " + schema);
            logger.info("Schema set to {} on current connection", schema);
        }
    }
        

    //logout method
    @GetMapping("/logout")
    public String logout(HttpServletRequest request, HttpServletResponse response) {
        // Get the current session, if it exists
        HttpSession session = request.getSession(false);
        if (session != null) {
            // Invalidate the session
            session.invalidate();
        }

        SchemaContext.clear();

        return "redirect:/"; 
    }


}
