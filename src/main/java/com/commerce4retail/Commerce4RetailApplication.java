package com.commerce4retail;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main entry point for the Commerce4Retail backend application.
 * Placed at the root of the {@code com.commerce4retail} package so that
 * Spring Boot's component-scan, JPA repository-scan, and entity-scan all
 * cover every sub-module (product, cart, etc.) automatically.
 */
@SpringBootApplication
public class Commerce4RetailApplication {

    public static void main(String[] args) {
        SpringApplication.run(Commerce4RetailApplication.class, args);
    }
}
