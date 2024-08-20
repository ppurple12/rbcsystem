package com.rbcsystem.demo.config;

import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;



public class DataSourceRouting extends AbstractRoutingDataSource {

    private Map<Object, Object> targetDataSources = new HashMap<>();

    @Override
    protected Object determineCurrentLookupKey() {
        String schema = SchemaContext.getSchema();
       // System.out.println("Determining current lookup key: " + schema); // Debug log
        return schema;
    }
    public void addDataSource(String schemaName, DataSource dataSource) {
        targetDataSources.put(schemaName, dataSource);
        setTargetDataSources(targetDataSources);
        afterPropertiesSet();
    }

    public void removeDataSource(String schemaName) {
        targetDataSources.remove(schemaName);
        setTargetDataSources(targetDataSources);
        afterPropertiesSet();
    }
}