package com.familytree.controller;

import com.familytree.dto.PersonDTO;
import com.familytree.model.Person;
import com.familytree.service.PersonService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for PersonController.
 */
@WebMvcTest(PersonController.class)
@AutoConfigureMockMvc
class PersonControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private PersonService personService;
    
    private Person testPerson;
    
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
    }
    
    @Test
    void testGetAllPersons_Success() throws Exception {
        when(personService.findAll()).thenReturn(Arrays.asList(testPerson));
        
        mockMvc.perform(get("/api/persons"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].firstName").value("John"))
            .andExpect(jsonPath("$[0].lastName").value("Doe"));
    }
    
    @Test
    void testGetPerson_Found() throws Exception {
        when(personService.findById(1L)).thenReturn(Optional.of(testPerson));
        
        mockMvc.perform(get("/api/persons/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.firstName").value("John"))
            .andExpect(jsonPath("$.lastName").value("Doe"));
    }
    
    @Test
    void testGetPerson_NotFound() throws Exception {
        when(personService.findById(999L)).thenReturn(Optional.empty());
        
        mockMvc.perform(get("/api/persons/999"))
            .andExpect(status().isNotFound());
    }
    
    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreatePerson_Success() throws Exception {
        when(personService.createPerson(any(PersonDTO.class))).thenReturn(testPerson);
        
        mockMvc.perform(post("/api/persons")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"firstName\":\"John\",\"lastName\":\"Doe\",\"isPublic\":true,\"visibility\":\"PUBLIC\"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.firstName").value("John"));
    }
    
    @Test
    void testSearchPersons_Success() throws Exception {
        when(personService.searchByName("John")).thenReturn(Arrays.asList(testPerson));
        
        mockMvc.perform(get("/api/persons/search")
                .param("name", "John"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].firstName").value("John"));
    }
}
