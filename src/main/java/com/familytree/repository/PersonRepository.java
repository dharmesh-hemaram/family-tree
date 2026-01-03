package com.familytree.repository;

import com.familytree.model.Person;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Person entity with graph-specific queries.
 */
@Repository
public interface PersonRepository extends Neo4jRepository<Person, Long> {
    
    Optional<Person> findByFirstNameAndLastName(String firstName, String lastName);
    
    List<Person> findByLastName(String lastName);
    
    /**
     * Find all ancestors of a person up to a certain depth
     */
    @Query("MATCH (p:Person)-[:PARENT_OF*1..{depth}]->(child:Person) " +
           "WHERE id(child) = $personId " +
           "RETURN p")
    List<Person> findAncestors(@Param("personId") Long personId, @Param("depth") int depth);
    
    /**
     * Find all descendants of a person up to a certain depth
     */
    @Query("MATCH (p:Person)-[:PARENT_OF*1..{depth}]->(descendant:Person) " +
           "WHERE id(p) = $personId " +
           "RETURN descendant")
    List<Person> findDescendants(@Param("personId") Long personId, @Param("depth") int depth);
    
    /**
     * Find siblings (people with same parents)
     */
    @Query("MATCH (p:Person)<-[:PARENT_OF]-(parent:Person)-[:PARENT_OF]->(sibling:Person) " +
           "WHERE id(p) = $personId AND id(p) <> id(sibling) " +
           "RETURN DISTINCT sibling")
    List<Person> findSiblings(@Param("personId") Long personId);
    
    /**
     * Find relationship path between two people
     */
    @Query("MATCH path = shortestPath((p1:Person)-[*]-(p2:Person)) " +
           "WHERE id(p1) = $person1Id AND id(p2) = $person2Id " +
           "RETURN nodes(path)")
    List<Person> findRelationshipPath(@Param("person1Id") Long person1Id, 
                                      @Param("person2Id") Long person2Id);
    
    /**
     * Search persons by name (partial match)
     */
    @Query("MATCH (p:Person) " +
           "WHERE p.firstName CONTAINS $searchTerm OR p.lastName CONTAINS $searchTerm " +
           "RETURN p LIMIT 50")
    List<Person> searchByName(@Param("searchTerm") String searchTerm);
    
    /**
     * Find all public persons
     */
    List<Person> findByIsPublicTrue();
}
