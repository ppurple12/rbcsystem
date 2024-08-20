package com.rbcsystem.demo.services;

import java.util.List;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rbcsystem.demo.config.DataSourceConfig;
import com.rbcsystem.demo.config.DataSourceRouting;
import com.zaxxer.hikari.HikariDataSource;

import jakarta.annotation.PostConstruct;


@Service
public class DataSourceService {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private  SchemaCreationService schemaCreationService;
    
    @PostConstruct
    public void initializeDataSources() {
        // Retrieve all schema names from the database
        List<String> schemas = schemaCreationService.getAllDistinctEnterprises();
        
        // Get the DataSourceRouting bean from the application context
        DataSourceRouting dataSourceRouting = (DataSourceRouting) applicationContext.getBean("dataSourceRouting");
        
        // Loop through each schema and add it to the routing data source
        for (String schema : schemas) {
            DataSource dataSource = createDataSource(schema);
            dataSourceRouting.addDataSource(schema, dataSource);
        }
    }

    private DataSource createDataSource(String schema) {
        // Method to create a new DataSource for the given schema
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl("jdbc:mysql://localhost:3306/" + schema);
        dataSource.setUsername("root");
        dataSource.setPassword("Doorn*bb12");
        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");

        return dataSource;
    }

    public void addSchemaDataSource(String schemaName) {
        DataSource dataSource = createDataSource(schemaName);
    }

    @Transactional
    public void removeSchema(String schemaName) {
        
    }
}