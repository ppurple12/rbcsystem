package com.rbcsystem.demo.config;



import com.zaxxer.hikari.HikariDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class StarterDataConfig {

    @Bean
    public DataSource starterDataSource() {
        String dbUrl = System.getenv("DB_URL");
        String dbUsername = System.getenv("DB_USERNAME");
        String dbPassword = System.getenv("DB_PASSWORD");
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl("jdbc:mysql://localhost:3306/all_users?verifyServerCertificate=true&useSSL=true&requireSSL=true");
        dataSource.setUsername("Evanw");
        dataSource.setPassword(DB_PASSWORD);
        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");

        return dataSource;
    }
}