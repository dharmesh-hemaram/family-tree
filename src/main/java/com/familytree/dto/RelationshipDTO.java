package com.familytree.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * DTO for relationship information between two people.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RelationshipDTO {
    private Long person1Id;
    private Long person2Id;
    private String relationshipType; // PARENT, CHILD, SPOUSE, SIBLING, etc.
    private String description;
}
