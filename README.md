# Family Tree

**A graph-driven genealogy platform for modeling lineage, relationships, and multi-generation family history.**

Family Tree is a modern, scalable platform designed to manage complex kinship structures, historical location tracking, advanced ancestor discovery, and role-based access control. Built with a graph database at its core, Family Tree treats ancestry as what it truly is—a graph, not a table.

## 🌟 Features

### Core Capabilities
- **Graph-Driven Architecture**: Utilizes Neo4j graph database for natural representation of family relationships
- **Complex Kinship Support**: Models various relationship types including parent-child, spouse, siblings, and extended family
- **Multi-Generation Lineage**: Track and visualize family trees across unlimited generations
- **Advanced Ancestor Discovery**: Powerful algorithms to find relationships between any two people in the tree

### Data Management
- **Comprehensive Person Profiles**: Store detailed biographical information including:
  - Basic demographics (name, gender, dates)
  - Historical locations (birth place, death place, residence)
  - Life events and occupation
  - Photos and biographical notes
- **Historical Location Tracking**: Maintain geographical context with support for historical place names
- **Event Timeline**: Record significant life events with dates and locations
- **Privacy Controls**: Configurable visibility settings (public, family, private)

### Security & Access
- **Role-Based Access Control (RBAC)**: Four-tier permission system
  - **Admin**: Full system access
  - **Editor**: Can create and modify family records
  - **Viewer**: Read-only access to permitted records
  - **Family Member**: Access to own family branch
- **Authentication**: Secure JWT-based authentication
- **Data Privacy**: Fine-grained control over who can view what information

### AI & Future-Ready
- **Clean Data Model**: Structured for machine learning applications
- **Export Capabilities**: RESTful API for data integration
- **Graph Queries**: Support for complex relationship analysis
- **Extensible Architecture**: Easy to add new features and integrations

## 🏗️ Architecture

### Technology Stack
- **Backend**: Spring Boot 3.2
- **Database**: Neo4j (Graph Database)
- **Security**: Spring Security with JWT
- **API**: RESTful endpoints
- **Build Tool**: Maven
- **Language**: Java 17

### Design Principles
1. **Graph-First**: All relationships are first-class citizens
2. **Scalability**: Designed to handle large family trees (thousands of people)
3. **Clean Separation**: Clear boundaries between layers (Model, Repository, Service, Controller)
4. **Security by Default**: Authentication and authorization built-in
5. **API-Driven**: All functionality accessible via REST API

## 🚀 Getting Started

### Prerequisites
- Java 17 or higher
- Neo4j 5.x
- Maven 3.6+

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/dharmesh-hemaram/family-tree.git
   cd family-tree
   ```

2. **Start Neo4j Database**
   ```bash
   # Using Docker
   docker run -d \
     --name neo4j \
     -p 7474:7474 -p 7687:7687 \
     -e NEO4J_AUTH=neo4j/password \
     neo4j:5.13
   ```

3. **Configure Application**
   Edit `src/main/resources/application.properties`:
   ```properties
   spring.neo4j.uri=bolt://localhost:7687
   spring.neo4j.authentication.username=neo4j
   spring.neo4j.authentication.password=password
   ```

4. **Build the Application**
   ```bash
   mvn clean install
   ```

5. **Run the Application**
   ```bash
   mvn spring-boot:run
   ```

The application will start on `http://localhost:8080`

## 📖 API Documentation

### Person Management

#### Create a Person
```http
POST /api/persons
Content-Type: application/json

{
  "firstName": "John",
  "lastName": "Doe",
  "birthDate": "1950-01-15",
  "gender": "MALE",
  "birthPlace": "New York, USA",
  "isPublic": true,
  "visibility": "PUBLIC"
}
```

#### Get Person Details
```http
GET /api/persons/{id}
```

#### Search Persons
```http
GET /api/persons/search?name=John
```

#### Add Parent-Child Relationship
```http
POST /api/persons/relationships/parent-child
Content-Type: application/json

{
  "person1Id": 1,  // Parent ID
  "person2Id": 2   // Child ID
}
```

#### Get Lineage
```http
GET /api/persons/{id}/lineage?ancestorDepth=5&descendantDepth=5
```

#### Find Relationship Path
```http
GET /api/persons/relationship-path?person1Id=1&person2Id=5
```

### Advanced Queries

The platform supports advanced graph queries including:
- **Ancestor Discovery**: Find all ancestors up to N generations
- **Descendant Tracking**: Find all descendants down to N generations
- **Sibling Finding**: Identify all siblings of a person
- **Relationship Path**: Calculate the shortest relationship path between any two people
- **Family Branch Analysis**: Analyze specific branches of the family tree

## 🗄️ Data Model

### Core Entities

#### Person (Node)
- Demographics: name, gender, dates
- Locations: birth, death, current
- Metadata: occupation, nationality, biography
- Privacy: visibility settings

#### Relationships (Edges)
- **PARENT_OF**: Directed relationship from parent to child
- **SPOUSE_OF**: Bidirectional relationship between spouses
- **PARTICIPATED_IN**: Link to events

#### Event (Node)
- Event type: birth, death, marriage, migration
- Date and location
- Source documentation

#### Location (Node)
- Geographic coordinates
- Historical names and dates
- Hierarchical structure (city, state, country)

## 🔒 Security Model

### Role Hierarchy
```
ADMIN > EDITOR > FAMILY_MEMBER > VIEWER
```

### Permissions
- **ADMIN**: All operations
- **EDITOR**: Create, update persons and relationships
- **FAMILY_MEMBER**: View family branch, suggest edits
- **VIEWER**: Read-only access to public records

### Authentication Flow
1. User registers/logs in
2. JWT token issued
3. Token included in subsequent requests
4. Role-based authorization on each endpoint

## 🧪 Testing

Run the test suite:
```bash
mvn test
```

## 📊 Use Cases

### Genealogy Research
- Build and maintain family trees
- Track migration patterns
- Document family history
- Preserve family stories

### Historical Analysis
- Study population movements
- Analyze naming patterns
- Track occupational trends
- Geographic distribution analysis

### AI/ML Applications
- Relationship prediction
- Missing data inference
- Pattern recognition in family structures
- Genetic trait analysis preparation

## 🛣️ Roadmap

### Phase 1: Core Platform (Current)
- ✅ Graph-based data model
- ✅ Person and relationship management
- ✅ Basic security and RBAC
- ✅ RESTful API

### Phase 2: Enhanced Features
- [ ] GraphQL API for complex queries
- [ ] Advanced search with filters
- [ ] Bulk import/export (GEDCOM support)
- [ ] Photo gallery and document management

### Phase 3: Collaboration
- [ ] Multi-user editing with conflict resolution
- [ ] Comments and annotations
- [ ] Source citation management
- [ ] Change history and audit trail

### Phase 4: AI Integration
- [ ] Relationship suggestion engine
- [ ] Missing data inference
- [ ] DNA match integration
- [ ] Historical record matching

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## 📄 License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

Built with:
- Spring Boot
- Neo4j Graph Database
- Spring Security
- Project Lombok

---

**Family Tree** - Because ancestry is a graph, not a table. 
