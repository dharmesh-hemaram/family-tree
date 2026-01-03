package com.familytree.repository;

import com.familytree.model.Location;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Location entity.
 */
@Repository
public interface LocationRepository extends Neo4jRepository<Location, Long> {
    
    Optional<Location> findByName(String name);
    
    List<Location> findByCountry(String country);
    
    List<Location> findByCity(String city);
}
