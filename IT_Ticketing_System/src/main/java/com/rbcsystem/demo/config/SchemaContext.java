package com.rbcsystem.demo.config;

public class SchemaContext {
    private static final InheritableThreadLocal<String> currentSchema = new InheritableThreadLocal<>();

    public static void setSchema(String schema) {
        currentSchema.set(schema);
        //System.out.println("Schema set to: " + schema); // Add this line for debugging
    }

    public static String getSchema() {
        String schema = currentSchema.get();
       // System.out.println("Schema retrieved: " + schema); // Add this line for debugging
        return schema;
    }

    public static void clear() {
        currentSchema.remove();
    }
}