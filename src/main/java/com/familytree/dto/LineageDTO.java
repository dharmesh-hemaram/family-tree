package com.familytree.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

/**
 * DTO for lineage information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LineageDTO {
    private Long personId;
    private String personName;
    private List<PersonDTO> ancestors;
    private List<PersonDTO> descendants;
    private int generationsUp;
    private int generationsDown;
}
