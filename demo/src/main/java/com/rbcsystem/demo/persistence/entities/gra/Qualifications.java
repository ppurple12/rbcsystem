package com.rbcsystem.demo.persistence.entities.gra;

import com.rbcsystem.demo.persistence.entities.AgentEntity;

import jakarta.persistence.*;

@Entity
@Table(name = "qualifications")
public class Qualifications {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "agent_id", referencedColumnName = "id", nullable = false)
    private AgentEntity agent;

    @Column(name = "it_support_ability")
    private Double itSupportAbility;

    @Column(name = "database_administration_ability")
    private Double databaseAdministrationAbility;

    @Column(name = "network_specialist_ability")
    private Double networkSpecialistAbility;

    @Column(name = "systems_administration_ability")
    private Double systemsAdministrationAbility;

    @Column(name = "software_specialist_ability")
    private Double softwareSpecialistAbility;

    @Column(name = "hardware_specialist_ability")
    private Double hardwareSpecialistAbility;

    @Column(name = "hardware_support_ability")
    private Double hardwareSupportAbility;

    @Column(name = "supervisor_ability")
    private Double supervisorAbility;

    // Default constructor (required by JPA)
    public Qualifications() {}

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public AgentEntity getAgent() {
        return agent;
    }

    public void setAgent(AgentEntity agent) {
        this.agent = agent;
    }

    public Double getItSupportAbility() {
        return itSupportAbility;
    }

    public void setItSupportAbility(Double itSupportAbility) {
        this.itSupportAbility = itSupportAbility;
    }

    public Double getDatabaseAdministrationAbility() {
        return databaseAdministrationAbility;
    }

    public void setDatabaseAdministrationAbility(Double databaseAdministrationAbility) {
        this.databaseAdministrationAbility = databaseAdministrationAbility;
    }

    public Double getNetworkSpecialistAbility() {
        return networkSpecialistAbility;
    }

    public void setNetworkSpecialistAbility(Double networkSpecialistAbility) {
        this.networkSpecialistAbility = networkSpecialistAbility;
    }

    public Double getSystemsAdministrationAbility() {
        return systemsAdministrationAbility;
    }

    public void setSystemsAdministrationAbility(Double systemsAdministrationAbility) {
        this.systemsAdministrationAbility = systemsAdministrationAbility;
    }

    public Double getSoftwareSpecialistAbility() {
        return softwareSpecialistAbility;
    }

    public void setSoftwareSpecialistAbility(Double softwareSpecialistAbility) {
        this.softwareSpecialistAbility = softwareSpecialistAbility;
    }

    public Double getHardwareSpecialistAbility() {
        return hardwareSpecialistAbility;
    }

    public void setHardwareSpecialistAbility(Double hardwareSpecialistAbility) {
        this.hardwareSpecialistAbility = hardwareSpecialistAbility;
    }

    public Double getHardwareSupportAbility() {
        return hardwareSupportAbility;
    }

    public void setHardwareSupportAbility(Double hardwareSupportAbility) {
        this.hardwareSupportAbility = hardwareSupportAbility;
    }

    public Double getSupervisorAbility() {
        return supervisorAbility;
    }

    public void setSupervisorAbility(Double supervisorAbility) {
        this.supervisorAbility = supervisorAbility;
    }
}