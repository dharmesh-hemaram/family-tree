package com.familytree.repository;

import com.familytree.model.Event;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for Event entity.
 */
@Repository
public interface EventRepository extends Neo4jRepository<Event, Long> {
    
    List<Event> findByEventType(String eventType);
    
    @Query("MATCH (e:Event)<-[:PARTICIPATED_IN]-(p:Person) " +
           "WHERE id(p) = $personId " +
           "RETURN e ORDER BY e.eventDate")
    List<Event> findEventsByPerson(@Param("personId") Long personId);
}
