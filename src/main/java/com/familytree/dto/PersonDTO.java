package com.familytree.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.util.Set;

/**
 * DTO for Person data transfer.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PersonDTO {
    private Long id;
    private String firstName;
    private String middleName;
    private String lastName;
    private String maidenName;
    private LocalDate birthDate;
    private LocalDate deathDate;
    private String gender;
    private String biography;
    private String profileImageUrl;
    private String birthPlace;
    private String deathPlace;
    private String currentLocation;
    private String occupation;
    private String nationality;
    private boolean isPublic;
    private String visibility;
    private Set<Long> childrenIds;
    private Set<Long> parentIds;
    private Set<Long> spouseIds;
}
