package com.rbcsystem.demo.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.rbcsystem.demo.config.SchemaContext;
import com.rbcsystem.demo.persistence.entities.ClientEntity;
import com.rbcsystem.demo.services.ClientService;
import com.rbcsystem.demo.services.SchemaCreationService;

import jakarta.servlet.http.HttpSession;

import com.rbcsystem.demo.persistence.entities.enums.AccessLevel;


//signup controller
@Controller
public class SignupController {

    private final ClientService userService;

    @Autowired
    private SchemaCreationService schemaCreationService;

    

    //uses user services
    public SignupController(ClientService userService) {
        this.userService = userService;
    }

    //maps to signup page
    @GetMapping("/signup")
    public String signup() {
        return "functional/signup";
    }

    // grabs signup info
    @PostMapping("/signup")
    public String processUserForm(@RequestParam String username,
                               @RequestParam String email,
                               @RequestParam String password,
                               RedirectAttributes redirectAttributes) {
        //retrieves enterprise-related data from redirect attributes
        String department = (String) redirectAttributes.getAttribute("enterpriseDepartment");
        String position = (String) redirectAttributes.getAttribute("enterprisePosition");

        //creates a new client instance with user info and enterprise info
        ClientEntity user = new ClientEntity(username, email, password, department, position, 0, AccessLevel.USER);

        //saves the client to the database using the userService
        userService.saveUser(user);
        
        //directs to the correct page according to department
       
        // Redirect to the appropriate dashboard
        return "redirect:/login";
    }

    //mapping func
    @GetMapping("/signup-enterpriseinfo")
    public String userinfo() {
        SchemaContext.clear();
        SchemaContext.setSchema("all_users");
        return "functional/signup-enterpriseinfo";
    }

    @PostMapping("/signup-enterpriseinfo")
    public String processEnterpriseForm(@RequestParam String enterprise,
                                        @RequestParam String department,
                                        @RequestParam String position,
                                        RedirectAttributes redirectAttributes) {
        String processedEnterpriseName = enterprise.toLowerCase().replaceAll("\\s+", "");
        boolean exists = userService.doesEnterpriseExist(processedEnterpriseName);
        
        if (exists) {
            // Add the enterprise info to redirect attributes
            redirectAttributes.addFlashAttribute("enterprise", processedEnterpriseName);
            redirectAttributes.addFlashAttribute("department", department);
            redirectAttributes.addFlashAttribute("position", position);
        } 
        
        else {
            redirectAttributes.addFlashAttribute("errorMessage", "Enterprise does not exist in the database.");
            return "redirect:/signup-enterpriseinfo"; // Redirect back to the signup form
        }

        return "redirect:/signup";
    }

    @GetMapping("/enroll-enterprise")
    public String showEnrollmentForm() {
        return ("functional/enroll-enterprises");
    }

    @PostMapping("/enroll-enterprise")
    public String handleEnterpriseInfo(
        @RequestParam("enterprise") String enterpriseName,
        RedirectAttributes redirectAttributes, HttpSession session) {
        String processedEnterpriseName = enterpriseName.toLowerCase().replaceAll("\\s+", "");
        // Check if the enterprise name already exists
        if (userService.doesEnterpriseExist(processedEnterpriseName)) {
            redirectAttributes.addFlashAttribute("errorMessage", "The enterprise name already exists. Please use another name.");
            return "redirect:/enroll-enterprise";
        }

        // If enterprise does not exist, proceed to enroll new enterprise
        schemaCreationService.createSchemaForNewEnterprise(processedEnterpriseName);
        session.setAttribute("enterprise", processedEnterpriseName);
        redirectAttributes.addFlashAttribute("enterprise", processedEnterpriseName);
        return "redirect:/admin-signup";
    }

    
    @GetMapping("/admin-signup")
    public String makeSignupForm() {
        return ("functional/admin-signup");
    }

    @PostMapping("/admin-signup")
    public String handleAdminSignup(
        @RequestParam("username") String username,
        @RequestParam("branchNumber") String branchNumber,
        @RequestParam("department") String department,
        @RequestParam("position") String position,
        @RequestParam("email") String email,
        @RequestParam("password") String password,
        @RequestParam("confirm") String confirmPassword,
       HttpSession session,
        RedirectAttributes redirectAttributes) {

        // Validate that passwords match
        if (!password.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Passwords do not match.");
            return "redirect:/admin-signup";
        }
        String enterprise = (String) session.getAttribute("enterprise");
        //make new user 
        ClientEntity client = new ClientEntity();
        client.setName(username);
        client.setImportance(5);
        client.setEmail(email);
        client.setPassword(confirmPassword);
        client.setDepartment(department);
        client.setPosition(position);
        client.setEnterprise(enterprise);
        client.setAccessLevel(AccessLevel.ADMIN);
        
        // add to default db and client's db
        SchemaContext.setSchema("all_users");
        userService.saveUser(client);
        SchemaContext.clear();
        SchemaContext.setSchema(enterprise);
        userService.saveUser(client);
        SchemaContext.clear();
        return "redirect:/login";
    }
}