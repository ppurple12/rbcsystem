package com.rbcsystem.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import com.rbcsystem.demo.persistence.entities.ClientEntity;
import jakarta.servlet.http.HttpSession;

@Controller
public class ProfileController {

    //gets client and returns to according page
    @GetMapping("/dashboard/profile")
    public String getProfileDetail( HttpSession session) {
        ClientEntity client = (ClientEntity) session.getAttribute("loggedInClient");
        if (client != null) {
                return "dashboard/profile";
            
        }
        return "redirect:/"; // Redirect to home
    }

    //gets client and returns to according page
    @GetMapping("/website-issues")
    public String getWebsiteIssue( HttpSession session) {
        ClientEntity client = (ClientEntity) session.getAttribute("loggedInClient");
        if (client != null) {
                return "functional/website-issues";
            
        }
        return "redirect:/"; // Redirect to home
    }

    //gets client and models its information to according page
    @GetMapping("/information")
    public String getInformation(HttpSession session, Model model) {
        ClientEntity client = (ClientEntity) session.getAttribute("loggedInClient");
        if (client != null) {
            model.addAttribute("user", client);
            return "dashboard/information";
            
    
        }
        return "redirect:/";
    }
    //DO NOT FORGET TO GET INFO FROM FEEBACK HERE
}
