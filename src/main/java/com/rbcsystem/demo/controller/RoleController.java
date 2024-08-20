package com.rbcsystem.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.rbcsystem.demo.persistence.entities.AgentEntity;
import com.rbcsystem.demo.persistence.entities.gra.Role;
import com.rbcsystem.demo.services.AgentService;
import com.rbcsystem.demo.services.QualificationService;
import com.rbcsystem.demo.services.RoleService;

import jakarta.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;

@Controller
public class RoleController {

    @Autowired
    private RoleService roleService;

    @Autowired
    private AgentService agentService;

    @Autowired
    private QualificationService qualificationService;

    @GetMapping("/modify-roles/{id}")
    public String modifyRoles(@PathVariable Long id, Model model) {
        List<Role> allRoles = new ArrayList<>(roleService.getAll());
        AgentEntity agent = agentService.getWithId(id);
        
        List<Role> roles = new ArrayList<>(allRoles);
        double[] quals = qualificationService.getAbilitiesByAgentId(id);
        
        for (int i = 0; i < allRoles.size(); i++) {
            if (quals[i] <= 0) {
                roles.remove(allRoles.get(i));
                
            }
        }

        allRoles.removeAll(roles); // Remove active roles from allRoles list to allow adding of roles

        model.addAttribute("agent", agent);
        model.addAttribute("roles", roles);
        model.addAttribute("allRoles", allRoles);
        
        return "agents/modify-roles";
    }

    // adds a certain role to an agent
    @PostMapping("/addRole")
    public String addRole(@RequestParam("roleId") Long roleId, @RequestParam("ability") int ability, @RequestParam("agentId") Long agentId) {
        List<Role> allRoles = new ArrayList<>(roleService.getAll());
        int index = allRoles.indexOf(roleService.getById(roleId));
        double[] abilities = qualificationService.getAbilitiesByAgentId(agentId);
        abilities[index] = (ability/10.0);
        qualificationService.updateQualificationsByAgentId(agentId, abilities);

        return "redirect:/modify-roles/" + agentId;
    }

    //removes a role
    @PostMapping("/removeRole")
    public String removeRole(@RequestParam("roleId") Long roleId, @RequestParam("agentId") Long agentId) {
        List<Role> allRoles = new ArrayList<>(roleService.getAll());
        int index = allRoles.indexOf(roleService.getById(roleId));
        double[] abilities = qualificationService.getAbilitiesByAgentId(agentId);
        abilities[index] = 0.0;
        qualificationService.updateQualificationsByAgentId(agentId, abilities);

        return "redirect:/modify-roles/" + agentId;
    }
    
}
