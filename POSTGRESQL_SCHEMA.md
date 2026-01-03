# PostgreSQL Database Schema for Family Tree System

This document provides a comprehensive PostgreSQL database schema design for the Family Tree genealogy platform. While the current implementation uses Neo4j graph database, this PostgreSQL schema offers an alternative relational approach with optimizations for recursive queries, auditability, and scalability.

## Overview

The schema is designed to:
- Model people, relationships, and lineage as graph-like data in a relational database
- Support parent-child, spouse, and extended family relationships
- Track temporal location history with full audit trail
- Optimize recursive ancestor/descendant queries using PostgreSQL's recursive CTEs
- Maintain data integrity with constraints and foreign keys
- Scale for large family trees with proper indexing
- Integrate seamlessly with Java/Spring Boot backend

## Core Tables

### 1. Person Table

Stores individual person records with genealogical attributes.

```sql
CREATE TABLE person (
    person_id BIGSERIAL PRIMARY KEY,
    
    -- Name information
    first_name VARCHAR(100) NOT NULL,
    middle_name VARCHAR(100),
    last_name VARCHAR(100) NOT NULL,
    maiden_name VARCHAR(100),
    
    -- Biographical data
    gender VARCHAR(20) CHECK (gender IN ('MALE', 'FEMALE', 'OTHER', 'UNKNOWN')),
    birth_date DATE,
    death_date DATE,
    birth_place VARCHAR(255),
    death_place VARCHAR(255),
    current_location VARCHAR(255),
    
    -- Additional metadata
    occupation VARCHAR(100),
    nationality VARCHAR(100),
    biography TEXT,
    profile_image_url VARCHAR(500),
    
    -- Privacy and access control
    is_public BOOLEAN DEFAULT true,
    visibility VARCHAR(20) DEFAULT 'PUBLIC' CHECK (visibility IN ('PUBLIC', 'FAMILY', 'PRIVATE')),
    
    -- Audit fields
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    version INTEGER DEFAULT 1,
    
    -- Constraints
    CONSTRAINT chk_dates CHECK (death_date IS NULL OR birth_date IS NULL OR death_date >= birth_date)
);

-- Indexes for common queries
CREATE INDEX idx_person_names ON person(last_name, first_name);
CREATE INDEX idx_person_birth_date ON person(birth_date);
CREATE INDEX idx_person_death_date ON person(death_date) WHERE death_date IS NOT NULL;
CREATE INDEX idx_person_visibility ON person(visibility, is_public);
CREATE INDEX idx_person_created_at ON person(created_at);

-- Full-text search index for names and biography
CREATE INDEX idx_person_fulltext ON person USING gin(
    to_tsvector('english', 
        coalesce(first_name, '') || ' ' || 
        coalesce(middle_name, '') || ' ' || 
        coalesce(last_name, '') || ' ' || 
        coalesce(biography, '')
    )
);

COMMENT ON TABLE person IS 'Stores individual person records with genealogical and biographical information';
```

### 2. Relationship Table

Models all types of relationships between people with temporal validity.

```sql
CREATE TABLE relationship (
    relationship_id BIGSERIAL PRIMARY KEY,
    
    -- Relationship participants
    person1_id BIGINT NOT NULL REFERENCES person(person_id) ON DELETE CASCADE,
    person2_id BIGINT NOT NULL REFERENCES person(person_id) ON DELETE CASCADE,
    
    -- Relationship type and metadata
    relationship_type VARCHAR(50) NOT NULL CHECK (relationship_type IN (
        'PARENT_CHILD',      -- person1 is parent of person2
        'SPOUSE',            -- bidirectional marriage
        'SIBLING',           -- biological siblings
        'HALF_SIBLING',      -- share one parent
        'ADOPTED',           -- adoption relationship
        'GUARDIAN',          -- legal guardian
        'GODPARENT',         -- godparent relationship
        'STEP_PARENT',       -- step-parent relationship
        'IN_LAW'             -- extended family through marriage
    )),
    
    -- Temporal validity
    start_date DATE,
    end_date DATE,
    is_current BOOLEAN DEFAULT true,
    
    -- Additional relationship metadata
    relationship_notes TEXT,
    confidence_level VARCHAR(20) DEFAULT 'CONFIRMED' CHECK (confidence_level IN (
        'CONFIRMED', 'PROBABLE', 'POSSIBLE', 'DISPUTED'
    )),
    
    -- Source documentation
    source VARCHAR(500),
    source_url VARCHAR(500),
    
    -- Audit fields
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    version INTEGER DEFAULT 1,
    
    -- Constraints
    CONSTRAINT chk_different_persons CHECK (person1_id != person2_id),
    CONSTRAINT chk_relationship_dates CHECK (end_date IS NULL OR start_date IS NULL OR end_date >= start_date),
    CONSTRAINT chk_bidirectional_unique UNIQUE (person1_id, person2_id, relationship_type, start_date)
);

-- Indexes for relationship queries
CREATE INDEX idx_relationship_person1 ON relationship(person1_id, relationship_type, is_current);
CREATE INDEX idx_relationship_person2 ON relationship(person2_id, relationship_type, is_current);
CREATE INDEX idx_relationship_type ON relationship(relationship_type, is_current);
CREATE INDEX idx_relationship_dates ON relationship(start_date, end_date);

-- Composite index for graph traversal
CREATE INDEX idx_relationship_graph ON relationship(person1_id, person2_id, relationship_type) 
    WHERE is_current = true;

COMMENT ON TABLE relationship IS 'Models all types of relationships between people with temporal validity and source documentation';
```

