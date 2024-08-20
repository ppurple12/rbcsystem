package com.rbcsystem.demo.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.zaxxer.hikari.HikariDataSource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

@Component
public class SchemaInitializer implements ApplicationListener<ContextRefreshedEvent> {

    
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    @Lazy
    private DataSourceRouting dataSourceRouting;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        initializeSchemas();
    }

    private void initializeSchemas() {
        List<String> schemas = fetchEnterpriseValues();
        Map<Object, Object> targetDataSources = new HashMap<>();

        // Add existing schemas (if any)
        targetDataSources.put("all_users", createDataSource("all_users"));

        // Create data sources for new schemas
        for (String schema : schemas) {
            targetDataSources.put(schema, createDataSource(schema));
        }

        // Update the routing data sources
        dataSourceRouting.setTargetDataSources(targetDataSources);
        dataSourceRouting.afterPropertiesSet(); // Refresh the data source routing
    }

    public List<String> fetchEnterpriseValues() {
        String sql = "SELECT DISTINCT enterprise FROM all_users.users";
        return jdbcTemplate.queryForList(sql, String.class);
    }

    private DataSource createDataSource(String schema) {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl("jdbc:mysql://localhost:3306/" + schema);
        dataSource.setUsername("root");
        dataSource.setPassword("Doorn*bb12");
        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");

        return dataSource;
    }
}