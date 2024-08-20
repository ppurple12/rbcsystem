package com.rbcsystem.demo.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rbcsystem.demo.config.SchemaContext;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

public class SchemaContextFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger(SchemaContextFilter.class);

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String schema = getSchemaFromSession(httpRequest);
        
        if (schema != null) {
            SchemaContext.setSchema(schema);
           //log.info("Schema set to: {}", schema);
        } else {
            // Do not set a default schema if not required; ensure no unexpected operations
            //log.info("No schema found in session, no schema context set");
        }

        try {
            chain.doFilter(request, response);
        } finally {
            // Clear schema context after processing the request to avoid leakage
            SchemaContext.clear();
            //log.info("Schema context cleared");
        }
    }

    private String getSchemaFromSession(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            return (String) session.getAttribute("userEnterprise");
        }
        return null;
    }
}