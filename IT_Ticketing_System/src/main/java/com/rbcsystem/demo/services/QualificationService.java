package com.rbcsystem.demo.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rbcsystem.demo.persistence.entities.AgentEntity;
import com.rbcsystem.demo.persistence.entities.gra.Qualifications;
import com.rbcsystem.demo.persistence.repositories.gra.QualificationRepository;
import java.util.List;

@Service
public class QualificationService {
    
    @Autowired
    private QualificationRepository qualificationRepository;
    

    // Returns an array of abilities for all roles of an agent
    
    public double[] getAbilitiesByAgentId(long agentId) {
        // Static sizing for demonstration; adjust as needed
        double[] abilities = new double[8];
        int i = 0;
    
        abilities[i++] = qualificationRepository.findItSupportAbilityByAgentId(agentId) != null ? qualificationRepository.findItSupportAbilityByAgentId(agentId) : 0.0;
        abilities[i++] = qualificationRepository.findDatabaseAdministrationAbilityByAgentId(agentId) != null ? qualificationRepository.findDatabaseAdministrationAbilityByAgentId(agentId) : 0.0;
        abilities[i++] = qualificationRepository.findNetworkSpecialistAbilityByAgentId(agentId) != null ? qualificationRepository.findNetworkSpecialistAbilityByAgentId(agentId) : 0.0;
        abilities[i++] = qualificationRepository.findSystemsAdministrationAbilityByAgentId(agentId) != null ? qualificationRepository.findSystemsAdministrationAbilityByAgentId(agentId) : 0.0;
        abilities[i++] = qualificationRepository.findSoftwareSpecialistAbilityByAgentId(agentId) != null ? qualificationRepository.findSoftwareSpecialistAbilityByAgentId(agentId) : 0.0;
        abilities[i++] = qualificationRepository.findHardwareSpecialistAbilityByAgentId(agentId) != null ? qualificationRepository.findHardwareSpecialistAbilityByAgentId(agentId) : 0.0;
        abilities[i++] = qualificationRepository.findHardwareSupportAbilityByAgentId(agentId) != null ? qualificationRepository.findHardwareSupportAbilityByAgentId(agentId) : 0.0;
        abilities[i++] = qualificationRepository.findSupervisorAbilityByAgentId(agentId) != null ? qualificationRepository.findSupervisorAbilityByAgentId(agentId) : 0.0;
    
        return abilities;
    }
    

    // Saves qualifications for an agent
    @Transactional
    public void saveQualification(AgentEntity agent, List<Integer> abilities) {
        Qualifications qual = new Qualifications();
        qual.setAgent(agent);

        // Assuming abilities list contains values in the order of roles
        qual.setItSupportAbility(abilities.get(0)/10.0);
        qual.setDatabaseAdministrationAbility(abilities.get(1)/10.0);
        qual.setNetworkSpecialistAbility(abilities.get(2)/10.0);
        qual.setSystemsAdministrationAbility(abilities.get(3)/10.0);
        qual.setSoftwareSpecialistAbility(abilities.get(4)/10.0);
        qual.setHardwareSpecialistAbility(abilities.get(5)/10.0);
        qual.setHardwareSupportAbility(abilities.get(5)/10.0);
        qual.setSupervisorAbility(abilities.get(6)/10.0);

        qualificationRepository.save(qual);
    }

    // Updates qualifications for an agent
    @Transactional
    public void updateQualificationsByAgentId(long agentId, double[] updatedAbilities) {
        int i = 0;
        qualificationRepository.updateItSupportAbilityByAgentId(agentId, updatedAbilities[i++]);
        qualificationRepository.updateDatabaseAdministrationAbilityByAgentId(agentId, updatedAbilities[i++]);
        qualificationRepository.updateNetworkSpecialistAbilityByAgentId(agentId, updatedAbilities[i++]);
        qualificationRepository.updateSystemsAdministrationAbilityByAgentId(agentId, updatedAbilities[i++]);
        qualificationRepository.updateSoftwareSpecialistAbilityByAgentId(agentId, updatedAbilities[i++]);
        qualificationRepository.updateHardwareSpecialistAbilityByAgentId(agentId, updatedAbilities[i++]);
        qualificationRepository.updateHardwareSupportAbilityByAgentId(agentId, updatedAbilities[i++]);
        qualificationRepository.updateSupervisorAbilityByAgentId(agentId, updatedAbilities[i]);
    }
}