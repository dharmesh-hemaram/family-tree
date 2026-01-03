package com.familytree;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Family Tree Application - Graph-driven genealogy platform.
 * 
 * Features:
 * - Graph-based data modeling using Neo4j
 * - Complex kinship structure support
 * - Historical location tracking
 * - Advanced ancestor discovery
 * - Role-based access control
 * - RESTful API
 * - AI-ready data export
 */
@SpringBootApplication
public class FamilyTreeApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(FamilyTreeApplication.class, args);
    }
}
