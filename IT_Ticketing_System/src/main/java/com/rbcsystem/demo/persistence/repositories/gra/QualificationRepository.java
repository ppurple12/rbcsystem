package com.rbcsystem.demo.persistence.repositories.gra;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.rbcsystem.demo.persistence.entities.gra.Qualifications;


import org.springframework.transaction.annotation.Transactional;

@Repository
public interface QualificationRepository extends JpaRepository<Qualifications, Long> {

    // Queries to find abilities by agent ID

    @Query("SELECT q.itSupportAbility FROM Qualifications q WHERE q.agent.id = :agentId")
    Double findItSupportAbilityByAgentId(@Param("agentId") long agentId);

    @Query("SELECT q.databaseAdministrationAbility FROM Qualifications q WHERE q.agent.id = :agentId")
    Double findDatabaseAdministrationAbilityByAgentId(@Param("agentId") long agentId);

    @Query("SELECT q.networkSpecialistAbility FROM Qualifications q WHERE q.agent.id = :agentId")
    Double findNetworkSpecialistAbilityByAgentId(@Param("agentId") long agentId);

    @Query("SELECT q.systemsAdministrationAbility FROM Qualifications q WHERE q.agent.id = :agentId")
    Double findSystemsAdministrationAbilityByAgentId(@Param("agentId") long agentId);

    @Query("SELECT q.softwareSpecialistAbility FROM Qualifications q WHERE q.agent.id = :agentId")
    Double findSoftwareSpecialistAbilityByAgentId(@Param("agentId") long agentId);

    @Query("SELECT q.hardwareSpecialistAbility FROM Qualifications q WHERE q.agent.id = :agentId")
    Double findHardwareSpecialistAbilityByAgentId(@Param("agentId") long agentId);

    @Query("SELECT q.hardwareSupportAbility FROM Qualifications q WHERE q.agent.id = :agentId")
    Double findHardwareSupportAbilityByAgentId(@Param("agentId") long agentId);

    @Query("SELECT q.supervisorAbility FROM Qualifications q WHERE q.agent.id = :agentId")
    Double findSupervisorAbilityByAgentId(@Param("agentId") long agentId);


    // Updater functions

    @Transactional
    @Modifying
    @Query("UPDATE Qualifications q SET q.itSupportAbility = :itSupportAbility WHERE q.agent.id = :agentId")
    void updateItSupportAbilityByAgentId(@Param("agentId") long agentId, @Param("itSupportAbility") double itSupportAbility);

    @Transactional
    @Modifying
    @Query("UPDATE Qualifications q SET q.databaseAdministrationAbility = :databaseAdministrationAbility WHERE q.agent.id = :agentId")
    void updateDatabaseAdministrationAbilityByAgentId(@Param("agentId") long agentId, @Param("databaseAdministrationAbility") double databaseAdministrationAbility);

    @Transactional
    @Modifying
    @Query("UPDATE Qualifications q SET q.networkSpecialistAbility = :networkSpecialistAbility WHERE q.agent.id = :agentId")
    void updateNetworkSpecialistAbilityByAgentId(@Param("agentId") long agentId, @Param("networkSpecialistAbility") double networkSpecialistAbility);

    @Transactional
    @Modifying
    @Query("UPDATE Qualifications q SET q.systemsAdministrationAbility = :systemsAdministrationAbility WHERE q.agent.id = :agentId")
    void updateSystemsAdministrationAbilityByAgentId(@Param("agentId") long agentId, @Param("systemsAdministrationAbility") double systemsAdministrationAbility);

    @Transactional
    @Modifying
    @Query("UPDATE Qualifications q SET q.softwareSpecialistAbility = :softwareSpecialistAbility WHERE q.agent.id = :agentId")
    void updateSoftwareSpecialistAbilityByAgentId(@Param("agentId") long agentId, @Param("softwareSpecialistAbility") double softwareSpecialistAbility);

    @Transactional
    @Modifying
    @Query("UPDATE Qualifications q SET q.hardwareSpecialistAbility = :hardwareSpecialistAbility WHERE q.agent.id = :agentId")
    void updateHardwareSpecialistAbilityByAgentId(@Param("agentId") long agentId, @Param("hardwareSpecialistAbility") double hardwareSpecialistAbility);

    @Transactional
    @Modifying
    @Query("UPDATE Qualifications q SET q.hardwareSupportAbility = :hardwareSupportAbility WHERE q.agent.id = :agentId")
    void updateHardwareSupportAbilityByAgentId(@Param("agentId") long agentId, @Param("hardwareSupportAbility") double hardwareSupportAbility);

    @Transactional
    @Modifying
    @Query("UPDATE Qualifications q SET q.supervisorAbility = :supervisorAbility WHERE q.agent.id = :agentId")
    void updateSupervisorAbilityByAgentId(@Param("agentId") long agentId, @Param("supervisorAbility") double supervisorAbility);
}