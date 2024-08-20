package com.rbcsystem.demo.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.rbcsystem.demo.persistence.entities.ClientEntity;
import com.rbcsystem.demo.persistence.repositories.ClientRepository;

import org.springframework.security.config.Customizer;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    //intializations and constructor
    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

    private final ClientRepository clientRepository;

    public SecurityConfig(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    //class to authenticate user details
    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            ClientEntity client = clientRepository.findByEmail(username);
            if (client != null) {
                logger.info("ClientEntity created successfully: {}", client.getEmail());
                return org.springframework.security.core.userdetails.User.builder()
                    .username(client.getEmail())
                    .password(client.getPassword())
                    .roles(client.getAccessLevel().name())
                    .build();
            } else {
                throw new UsernameNotFoundException("User not found");
            }
        };
    }

    //class that ensures users are authenticated to access website
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher(  "/css/**", "/js/**", "/images/**")
            .csrf(Customizer.withDefaults())
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/login-info","/", "/signup", "/signup-enterpriseinfo").permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/it/**").hasRole("IT")
                .requestMatchers("/user/**").hasRole("USER")
                .anyRequest().authenticated()
            )
            .httpBasic().disable()
            .formLogin(login -> login
                .loginPage("/login-info") 
                .loginProcessingUrl("/login-info")
                .defaultSuccessUrl("/dashboard/default") // Corrected spelling
                .permitAll()
            ).logout()
            .logoutUrl("/logout")
            .logoutSuccessUrl("/login?logout")
            .permitAll();;
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}