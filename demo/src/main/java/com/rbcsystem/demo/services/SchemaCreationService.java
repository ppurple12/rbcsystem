package com.rbcsystem.demo.services;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SchemaCreationService {

   
    @Autowired
    private JdbcTemplate jdbcTemplate;

    // Method to fetch all distinct enterprise names from the 'users' table in the 'all_users' schema
    public List<String> getAllDistinctEnterprises() {
        String sql = "SELECT DISTINCT enterprise FROM all_users.users";

        // Execute the query and return the list of distinct enterprise names
        return jdbcTemplate.queryForList(sql, String.class);
    }

    @Transactional
    public void createSchemaForNewEnterprise(String newSchemaName) {
        // Step 1: Create the new schema
        String createSchemaSQL = "CREATE SCHEMA " + newSchemaName;
        jdbcTemplate.execute(createSchemaSQL);

        // Step 2: Copy table structure for each table in the template schema
        List<String> tableNames = List.of("agents", "qualifications", "role", "rolemap", "statusticket", "task", "ticketmap", "tickets", "updatelog", "users");

        for (String tableName : tableNames) {
            String copyTableStructureSQL = "CREATE TABLE " + newSchemaName + "." + tableName + " LIKE it_support_structure." + tableName;
            jdbcTemplate.execute(copyTableStructureSQL);
        }

        // Step 3: Copy data from the 'role' table only
        String copyRoleDataSQL = "INSERT INTO " + newSchemaName + ".role SELECT * FROM it_support_structure.role";
        jdbcTemplate.execute(copyRoleDataSQL);
    }
} 