### 3. Location History Table

Tracks temporal location history for each person.

```sql
CREATE TABLE location_history (
    location_history_id BIGSERIAL PRIMARY KEY,
    
    -- Person reference
    person_id BIGINT NOT NULL REFERENCES person(person_id) ON DELETE CASCADE,
    
    -- Location details
    location_name VARCHAR(255) NOT NULL,
    city VARCHAR(100),
    state VARCHAR(100),
    country VARCHAR(100) NOT NULL,
    
    -- Geographic coordinates
    latitude DECIMAL(10, 8),
    longitude DECIMAL(11, 8),
    
    -- Historical context
    historical_name VARCHAR(255),
    historical_year INTEGER,
    
    -- Temporal validity
    from_date DATE,
    to_date DATE,
    is_current BOOLEAN DEFAULT false,
    
    -- Location type
    location_type VARCHAR(50) CHECK (location_type IN (
        'BIRTH', 'DEATH', 'RESIDENCE', 'WORK', 'MIGRATION', 'EVENT'
    )),
    
    -- Audit fields
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    
    -- Constraints
    CONSTRAINT chk_location_dates CHECK (to_date IS NULL OR from_date IS NULL OR to_date >= from_date),
    CONSTRAINT chk_one_current_per_person UNIQUE (person_id, is_current) 
        WHERE is_current = true
);

-- Indexes for location queries
CREATE INDEX idx_location_person ON location_history(person_id, is_current);
CREATE INDEX idx_location_country ON location_history(country, city);
CREATE INDEX idx_location_dates ON location_history(from_date, to_date);
CREATE INDEX idx_location_type ON location_history(location_type, person_id);

-- Spatial index for geographic queries
CREATE INDEX idx_location_coordinates ON location_history USING gist(
    ll_to_earth(latitude, longitude)
) WHERE latitude IS NOT NULL AND longitude IS NOT NULL;

COMMENT ON TABLE location_history IS 'Tracks temporal location history for each person with geographic and historical context';
```

### 4. Event Table

Records significant life events with full context.

```sql
CREATE TABLE event (
    event_id BIGSERIAL PRIMARY KEY,
    
    -- Event details
    event_type VARCHAR(50) NOT NULL CHECK (event_type IN (
        'BIRTH', 'DEATH', 'BAPTISM', 'MARRIAGE', 'DIVORCE',
        'MIGRATION', 'EDUCATION', 'OCCUPATION', 'MILITARY',
        'CENSUS', 'OTHER'
    )),
    event_date DATE,
    event_title VARCHAR(255) NOT NULL,
    event_description TEXT,
    
    -- Location
    location VARCHAR(255),
    
    -- Source documentation
    source VARCHAR(500),
    source_url VARCHAR(500),
    
    -- Audit fields
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT
);

-- Event participation (many-to-many)
CREATE TABLE event_participant (
    event_id BIGINT NOT NULL REFERENCES event(event_id) ON DELETE CASCADE,
    person_id BIGINT NOT NULL REFERENCES person(person_id) ON DELETE CASCADE,
    role VARCHAR(50) DEFAULT 'PARTICIPANT' CHECK (role IN (
        'PARTICIPANT', 'WITNESS', 'OFFICIANT', 'PRIMARY', 'SECONDARY'
    )),
    
    PRIMARY KEY (event_id, person_id)
);

-- Indexes for event queries
CREATE INDEX idx_event_type_date ON event(event_type, event_date);
CREATE INDEX idx_event_date ON event(event_date);
CREATE INDEX idx_event_participant_person ON event_participant(person_id);
CREATE INDEX idx_event_participant_event ON event_participant(event_id);

COMMENT ON TABLE event IS 'Records significant life events with participants and source documentation';
```

