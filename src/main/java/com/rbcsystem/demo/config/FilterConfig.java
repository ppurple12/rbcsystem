package com.rbcsystem.demo.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.rbcsystem.demo.filter.SchemaContextFilter;

@Configuration
public class FilterConfig {

    @Bean
    public FilterRegistrationBean<SchemaContextFilter> schemaContextFilter() {
        FilterRegistrationBean<SchemaContextFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new SchemaContextFilter());
        registrationBean.addUrlPatterns("/*"); // Adjust URL patterns as needed
        registrationBean.setOrder(1); // Ensure correct filter order if needed
        return registrationBean;
    }
}