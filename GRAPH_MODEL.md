# Graph Data Model

This document explains the graph database model used in Family Tree.

## Graph Structure

```
┌─────────────┐
│   Person    │ (Node)
└─────────────┘
      │
      ├─── PARENT_OF ──→ Person (Child)
      ├─── SPOUSE_OF ───→ Person (Spouse)
      └─── PARTICIPATED_IN ──→ Event
```

## Node Types

### Person Node
```cypher
(p:Person {
  id: Long,
  firstName: String,
  middleName: String,
  lastName: String,
  maidenName: String,
  birthDate: LocalDate,
  deathDate: LocalDate,
  gender: String,
  biography: String,
  profileImageUrl: String,
  birthPlace: String,
  deathPlace: String,
  currentLocation: String,
  occupation: String,
  nationality: String,
  isPublic: Boolean,
  visibility: String
})
```

### Event Node
```cypher
(e:Event {
  id: Long,
  eventType: String,
  title: String,
  description: String,
  eventDate: LocalDate,
  location: String,
  source: String,
  sourceUrl: String
})
```

### Location Node
```cypher
(l:Location {
  id: Long,
  name: String,
  city: String,
  state: String,
  country: String,
  latitude: Double,
  longitude: Double,
  historicalName: String,
  historicalYear: Integer,
  description: String
})
```

### User Node
```cypher
(u:User {
  id: Long,
  username: String,
  email: String,
  passwordHash: String,
  firstName: String,
  lastName: String,
  roles: Set<String>,
  enabled: Boolean,
  createdAt: LocalDateTime,
  lastLoginAt: LocalDateTime,
  personId: Long
})
```

## Relationship Types

### PARENT_OF
Direction: Parent → Child
```cypher
(parent:Person)-[:PARENT_OF]->(child:Person)
```

**Example:**
```cypher
(john:Person)-[:PARENT_OF]->(emily:Person)
```

### SPOUSE_OF
Direction: Bidirectional
```cypher
(person1:Person)-[:SPOUSE_OF]-(person2:Person)
```

**Example:**
```cypher
(john:Person)-[:SPOUSE_OF]-(sarah:Person)
```

### PARTICIPATED_IN
Direction: Person → Event
```cypher
(person:Person)-[:PARTICIPATED_IN]->(event:Event)
```

## Example Queries

### Find All Ancestors
```cypher
MATCH (p:Person)-[:PARENT_OF*1..10]->(child:Person)
WHERE id(child) = $personId
RETURN p
```

### Find All Descendants
```cypher
MATCH (p:Person)-[:PARENT_OF*1..10]->(descendant:Person)
WHERE id(p) = $personId
RETURN descendant
```

### Find Siblings
```cypher
MATCH (p:Person)<-[:PARENT_OF]-(parent:Person)-[:PARENT_OF]->(sibling:Person)
WHERE id(p) = $personId AND id(p) <> id(sibling)
RETURN DISTINCT sibling
```

### Find Shortest Relationship Path
```cypher
MATCH path = shortestPath((p1:Person)-[*]-(p2:Person))
WHERE id(p1) = $person1Id AND id(p2) = $person2Id
RETURN path
```

### Find Common Ancestors
```cypher
MATCH (p1:Person)<-[:PARENT_OF*]-(ancestor:Person)-[:PARENT_OF*]->(p2:Person)
WHERE id(p1) = $id1 AND id(p2) = $id2
RETURN DISTINCT ancestor
```

### Count Descendants
```cypher
MATCH (p:Person)-[:PARENT_OF*]->(descendant:Person)
WHERE id(p) = $personId
RETURN count(descendant) as totalDescendants
```

## Benefits of Graph Model

1. **Natural Representation**: Family relationships are inherently graph-structured
2. **Efficient Traversal**: O(1) relationship lookups
3. **Flexible Depth**: Easy to query N generations
4. **Path Finding**: Built-in algorithms for relationship discovery
5. **No JOINs**: Direct relationship traversal
6. **Schema Flexibility**: Easy to add new relationship types

## Indexes

Recommended indexes for performance:

```cypher
CREATE INDEX person_name FOR (p:Person) ON (p.firstName, p.lastName);
CREATE INDEX person_birthdate FOR (p:Person) ON (p.birthDate);
CREATE INDEX user_username FOR (u:User) ON (u.username);
CREATE INDEX user_email FOR (u:User) ON (u.email);
```

## Constraints

```cypher
CREATE CONSTRAINT person_id IF NOT EXISTS FOR (p:Person) REQUIRE p.id IS UNIQUE;
CREATE CONSTRAINT user_id IF NOT EXISTS FOR (u:User) REQUIRE u.id IS UNIQUE;
CREATE CONSTRAINT user_username IF NOT EXISTS FOR (u:User) REQUIRE u.username IS UNIQUE;
CREATE CONSTRAINT user_email IF NOT EXISTS FOR (u:User) REQUIRE u.email IS UNIQUE;
```
