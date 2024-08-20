package com.rbcsystem.demo.config;

import com.zaxxer.hikari.HikariDataSource;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



@Configuration
public class DataSourceConfig {

    

    @Bean
    @Primary
    public DataSourceRouting dataSourceRouting() {
        DataSourceRouting dataSourceRouting = new DataSourceRouting();
        DataSource defaultDataSource = createDataSource("all_users");

        Map<Object, Object> targetDataSources = new HashMap<>();
        targetDataSources.put("all_users", defaultDataSource);

        dataSourceRouting.setTargetDataSources(targetDataSources);
        dataSourceRouting.setDefaultTargetDataSource(defaultDataSource);
        dataSourceRouting.afterPropertiesSet(); // Initialize with default data source

        return dataSourceRouting;
    }

    @Bean
    public DataSource createDataSource(String schema) {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl("jdbc:mysql://localhost:3306/" + schema); // Adjust as needed
        dataSource.setUsername("root");
        dataSource.setPassword("Doorn*bb12");
        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");

        return dataSource;
    }
}
    