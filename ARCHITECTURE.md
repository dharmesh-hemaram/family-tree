# Architecture Documentation

## System Overview

Family Tree is built as a layered application with clear separation of concerns:

```
┌─────────────────────────────────────────┐
│         REST API Layer                   │
│         (Controllers)                    │
├─────────────────────────────────────────┤
│         Service Layer                    │
│         (Business Logic)                 │
├─────────────────────────────────────────┤
│         Repository Layer                 │
│         (Data Access)                    │
├─────────────────────────────────────────┤
│         Neo4j Graph Database             │
└─────────────────────────────────────────┘
```

## Component Architecture

### 1. Data Layer (Model)

**Graph Nodes:**
- `Person`: Core entity representing an individual
- `Event`: Life events (birth, marriage, death, etc.)
- `Location`: Geographic locations with historical context
- `User`: System users with authentication data

**Graph Relationships:**
- `PARENT_OF`: Parent → Child (directed)
- `SPOUSE_OF`: Person ↔ Person (bidirectional)
- `PARTICIPATED_IN`: Person → Event

### 2. Repository Layer

Spring Data Neo4j repositories provide:
- Basic CRUD operations
- Custom Cypher queries for graph traversal
- Relationship queries (ancestors, descendants, siblings)
- Path finding algorithms

**Key Queries:**
```cypher
// Find ancestors
MATCH (p:Person)-[:PARENT_OF*1..n]->(child:Person)
WHERE id(child) = $personId
RETURN p

// Find shortest relationship path
MATCH path = shortestPath((p1:Person)-[*]-(p2:Person))
WHERE id(p1) = $id1 AND id(p2) = $id2
RETURN path
```

### 3. Service Layer

Business logic including:
- Person lifecycle management
- Relationship establishment
- Lineage calculation
- Access control validation
- Data transformation (Entity ↔ DTO)

### 4. Controller Layer

RESTful API endpoints:
- Standard CRUD operations
- Relationship management
- Advanced queries (lineage, paths)
- Search functionality

### 5. Security Layer

Multi-layered security:
- Spring Security for authentication
- JWT tokens for stateless sessions
- Method-level authorization with `@PreAuthorize`
- Role-based access control (RBAC)

## Data Flow

### Creating a Person with Relationships

```
Client Request → SecurityFilter → Controller → Service → Repository → Neo4j
                                                  ↓
                                            Validation
                                            Transformation
                                            Authorization
```

### Querying Lineage

```
1. Client requests lineage for person X
2. SecurityFilter validates JWT token
3. Controller receives request
4. Service layer:
   - Validates person exists
   - Checks access permissions
   - Queries repository for ancestors
   - Queries repository for descendants
   - Transforms to DTO
5. Controller returns JSON response
```

## Graph Model Benefits

### Why Graph Database?

**Natural Representation:**
- Family relationships are inherently graph-structured
- Direct mapping between domain and data model
- No complex JOIN operations needed

**Query Performance:**
- Relationship traversal is O(1) in Neo4j
- Path finding optimized for graph structure
- Efficient for multi-hop queries (great-great-grandparents)

**Flexibility:**
- Easy to add new relationship types
- No schema migrations for new connections
- Support for variable-depth queries

**Examples:**
```cypher
// Find all 3rd generation descendants
MATCH (p:Person)-[:PARENT_OF*3]->(descendant:Person)
WHERE id(p) = $personId
RETURN descendant

// Find common ancestors of two people
MATCH (p1:Person)<-[:PARENT_OF*]-(ancestor:Person)-[:PARENT_OF*]->(p2:Person)
WHERE id(p1) = $id1 AND id(p2) = $id2
RETURN DISTINCT ancestor
```

## Scalability Considerations

### Database Scaling
- Neo4j supports clustering for read replicas
- Sharding by family branches possible
- Caching layer for frequently accessed data

### API Scaling
- Stateless design enables horizontal scaling
- Load balancer distributes requests
- Connection pooling for database efficiency

### Performance Optimization
- Index on person names for search
- Depth limits on recursive queries
- Pagination for large result sets
- Lazy loading of relationships

## Security Architecture

### Authentication Flow
```
1. User login → Credentials validation
2. Generate JWT token with roles
3. Return token to client
4. Client includes token in Authorization header
5. Security filter validates token on each request
6. Extract user identity and roles
7. Proceed to authorized endpoint
```

### Authorization Levels

**Endpoint Protection:**
```java
@PreAuthorize("hasAnyRole('ADMIN', 'EDITOR')")
public ResponseEntity<Person> createPerson(...)
```

**Data-Level Security:**
- Public persons: Viewable by anyone
- Family persons: Viewable by authenticated family members
- Private persons: Viewable only by creator or admin

## AI-Readiness Features

### Data Export
- RESTful API provides structured data access
- JSON format compatible with ML frameworks
- GraphQL support (planned) for flexible queries

### Graph Structure Benefits
- Ideal for graph neural networks
- Relationship features for ML models
- Easy to extract sub-graphs for analysis

### Potential AI Applications
1. **Relationship Prediction**: Suggest missing relationships
2. **Data Completion**: Infer missing dates/locations
3. **Record Matching**: Match historical records to persons
4. **Pattern Detection**: Identify naming patterns, migration trends
5. **Anomaly Detection**: Find data inconsistencies

## Future Enhancements

### Planned Features
1. **Event Sourcing**: Full audit trail of changes
2. **CQRS**: Separate read/write models for optimization
3. **Caching**: Redis for frequently accessed data
4. **GraphQL**: More flexible query interface
5. **Webhooks**: Real-time notifications of changes
6. **Batch Processing**: Bulk import/export operations

### Technology Considerations
- Elasticsearch for advanced text search
- Message queue (RabbitMQ/Kafka) for async operations
- Object storage (S3) for photos/documents
- CDN for static assets
