package com.rbcsystem.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import com.rbcsystem.demo.persistence.entities.ClientEntity;
import com.rbcsystem.demo.persistence.repositories.ClientRepository;
import com.rbcsystem.demo.services.ClientService;

@Component
public class StartupData implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(StartupData.class);
    private final ClientRepository clientRepository;
    private final ClientService authService;

    public StartupData(ClientRepository clientRepository, ClientService authService) {
        this.clientRepository = clientRepository;
        this.authService = authService;
    }

    @Override
    public void run(String... args) throws Exception {
        logger.info("Inserting startup data");

        // Check if the client already exists in the database
        if (clientRepository.findByEmail("poo@poo.com") == null) {
            // Client does not exist, so create and save it
            ClientEntity client = new ClientEntity();
            client.setName("John Doe");
            client.setEmail("poo@poo.com");
            client.setPassword("wow");
            client.setDepartment("Sales");
            client.setPosition("Manager");
            
            clientRepository.save(client);
            
            logger.info("Startup data inserted");
        } else {
            logger.info("Startup data already exists in the database");
        }

        boolean isAuthenticated = authService.authenticateUser("john.doe@example.com", "password");
        logger.info("GUESS WHAT{}", isAuthenticated);
    }
}
