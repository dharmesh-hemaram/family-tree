package com.familytree.service;

import com.familytree.dto.LineageDTO;
import com.familytree.dto.PersonDTO;
import com.familytree.model.Person;
import com.familytree.repository.PersonRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PersonService.
 */
@ExtendWith(MockitoExtension.class)
class PersonServiceTest {
    
    @Mock
    private PersonRepository personRepository;
    
    @InjectMocks
    private PersonService personService;
    
    private Person testPerson;
    private PersonDTO testPersonDTO;
    
    @BeforeEach
    void setUp() {
        testPerson = Person.builder()
            .id(1L)
            .firstName("John")
            .lastName("Doe")
            .birthDate(LocalDate.of(1950, 1, 15))
            .gender("MALE")
            .isPublic(true)
            .visibility("PUBLIC")
            .children(new HashSet<>())
            .parents(new HashSet<>())
            .spouses(new HashSet<>())
            .build();
        
        testPersonDTO = PersonDTO.builder()
            .firstName("Jane")
            .lastName("Smith")
            .birthDate(LocalDate.of(1975, 6, 20))
            .gender("FEMALE")
            .isPublic(true)
            .visibility("PUBLIC")
            .build();
    }
    
    @Test
    void testFindById_Success() {
        when(personRepository.findById(1L)).thenReturn(Optional.of(testPerson));
        
        Optional<Person> result = personService.findById(1L);
        
        assertTrue(result.isPresent());
        assertEquals("John", result.get().getFirstName());
        verify(personRepository, times(1)).findById(1L);
    }
    
    @Test
    void testFindById_NotFound() {
        when(personRepository.findById(999L)).thenReturn(Optional.empty());
        
        Optional<Person> result = personService.findById(999L);
        
        assertFalse(result.isPresent());
    }
    
    @Test
    void testCreatePerson_Success() {
        Person savedPerson = Person.builder()
            .id(2L)
            .firstName("Jane")
            .lastName("Smith")
            .build();
        
        when(personRepository.save(any(Person.class))).thenReturn(savedPerson);
        
        Person result = personService.createPerson(testPersonDTO);
        
        assertNotNull(result);
        assertEquals(2L, result.getId());
        assertEquals("Jane", result.getFirstName());
        verify(personRepository, times(1)).save(any(Person.class));
    }
    
    @Test
    void testSearchByName_ReturnsResults() {
        List<Person> persons = Arrays.asList(testPerson);
        when(personRepository.searchByName("John")).thenReturn(persons);
        
        List<Person> results = personService.searchByName("John");
        
        assertEquals(1, results.size());
        assertEquals("John", results.get(0).getFirstName());
    }
    
    @Test
    void testAddParentChildRelationship_Success() {
        Person parent = Person.builder()
            .id(1L)
            .firstName("Parent")
            .lastName("Doe")
            .children(new HashSet<>())
            .parents(new HashSet<>())
            .spouses(new HashSet<>())
            .build();
        
        Person child = Person.builder()
            .id(2L)
            .firstName("Child")
            .lastName("Doe")
            .children(new HashSet<>())
            .parents(new HashSet<>())
            .spouses(new HashSet<>())
            .build();
        
        when(personRepository.findById(1L)).thenReturn(Optional.of(parent));
        when(personRepository.findById(2L)).thenReturn(Optional.of(child));
        when(personRepository.save(any(Person.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        personService.addParentChildRelationship(1L, 2L);
        
        assertTrue(parent.getChildren().contains(child));
        assertTrue(child.getParents().contains(parent));
        verify(personRepository, times(2)).save(any(Person.class));
    }
    
    @Test
    void testGetLineage_Success() {
        Person ancestor = Person.builder()
            .id(0L)
            .firstName("Grandpa")
            .lastName("Doe")
            .children(new HashSet<>())
            .parents(new HashSet<>())
            .spouses(new HashSet<>())
            .build();
        
        Person descendant = Person.builder()
            .id(2L)
            .firstName("Junior")
            .lastName("Doe")
            .children(new HashSet<>())
            .parents(new HashSet<>())
            .spouses(new HashSet<>())
            .build();
        
        when(personRepository.findById(1L)).thenReturn(Optional.of(testPerson));
        when(personRepository.findAncestors(1L, 5)).thenReturn(Arrays.asList(ancestor));
        when(personRepository.findDescendants(1L, 5)).thenReturn(Arrays.asList(descendant));
        
        LineageDTO lineage = personService.getLineage(1L, 5, 5);
        
        assertNotNull(lineage);
        assertEquals(1L, lineage.getPersonId());
        assertEquals(1, lineage.getAncestors().size());
        assertEquals(1, lineage.getDescendants().size());
        assertEquals("Grandpa", lineage.getAncestors().get(0).getFirstName());
    }
    
    @Test
    void testFindSiblings_Success() {
        Person sibling = Person.builder()
            .id(3L)
            .firstName("Jack")
            .lastName("Doe")
            .build();
        
        when(personRepository.findSiblings(1L)).thenReturn(Arrays.asList(sibling));
        
        List<Person> siblings = personService.findSiblings(1L);
        
        assertEquals(1, siblings.size());
        assertEquals("Jack", siblings.get(0).getFirstName());
    }
}
