package com.familytree.config;

import com.familytree.dto.PersonDTO;
import com.familytree.model.User;
import com.familytree.service.PersonService;
import com.familytree.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.time.LocalDate;
import java.util.Set;

/**
 * Initialize sample data for development and testing.
 * Only runs when 'dev' profile is active.
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataInitializer {
    
    @Bean
    @Profile("dev")
    public CommandLineRunner initializeData(PersonService personService, UserService userService) {
        return args -> {
            log.info("Initializing sample data...");
            
            try {
                // Create admin user
                userService.createUser(
                    "admin",
                    "admin@familytree.com",
                    "admin123",
                    Set.of("ADMIN", "EDITOR", "VIEWER")
                );
                log.info("Created admin user");
                
                // Create sample family tree
                // Generation 1 - Grandparents
                var grandpa = personService.createPerson(PersonDTO.builder()
                    .firstName("Robert")
                    .lastName("Johnson")
                    .birthDate(LocalDate.of(1930, 3, 15))
                    .deathDate(LocalDate.of(2010, 8, 22))
                    .gender("MALE")
                    .birthPlace("Boston, MA, USA")
                    .occupation("Engineer")
                    .isPublic(true)
                    .visibility("PUBLIC")
                    .build());
                
                var grandma = personService.createPerson(PersonDTO.builder()
                    .firstName("Mary")
                    .maidenName("Smith")
                    .lastName("Johnson")
                    .birthDate(LocalDate.of(1932, 7, 8))
                    .deathDate(LocalDate.of(2015, 12, 5))
                    .gender("FEMALE")
                    .birthPlace("New York, NY, USA")
                    .occupation("Teacher")
                    .isPublic(true)
                    .visibility("PUBLIC")
                    .build());
                
                // Add spouse relationship
                personService.addSpouseRelationship(grandpa.getId(), grandma.getId());
                
                // Generation 2 - Parents
                var father = personService.createPerson(PersonDTO.builder()
                    .firstName("John")
                    .lastName("Johnson")
                    .birthDate(LocalDate.of(1955, 5, 20))
                    .gender("MALE")
                    .birthPlace("Boston, MA, USA")
                    .occupation("Doctor")
                    .isPublic(true)
                    .visibility("PUBLIC")
                    .build());
                
                var mother = personService.createPerson(PersonDTO.builder()
                    .firstName("Sarah")
                    .maidenName("Williams")
                    .lastName("Johnson")
                    .birthDate(LocalDate.of(1957, 9, 12))
                    .gender("FEMALE")
                    .birthPlace("Chicago, IL, USA")
                    .occupation("Lawyer")
                    .isPublic(true)
                    .visibility("PUBLIC")
                    .build());
                
                // Add parent-child relationships
                personService.addParentChildRelationship(grandpa.getId(), father.getId());
                personService.addParentChildRelationship(grandma.getId(), father.getId());
                personService.addSpouseRelationship(father.getId(), mother.getId());
                
                // Generation 3 - Children
                var child1 = personService.createPerson(PersonDTO.builder()
                    .firstName("Emily")
                    .lastName("Johnson")
                    .birthDate(LocalDate.of(1985, 2, 14))
                    .gender("FEMALE")
                    .birthPlace("San Francisco, CA, USA")
                    .occupation("Software Engineer")
                    .isPublic(true)
                    .visibility("PUBLIC")
                    .build());
                
                var child2 = personService.createPerson(PersonDTO.builder()
                    .firstName("Michael")
                    .lastName("Johnson")
                    .birthDate(LocalDate.of(1987, 11, 3))
                    .gender("MALE")
                    .birthPlace("San Francisco, CA, USA")
                    .occupation("Architect")
                    .isPublic(true)
                    .visibility("PUBLIC")
                    .build());
                
                // Add parent-child relationships
                personService.addParentChildRelationship(father.getId(), child1.getId());
                personService.addParentChildRelationship(mother.getId(), child1.getId());
                personService.addParentChildRelationship(father.getId(), child2.getId());
                personService.addParentChildRelationship(mother.getId(), child2.getId());
                
                log.info("Sample family tree created successfully!");
                log.info("- Created 3 generations");
                log.info("- Created {} persons", 6);
                log.info("- Established parent-child and spouse relationships");
                
            } catch (Exception e) {
                log.warn("Sample data may already exist or error occurred: {}", e.getMessage());
            }
        };
    }
}
