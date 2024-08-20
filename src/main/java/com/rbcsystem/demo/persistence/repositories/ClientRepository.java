package com.rbcsystem.demo.persistence.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.rbcsystem.demo.persistence.entities.ClientEntity;
import java.util.List;


//simple repo that extends jpa interface
@Repository
public interface ClientRepository extends JpaRepository<ClientEntity, Long> {

     //returns every department
     @Query(value = "SELECT DISTINCT department FROM users", nativeQuery = true)
     List<String> findAllDepartments();
    
    ClientEntity findByEmail(String email);

    ClientEntity findById(long id);
    
    List<ClientEntity> findByDepartment(String department);

    void deleteById(Long id);

    boolean existsByName(String name);

    ClientEntity findByName(String name);

    //checks if enterprise exists
    @Query("SELECT COUNT(c) > 0 FROM ClientEntity c WHERE c.enterprise = :enterpriseName")
    boolean existsByEnterprise(String enterpriseName);
}
