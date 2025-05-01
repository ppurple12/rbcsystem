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
        String dbUrl = System.getenv("DB_URL");
        String dbUsername = System.getenv("DB_USERNAME");
        String dbPassword = System.getenv("DB_PASSWORD");
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl("jdbc:mysql://localhost:3306/" + schema+"?useSSL=false&ssl-mode=REQUIRED");
        dataSource.setUsername("root");
        dataSource.setPassword(DB_PASSWORD);
        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");

        return dataSource;
    }
}
    