### 5. User and Authentication

User management with role-based access control.

```sql
CREATE TABLE app_user (
    user_id BIGSERIAL PRIMARY KEY,
    
    -- Authentication
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    
    -- User profile
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    
    -- Account status
    enabled BOOLEAN DEFAULT true,
    account_locked BOOLEAN DEFAULT false,
    email_verified BOOLEAN DEFAULT false,
    
    -- Linked person (optional)
    person_id BIGINT REFERENCES person(person_id) ON DELETE SET NULL,
    
    -- Timestamps
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    last_login_at TIMESTAMP WITH TIME ZONE,
    password_changed_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- User roles (many-to-many)
CREATE TABLE user_role (
    user_id BIGINT NOT NULL REFERENCES app_user(user_id) ON DELETE CASCADE,
    role_name VARCHAR(50) NOT NULL CHECK (role_name IN (
        'ADMIN', 'EDITOR', 'VIEWER', 'FAMILY_MEMBER'
    )),
    granted_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    granted_by BIGINT REFERENCES app_user(user_id),
    
    PRIMARY KEY (user_id, role_name)
);

-- Indexes
CREATE INDEX idx_user_email ON app_user(email);
CREATE INDEX idx_user_username ON app_user(username);
CREATE INDEX idx_user_person ON app_user(person_id);
CREATE INDEX idx_user_role ON user_role(user_id, role_name);

COMMENT ON TABLE app_user IS 'User authentication and profile information';
COMMENT ON TABLE user_role IS 'Role-based access control for users';
```

## Audit Tables

### Audit Log

Comprehensive audit trail for all changes.

```sql
CREATE TABLE audit_log (
    audit_id BIGSERIAL PRIMARY KEY,
    
    -- What was changed
    table_name VARCHAR(50) NOT NULL,
    record_id BIGINT NOT NULL,
    operation VARCHAR(10) NOT NULL CHECK (operation IN ('INSERT', 'UPDATE', 'DELETE')),
    
    -- Before and after state (JSONB for flexibility)
    old_values JSONB,
    new_values JSONB,
    changed_fields TEXT[], -- Array of changed field names
    
    -- Who and when
    changed_by BIGINT REFERENCES app_user(user_id),
    changed_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    
    -- Additional context
    ip_address INET,
    user_agent TEXT,
    change_reason TEXT
);

-- Indexes for audit queries
CREATE INDEX idx_audit_table_record ON audit_log(table_name, record_id);
CREATE INDEX idx_audit_changed_by ON audit_log(changed_by);
CREATE INDEX idx_audit_changed_at ON audit_log(changed_at);
CREATE INDEX idx_audit_operation ON audit_log(operation, table_name);

-- Partition by date for better performance
CREATE INDEX idx_audit_partition ON audit_log(changed_at, table_name);

COMMENT ON TABLE audit_log IS 'Comprehensive audit trail for all data changes';
```

## Optimized Queries

### 1. Recursive Ancestor Query

Find all ancestors of a person using PostgreSQL's recursive CTE.

