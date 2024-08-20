package com.rbcsystem.demo.services;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rbcsystem.demo.persistence.entities.enums.AccessLevel;
import com.rbcsystem.demo.persistence.entities.AgentEntity;
import com.rbcsystem.demo.persistence.entities.ClientEntity;
import com.rbcsystem.demo.persistence.entities.TicketEntity;
import com.rbcsystem.demo.persistence.entities.TicketMap;
import com.rbcsystem.demo.persistence.repositories.AgentRepository;

import com.rbcsystem.demo.persistence.repositories.ClientRepository;
import com.rbcsystem.demo.persistence.repositories.TicketMapRepository;



//simple repo class
@Service
@Lazy(false)
public class AgentService {

   

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private TicketMapRepository ticketMapRepository;

    @Autowired 
    private AgentRepository agentRepository;

    //  returns total amount of roles
    public int getRoleAmount(){
        return agentRepository.countTotalRoles();
    }

    //gets lists of all tickets assigned to specific agent
    public List<TicketEntity> getTicketsByAgentId(Long agentId) {
        // Fetch the agent along with its associated ticket maps eagerly
        AgentEntity agent = agentRepository.findById(agentId).orElse(null);
        if (agent != null) {
            // Initialize the ticket maps to ensure they're loaded eagerly
            agent.getTickets().size();
            return agent.getTickets().stream()
                                        .map(TicketMap::getTicket)
                                        .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    //  gets agent with ticket Id
     public List<AgentEntity> getAgentsByTicketId(long ticketId) {
        return agentRepository.findByTicketMapsTicket_Id(ticketId);
    }  

    //gets agent with the client entity
    public AgentEntity getWithUser(ClientEntity client){
        return agentRepository.findByUser(client);
    }

    //  gets agent with id
    public AgentEntity getWithId(Long id){
        return agentRepository.getById(id);
    }

    //  gets all agents
    @Transactional
    public List<AgentEntity> getAll(){
        return agentRepository.findAll();
    }
    
    //  get with ticket maps agent
    public List<AgentEntity> getWithTicketMapsTicket(TicketEntity ticket){
        return agentRepository.findByTicketMapsTicket(ticket);
    }
    // deletes agent with their respective ID
    @Transactional
    public boolean deleteAgentById(Long agentId) throws Exception {
        // Check if agent exists
        if (ticketMapRepository.existsByAgentId(agentId)) {
            return false;
        }
        agentRepository.deleteById(agentId);
        return true;
    }

    // updates access level from client to agent
    @Transactional
    public boolean updateAccessLevel(Long clientId, AccessLevel newAccessLevel) {
        Optional<ClientEntity> optionalClient = clientRepository.findById(clientId);
        
        if (optionalClient.isPresent()) {
            ClientEntity client = optionalClient.get();
            client.setAccessLevel(newAccessLevel);
            clientRepository.save(client);
            return true;
        } else {
            return false; 
        }
    }

    //  saves agent to database
    @Transactional
    public void saveAgent(AgentEntity agent){
        agentRepository.save(agent);
    }
    
        
}
