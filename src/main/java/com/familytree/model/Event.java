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

/**
 * Represents a significant event in a person's life or family history.
 */
@Node
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Event {
    
    @Id
    @GeneratedValue
    private Long id;
    
    private String eventType; // BIRTH, DEATH, MARRIAGE, DIVORCE, MIGRATION, etc.
    private String title;
    private String description;
    
    private LocalDate eventDate;
    private String location;
    
    private String source; // Source of information
    private String sourceUrl;
    
    @Relationship(type = "PARTICIPATED_IN", direction = Relationship.Direction.INCOMING)
    private Person person;
}
