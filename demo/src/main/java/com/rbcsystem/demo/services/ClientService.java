package com.rbcsystem.demo.services;

import com.rbcsystem.demo.config.SchemaContext;
import com.rbcsystem.demo.persistence.entities.ClientEntity;
import com.rbcsystem.demo.persistence.repositories.ClientRepository;
import com.rbcsystem.demo.persistence.repositories.TicketRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service
@Lazy(false)
public class ClientService {

    @Autowired
    private ClientRepository userRepository;
    
    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final Logger logger = Logger.getLogger(ClientService.class.getName());

    @Transactional
    public boolean authenticateUser(String email, String password) {
        try {
            // Set schema to 'all_users' to fetch user details
            SchemaContext.setSchema("all_users");
    
            // Fetch user from the 'all_users' schema
            ClientEntity user = userRepository.findByEmail(email);
            if (user != null) {
                // Check if the password matches
                if (user.getPassword().equals(password)) {
                    // After successful authentication, set the schema context to the user's specific schema
                    SchemaContext.setSchema(user.getEnterprise()); 
    
                    return true; // Authentication successful
                } else {
                    return false; // Incorrect password
                }
            } else {
                return false; // User not found
            }
        } finally {
            // Clear schema context after the operation is complete
            SchemaContext.clear();
            logger.info("Schema context cleared after authenticateUser");
        }
    }

    @Transactional
    public ClientEntity getWithEmail(String email){
        return userRepository.findByEmail(email);
    }

    @Transactional
    public ClientEntity getWithId(long id){
        return userRepository.findById(id);
    }

    public ClientEntity getWithName(String name){
        return userRepository.findByName(name);
    }

    @Transactional
    public List<String> findAllDepartments() {
        String sql = "SELECT DISTINCT department FROM users";
        return jdbcTemplate.queryForList(sql, String.class);
    }

    @Transactional
    public Map<String, List<String>> getClientsByDepartment() {
        Map<String, List<String>> clientsByDepartment = new HashMap<>();
        
        // Find all departments
        List<String> departments = findAllDepartments();

        // For each department, find clients and map them
        for (String department : departments) {
            List<ClientEntity> clients = userRepository.findByDepartment(department);
            if (clients != null) {
                List<String> clientNames = clients.stream().map(ClientEntity::getName).collect(Collectors.toList());
                clientsByDepartment.put(department, clientNames);
            } else {
                clientsByDepartment.put(department, new ArrayList<>()); // Add an empty list if clients list is null
            }
        }

        return clientsByDepartment;
    }

    @Transactional
    public List<ClientEntity> getClientsForDept(String dept){
        return userRepository.findByDepartment(dept);
    }
    
    @Transactional
    public void promoteClient(long clientId) {
        // Fetch the client by ID
        ClientEntity client = userRepository.findById(clientId);
        if (client != null) {
            client.setPosition("New Position");
            client.setDepartment("New Department");
            userRepository.save(client);
        } else {
            throw new RuntimeException("Client not found with ID: " + clientId);
        }
    }

    @Transactional
    public boolean deleteClientById(Long id){
        if (ticketRepository.existsBySenderId(id)){
            return false;
        }
        userRepository.deleteById(id);
        return true;
    }

    @Transactional
    public void changeImportance(long clientId, int newImportance) {

        ClientEntity client = userRepository.findById(clientId);
        if (client != null) {
            client.setImportance(newImportance);
            userRepository.save(client);
        } else {
            throw new RuntimeException("Client not found with ID: " + clientId);
        }
    }

    public boolean doesEnterpriseExist(String enterpriseName) {
        return userRepository.existsByEnterprise(enterpriseName);
    }

    public boolean existsByName(String name){
        return userRepository.existsByName(name);
    }

    @Transactional
    public void saveUser(ClientEntity user) {
        userRepository.save(user);
    }
}