```sql
-- Function to find all ancestors up to N generations
CREATE OR REPLACE FUNCTION find_ancestors(
    p_person_id BIGINT,
    p_max_depth INTEGER DEFAULT 10
)
RETURNS TABLE(
    person_id BIGINT,
    first_name VARCHAR,
    last_name VARCHAR,
    relationship_path TEXT,
    generation_level INTEGER
) AS $$
BEGIN
    RETURN QUERY
    WITH RECURSIVE ancestor_tree AS (
        -- Base case: start with the person
        SELECT 
            p.person_id,
            p.first_name,
            p.last_name,
            ''::TEXT as relationship_path,
            0 as generation_level
        FROM person p
        WHERE p.person_id = p_person_id
        
        UNION ALL
        
        -- Recursive case: find parents
        SELECT 
            p.person_id,
            p.first_name,
            p.last_name,
            at.relationship_path || ' -> ' || p.first_name || ' ' || p.last_name,
            at.generation_level + 1
        FROM person p
        INNER JOIN relationship r ON r.person1_id = p.person_id
        INNER JOIN ancestor_tree at ON r.person2_id = at.person_id
        WHERE r.relationship_type = 'PARENT_CHILD'
            AND r.is_current = true
            AND at.generation_level < p_max_depth
    )
    SELECT 
        at.person_id,
        at.first_name,
        at.last_name,
        at.relationship_path,
        at.generation_level
    FROM ancestor_tree at
    WHERE at.generation_level > 0
    ORDER BY at.generation_level, at.last_name, at.first_name;
END;
$$ LANGUAGE plpgsql;
```

### 2. Recursive Descendant Query

Find all descendants of a person.

```sql
-- Function to find all descendants up to N generations
CREATE OR REPLACE FUNCTION find_descendants(
    p_person_id BIGINT,
    p_max_depth INTEGER DEFAULT 10
)
RETURNS TABLE(
    person_id BIGINT,
    first_name VARCHAR,
    last_name VARCHAR,
    relationship_path TEXT,
    generation_level INTEGER
) AS $$
BEGIN
    RETURN QUERY
    WITH RECURSIVE descendant_tree AS (
        -- Base case: start with the person
        SELECT 
            p.person_id,
            p.first_name,
            p.last_name,
            ''::TEXT as relationship_path,
            0 as generation_level
        FROM person p
        WHERE p.person_id = p_person_id
        
        UNION ALL
        
        -- Recursive case: find children
        SELECT 
            p.person_id,
            p.first_name,
            p.last_name,
            dt.relationship_path || ' -> ' || p.first_name || ' ' || p.last_name,
            dt.generation_level + 1
        FROM person p
        INNER JOIN relationship r ON r.person2_id = p.person_id
        INNER JOIN descendant_tree dt ON r.person1_id = dt.person_id
        WHERE r.relationship_type = 'PARENT_CHILD'
            AND r.is_current = true
            AND dt.generation_level < p_max_depth
    )
    SELECT 
        dt.person_id,
        dt.first_name,
        dt.last_name,
        dt.relationship_path,
        dt.generation_level
    FROM descendant_tree dt
    WHERE dt.generation_level > 0
    ORDER BY dt.generation_level, dt.last_name, dt.first_name;
END;
$$ LANGUAGE plpgsql;
```

### 3. Find Siblings

```sql
-- Function to find all siblings (full and half)
CREATE OR REPLACE FUNCTION find_siblings(p_person_id BIGINT)
RETURNS TABLE(
    person_id BIGINT,
    first_name VARCHAR,
    last_name VARCHAR,
    sibling_type VARCHAR
) AS $$
BEGIN
    RETURN QUERY
    SELECT DISTINCT
        s.person_id,
        s.first_name,
        s.last_name,
        CASE 
            WHEN COUNT(DISTINCT r1.person1_id) = 2 THEN 'FULL_SIBLING'
            ELSE 'HALF_SIBLING'
        END as sibling_type
    FROM person s
    INNER JOIN relationship r2 ON r2.person2_id = s.person_id
    INNER JOIN relationship r1 ON r1.person1_id = r2.person1_id
    WHERE r1.person2_id = p_person_id
        AND r1.relationship_type = 'PARENT_CHILD'
        AND r2.relationship_type = 'PARENT_CHILD'
        AND r1.is_current = true
        AND r2.is_current = true
        AND s.person_id != p_person_id
    GROUP BY s.person_id, s.first_name, s.last_name
    ORDER BY s.last_name, s.first_name;
END;
$$ LANGUAGE plpgsql;
```

### 4. Migration History Query

Track geographic migrations over time.

```sql
-- Function to get migration history for a person
CREATE OR REPLACE FUNCTION get_migration_history(p_person_id BIGINT)
RETURNS TABLE(
    location_name VARCHAR,
    city VARCHAR,
    country VARCHAR,
    from_date DATE,
    to_date DATE,
    years_duration INTEGER,
    location_type VARCHAR
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        lh.location_name,
        lh.city,
        lh.country,
        lh.from_date,
        lh.to_date,
        CASE 
            WHEN lh.to_date IS NOT NULL THEN 
                EXTRACT(YEAR FROM AGE(lh.to_date, lh.from_date))::INTEGER
            ELSE NULL
        END as years_duration,
        lh.location_type
    FROM location_history lh
    WHERE lh.person_id = p_person_id
    ORDER BY lh.from_date NULLS LAST, lh.created_at;
END;
$$ LANGUAGE plpgsql;
```

