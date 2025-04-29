package com.rbcsystem.demo.persistence.repositories;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.rbcsystem.demo.persistence.entities.AgentEntity;
import java.util.ArrayList;
import com.rbcsystem.demo.persistence.entities.ClientEntity;
import com.rbcsystem.demo.persistence.entities.TicketEntity;
import java.util.List;





//simple repo that extends jpa interface
@Repository
public interface AgentRepository extends JpaRepository<AgentEntity, Long> {

    // returns amount of roles
    @Query("SELECT COUNT(r) FROM AgentEntity r")
    int countTotalRoles();

    //finds agent with ticket by searching through ticket maps
    @EntityGraph(attributePaths = "ticketMaps")

    AgentEntity findById(long id);

    AgentEntity findByUser(ClientEntity user);

    List<AgentEntity> findAll();

    ArrayList<AgentEntity> findByTicketMapsTicket(TicketEntity ticket);

    List<AgentEntity> findByTicketMapsTicket_Id(long ticketId);
}
