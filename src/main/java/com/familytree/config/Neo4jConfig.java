package com.familytree.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;

/**
 * Neo4j database configuration.
 */
@Configuration
@EnableNeo4jRepositories(basePackages = "com.familytree.repository")
public class Neo4jConfig {
    // Configuration is handled by Spring Boot auto-configuration
    // Additional custom configuration can be added here if needed
}
