package com.familytree.controller;

import com.familytree.dto.LineageDTO;
import com.familytree.dto.PersonDTO;
import com.familytree.dto.RelationshipDTO;
import com.familytree.model.Person;
import com.familytree.service.PersonService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST API controller for Person management.
 */
@RestController
@RequestMapping("/api/persons")
@RequiredArgsConstructor
public class PersonController {
    
    private final PersonService personService;
    
    @GetMapping
    public ResponseEntity<List<Person>> getAllPersons() {
        return ResponseEntity.ok(personService.findAll());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Person> getPerson(@PathVariable Long id) {
        return personService.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/search")
    public ResponseEntity<List<Person>> searchPersons(@RequestParam String name) {
        return ResponseEntity.ok(personService.searchByName(name));
    }
    
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'EDITOR')")
    public ResponseEntity<Person> createPerson(@RequestBody PersonDTO personDTO) {
        Person created = personService.createPerson(personDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EDITOR')")
    public ResponseEntity<Person> updatePerson(@PathVariable Long id, 
                                               @RequestBody PersonDTO personDTO) {
        Person updated = personService.updatePerson(id, personDTO);
        return ResponseEntity.ok(updated);
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deletePerson(@PathVariable Long id) {
        personService.deletePerson(id);
        return ResponseEntity.noContent().build();
    }
    
    @PostMapping("/relationships/parent-child")
    @PreAuthorize("hasAnyRole('ADMIN', 'EDITOR')")
    public ResponseEntity<Void> addParentChildRelationship(@RequestBody RelationshipDTO dto) {
        personService.addParentChildRelationship(dto.getPerson1Id(), dto.getPerson2Id());
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/relationships/spouse")
    @PreAuthorize("hasAnyRole('ADMIN', 'EDITOR')")
    public ResponseEntity<Void> addSpouseRelationship(@RequestBody RelationshipDTO dto) {
        personService.addSpouseRelationship(dto.getPerson1Id(), dto.getPerson2Id());
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/{id}/lineage")
    public ResponseEntity<LineageDTO> getLineage(
            @PathVariable Long id,
            @RequestParam(defaultValue = "5") int ancestorDepth,
            @RequestParam(defaultValue = "5") int descendantDepth) {
        LineageDTO lineage = personService.getLineage(id, ancestorDepth, descendantDepth);
        return ResponseEntity.ok(lineage);
    }
    
    @GetMapping("/{id}/siblings")
    public ResponseEntity<List<Person>> getSiblings(@PathVariable Long id) {
        return ResponseEntity.ok(personService.findSiblings(id));
    }
    
    @GetMapping("/relationship-path")
    public ResponseEntity<List<Person>> getRelationshipPath(
            @RequestParam Long person1Id,
            @RequestParam Long person2Id) {
        return ResponseEntity.ok(personService.findRelationshipPath(person1Id, person2Id));
    }
}
