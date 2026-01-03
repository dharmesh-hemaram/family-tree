package com.familytree.service;

import com.familytree.dto.LineageDTO;
import com.familytree.dto.PersonDTO;
import com.familytree.model.Person;
import com.familytree.repository.PersonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service for managing persons and genealogical operations.
 */
@Service
@RequiredArgsConstructor
public class PersonService {
    
    private final PersonRepository personRepository;
    
    @Transactional(readOnly = true)
    public Optional<Person> findById(Long id) {
        return personRepository.findById(id);
    }
    
    @Transactional(readOnly = true)
    public List<Person> findAll() {
        return personRepository.findAll();
    }
    
    @Transactional(readOnly = true)
    public List<Person> searchByName(String searchTerm) {
        return personRepository.searchByName(searchTerm);
    }
    
    @Transactional
    public Person createPerson(PersonDTO dto) {
        Person person = Person.builder()
            .firstName(dto.getFirstName())
            .middleName(dto.getMiddleName())
            .lastName(dto.getLastName())
            .maidenName(dto.getMaidenName())
            .birthDate(dto.getBirthDate())
            .deathDate(dto.getDeathDate())
            .gender(dto.getGender())
            .biography(dto.getBiography())
            .profileImageUrl(dto.getProfileImageUrl())
            .birthPlace(dto.getBirthPlace())
            .deathPlace(dto.getDeathPlace())
            .currentLocation(dto.getCurrentLocation())
            .occupation(dto.getOccupation())
            .nationality(dto.getNationality())
            .isPublic(dto.isPublic())
            .visibility(dto.getVisibility())
            .build();
        
        return personRepository.save(person);
    }
    
    @Transactional
    public Person updatePerson(Long id, PersonDTO dto) {
        Person person = personRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Person not found"));
        
        person.setFirstName(dto.getFirstName());
        person.setMiddleName(dto.getMiddleName());
        person.setLastName(dto.getLastName());
        person.setMaidenName(dto.getMaidenName());
        person.setBirthDate(dto.getBirthDate());
        person.setDeathDate(dto.getDeathDate());
        person.setGender(dto.getGender());
        person.setBiography(dto.getBiography());
        person.setProfileImageUrl(dto.getProfileImageUrl());
        person.setBirthPlace(dto.getBirthPlace());
        person.setDeathPlace(dto.getDeathPlace());
        person.setCurrentLocation(dto.getCurrentLocation());
        person.setOccupation(dto.getOccupation());
        person.setNationality(dto.getNationality());
        person.setPublic(dto.isPublic());
        person.setVisibility(dto.getVisibility());
        
        return personRepository.save(person);
    }
    
    @Transactional
    public void deletePerson(Long id) {
        personRepository.deleteById(id);
    }
    
    @Transactional
    public void addParentChildRelationship(Long parentId, Long childId) {
        Person parent = personRepository.findById(parentId)
            .orElseThrow(() -> new RuntimeException("Parent not found"));
        Person child = personRepository.findById(childId)
            .orElseThrow(() -> new RuntimeException("Child not found"));
        
        parent.getChildren().add(child);
        child.getParents().add(parent);
        
        personRepository.save(parent);
        personRepository.save(child);
    }
    
    @Transactional
    public void addSpouseRelationship(Long person1Id, Long person2Id) {
        Person person1 = personRepository.findById(person1Id)
            .orElseThrow(() -> new RuntimeException("Person 1 not found"));
        Person person2 = personRepository.findById(person2Id)
            .orElseThrow(() -> new RuntimeException("Person 2 not found"));
        
        person1.getSpouses().add(person2);
        person2.getSpouses().add(person1);
        
        personRepository.save(person1);
        personRepository.save(person2);
    }
    
    @Transactional(readOnly = true)
    public LineageDTO getLineage(Long personId, int ancestorDepth, int descendantDepth) {
        Person person = personRepository.findById(personId)
            .orElseThrow(() -> new RuntimeException("Person not found"));
        
        List<Person> ancestors = personRepository.findAncestors(personId, ancestorDepth);
        List<Person> descendants = personRepository.findDescendants(personId, descendantDepth);
        
        return LineageDTO.builder()
            .personId(personId)
            .personName(person.getFullName())
            .ancestors(ancestors.stream().map(this::toDTO).collect(Collectors.toList()))
            .descendants(descendants.stream().map(this::toDTO).collect(Collectors.toList()))
            .generationsUp(ancestorDepth)
            .generationsDown(descendantDepth)
            .build();
    }
    
    @Transactional(readOnly = true)
    public List<Person> findSiblings(Long personId) {
        return personRepository.findSiblings(personId);
    }
    
    @Transactional(readOnly = true)
    public List<Person> findRelationshipPath(Long person1Id, Long person2Id) {
        return personRepository.findRelationshipPath(person1Id, person2Id);
    }
    
    private PersonDTO toDTO(Person person) {
        return PersonDTO.builder()
            .id(person.getId())
            .firstName(person.getFirstName())
            .middleName(person.getMiddleName())
            .lastName(person.getLastName())
            .maidenName(person.getMaidenName())
            .birthDate(person.getBirthDate())
            .deathDate(person.getDeathDate())
            .gender(person.getGender())
            .biography(person.getBiography())
            .profileImageUrl(person.getProfileImageUrl())
            .birthPlace(person.getBirthPlace())
            .deathPlace(person.getDeathPlace())
            .currentLocation(person.getCurrentLocation())
            .occupation(person.getOccupation())
            .nationality(person.getNationality())
            .isPublic(person.isPublic())
            .visibility(person.getVisibility())
            .childrenIds(person.getChildren().stream().map(Person::getId).collect(Collectors.toSet()))
            .parentIds(person.getParents().stream().map(Person::getId).collect(Collectors.toSet()))
            .spouseIds(person.getSpouses().stream().map(Person::getId).collect(Collectors.toSet()))
            .build();
    }
}