## Triggers for Audit Trail

Automatically log all changes to critical tables.

```sql
-- Generic audit trigger function
CREATE OR REPLACE FUNCTION audit_trigger_func()
RETURNS TRIGGER AS $$
DECLARE
    v_old_data JSONB;
    v_new_data JSONB;
    v_changed_fields TEXT[];
BEGIN
    IF TG_OP = 'DELETE' THEN
        v_old_data = row_to_json(OLD)::JSONB;
        v_new_data = NULL;
    ELSIF TG_OP = 'UPDATE' THEN
        v_old_data = row_to_json(OLD)::JSONB;
        v_new_data = row_to_json(NEW)::JSONB;
        
        -- Identify changed fields
        SELECT array_agg(key)
        INTO v_changed_fields
        FROM jsonb_each(v_old_data)
        WHERE v_old_data->key IS DISTINCT FROM v_new_data->key;
    ELSE -- INSERT
        v_old_data = NULL;
        v_new_data = row_to_json(NEW)::JSONB;
    END IF;
    
    INSERT INTO audit_log (
        table_name,
        record_id,
        operation,
        old_values,
        new_values,
        changed_fields,
        changed_by,
        changed_at
    ) VALUES (
        TG_TABLE_NAME,
        CASE 
            WHEN TG_OP = 'DELETE' THEN (v_old_data->>'person_id')::BIGINT
            ELSE (v_new_data->>'person_id')::BIGINT
        END,
        TG_OP,
        v_old_data,
        v_new_data,
        v_changed_fields,
        current_setting('app.current_user_id', true)::BIGINT,
        CURRENT_TIMESTAMP
    );
    
    RETURN COALESCE(NEW, OLD);
END;
$$ LANGUAGE plpgsql;

-- Apply audit trigger to tables
CREATE TRIGGER person_audit_trigger
    AFTER INSERT OR UPDATE OR DELETE ON person
    FOR EACH ROW EXECUTE FUNCTION audit_trigger_func();

CREATE TRIGGER relationship_audit_trigger
    AFTER INSERT OR UPDATE OR DELETE ON relationship
    FOR EACH ROW EXECUTE FUNCTION audit_trigger_func();

CREATE TRIGGER location_history_audit_trigger
    AFTER INSERT OR UPDATE OR DELETE ON location_history
    FOR EACH ROW EXECUTE FUNCTION audit_trigger_func();
```

## Triggers for Automatic Updates

```sql
-- Trigger to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    NEW.version = OLD.version + 1;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Apply to tables with updated_at column
CREATE TRIGGER person_updated_at
    BEFORE UPDATE ON person
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER relationship_updated_at
    BEFORE UPDATE ON relationship
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER location_history_updated_at
    BEFORE UPDATE ON location_history
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
```

## Java/Spring Integration

### JPA Entity Example

```java
@Entity
@Table(name = "person", indexes = {
    @Index(name = "idx_person_names", columnList = "last_name, first_name"),
    @Index(name = "idx_person_birth_date", columnList = "birth_date")
})
public class Person {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "person_id")
    private Long personId;
    
    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;
    
    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;
    
    @Column(name = "birth_date")
    private LocalDate birthDate;
    
    @Column(name = "death_date")
    private LocalDate deathDate;
    
    @Column(name = "is_public")
    private Boolean isPublic = true;
    
    @Version
    @Column(name = "version")
    private Integer version;
    
    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private Instant updatedAt;
    
    // Getters and setters
}
```

### Repository with Recursive Queries

```java
@Repository
public interface PersonRepository extends JpaRepository<Person, Long> {
    
    // Use native query for recursive ancestor search
    @Query(value = "SELECT * FROM find_ancestors(:personId, :maxDepth)", 
           nativeQuery = true)
    List<PersonProjection> findAncestors(
        @Param("personId") Long personId, 
        @Param("maxDepth") Integer maxDepth
    );
    
    // Use native query for recursive descendant search
    @Query(value = "SELECT * FROM find_descendants(:personId, :maxDepth)", 
           nativeQuery = true)
    List<PersonProjection> findDescendants(
        @Param("personId") Long personId, 
        @Param("maxDepth") Integer maxDepth
    );
    
    // Find siblings
    @Query(value = "SELECT * FROM find_siblings(:personId)", 
           nativeQuery = true)
    List<PersonProjection> findSiblings(@Param("personId") Long personId);
}
```

