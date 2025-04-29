package com.rbcsystem.demo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.rbcsystem.demo.persistence.entities.ClientEntity;
import com.rbcsystem.demo.services.ClientService;


@Controller
public class EnterpriseController {

    @Autowired
    private ClientService clientService;

    // finds all departments to display on page
    @GetMapping("/enterprise-view")
    public String viewClientsByDepartment(Model model) {
        
        List<String> departments = clientService.findAllDepartments();
        model.addAttribute("departments", departments);
        return "dashboard/enterprise-view";
    }

    //creates dictionary with departments as keys and the clients as values
   @GetMapping("/clients-by-department")
    public ResponseEntity<List<ClientEntity>> getClientsForDepartment(@RequestParam String department) {
        List<ClientEntity> clients = clientService.getClientsForDept(department);
        return new ResponseEntity<>(clients, HttpStatus.OK);
    }
}
