package com.rbcsystem.demo.persistence.repositories.gra;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.rbcsystem.demo.persistence.entities.TicketEntity;
import com.rbcsystem.demo.persistence.entities.gra.Role;




//simple repo that extends jpa interface
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    
     // Method to count the total number of roles
    @Query("SELECT COUNT(r) FROM Role r")
    int countTotalRoles();

    //  returns a list of strings of all role names
    @Query("SELECT r.role_name FROM Role r")
    List<String> findAllRoleNames();

    List<Role> findAll();

    Role getRoleById(Long id);

    ArrayList<Role> findByTicketMapsTicket(TicketEntity ticket);

    Optional<Role> findById(Long id);
    
}