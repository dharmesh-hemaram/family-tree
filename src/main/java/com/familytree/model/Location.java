package com.familytree.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;

/**
 * Represents a geographical location with historical context.
 */
@Node
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Location {
    
    @Id
    @GeneratedValue
    private Long id;
    
    private String name;
    private String city;
    private String state;
    private String country;
    
    private Double latitude;
    private Double longitude;
    
    private String historicalName; // Historical name if different
    private Integer historicalYear; // Year of the historical name
    
    private String description;
}
