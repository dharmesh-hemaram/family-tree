package com.familytree.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents a user of the system with role-based access control.
 */
@Node
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    
    @Id
    @GeneratedValue
    private Long id;
    
    private String username;
    private String email;
    private String passwordHash;
    
    private String firstName;
    private String lastName;
    
    @Builder.Default
    private Set<String> roles = new HashSet<>(); // ADMIN, EDITOR, VIEWER, FAMILY_MEMBER
    
    private boolean enabled;
    private LocalDateTime createdAt;
    private LocalDateTime lastLoginAt;
    
    // Link to their person in the tree (if applicable)
    private Long personId;
}