### Setting User Context for Audit

```java
@Component
@Aspect
public class AuditAspect {
    
    @Autowired
    private EntityManager entityManager;
    
    @Before("@annotation(org.springframework.web.bind.annotation.RequestMapping)")
    public void setUserContext() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserDetails) {
            UserDetails user = (UserDetails) auth.getPrincipal();
            Long userId = getUserId(user);
            
            // Set session variable for audit trigger
            entityManager.createNativeQuery(
                "SELECT set_config('app.current_user_id', :userId, false)"
            ).setParameter("userId", userId.toString()).executeUpdate();
        }
    }
}
```

## Performance Considerations

### Partitioning

For large datasets, partition the audit_log table by date:

```sql
-- Create partitioned audit_log table
CREATE TABLE audit_log_partitioned (
    LIKE audit_log INCLUDING ALL
) PARTITION BY RANGE (changed_at);

-- Create monthly partitions
CREATE TABLE audit_log_2024_01 PARTITION OF audit_log_partitioned
    FOR VALUES FROM ('2024-01-01') TO ('2024-02-01');

CREATE TABLE audit_log_2024_02 PARTITION OF audit_log_partitioned
    FOR VALUES FROM ('2024-02-01') TO ('2024-03-01');
-- Add more partitions as needed
```

### Materialized Views

For frequently accessed lineage data:

```sql
-- Materialized view for quick ancestor counts
CREATE MATERIALIZED VIEW mv_person_ancestor_counts AS
SELECT 
    p.person_id,
    COUNT(DISTINCT a.person_id) as ancestor_count
FROM person p
LEFT JOIN LATERAL find_ancestors(p.person_id, 10) a ON true
GROUP BY p.person_id;

CREATE UNIQUE INDEX ON mv_person_ancestor_counts(person_id);

-- Refresh periodically
REFRESH MATERIALIZED VIEW CONCURRENTLY mv_person_ancestor_counts;
```

## Database Connection Pool Settings

For Java/Spring applications, configure HikariCP:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/familytree
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
  jpa:
    hibernate:
      ddl-auto: validate
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        jdbc:
          batch_size: 20
        order_inserts: true
        order_updates: true
```

## Migration from Neo4j

If migrating from Neo4j to PostgreSQL:

```sql
-- ETL script to migrate from Neo4j export
CREATE OR REPLACE FUNCTION migrate_from_neo4j()
RETURNS void AS $$
BEGIN
    -- Import persons from CSV export
    COPY person(person_id, first_name, last_name, birth_date, death_date)
    FROM '/path/to/neo4j_persons_export.csv'
    DELIMITER ',' CSV HEADER;
    
    -- Import relationships from CSV export
    COPY relationship(person1_id, person2_id, relationship_type, start_date)
    FROM '/path/to/neo4j_relationships_export.csv'
    DELIMITER ',' CSV HEADER;
    
    -- Update sequences
    SELECT setval('person_person_id_seq', (SELECT MAX(person_id) FROM person));
    SELECT setval('relationship_relationship_id_seq', (SELECT MAX(relationship_id) FROM relationship));
END;
$$ LANGUAGE plpgsql;
```

## Summary

This PostgreSQL schema provides:

✅ **Graph-like relationship modeling** using junction tables and recursive CTEs  
✅ **Temporal validity** for relationships and location history  
✅ **Complete audit trail** with triggers and JSONB storage  
✅ **Optimized recursive queries** for ancestors/descendants  
✅ **Migration tracking** with location_history table  
✅ **Proper indexing** for performance at scale  
✅ **Data integrity** with constraints and foreign keys  
✅ **Java/Spring integration** ready with JPA entities  
✅ **Scalability** through partitioning and materialized views  
✅ **Auditability** at database level with triggers  

The schema balances relational database strengths (ACID, constraints, SQL) with graph-like querying capabilities (recursive CTEs, relationship traversal) while maintaining excellent performance for genealogical workloads.
