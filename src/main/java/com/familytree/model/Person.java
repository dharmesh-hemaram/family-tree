package com.familytree.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents a person in the family tree.
 * This is a graph node that connects to other persons through relationships.
 */
@Node
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Person {
    
    @Id
    @GeneratedValue
    private Long id;
    
    private String firstName;
    private String middleName;
    private String lastName;
    private String maidenName;
    
    private LocalDate birthDate;
    private LocalDate deathDate;
    
    private String gender; // MALE, FEMALE, OTHER, UNKNOWN
    
    private String biography;
    private String profileImageUrl;
    
    // Historical location information
    private String birthPlace;
    private String deathPlace;
    private String currentLocation;
    
    // Additional metadata
    private String occupation;
    private String nationality;
    
    // Privacy and access control
    private boolean isPublic;
    private String visibility; // PUBLIC, FAMILY, PRIVATE
    
    @Relationship(type = "PARENT_OF", direction = Relationship.Direction.OUTGOING)
    @Builder.Default
    private Set<Person> children = new HashSet<>();
    
    @Relationship(type = "PARENT_OF", direction = Relationship.Direction.INCOMING)
    @Builder.Default
    private Set<Person> parents = new HashSet<>();
    
    @Relationship(type = "SPOUSE_OF")
    @Builder.Default
    private Set<Person> spouses = new HashSet<>();
    
    /**
     * Get full name of the person
     */
    public String getFullName() {
        StringBuilder name = new StringBuilder();
        if (firstName != null) name.append(firstName);
        if (middleName != null) name.append(" ").append(middleName);
        if (lastName != null) name.append(" ").append(lastName);
        return name.toString().trim();
    }
    
    /**
     * Check if person is alive
     */
    public boolean isAlive() {
        return deathDate == null;
    }
}
