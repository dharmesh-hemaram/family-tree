# API Reference

## Base URL
```
http://localhost:8080/api
```

## Authentication

All protected endpoints require a JWT token in the Authorization header:
```
Authorization: Bearer <your-jwt-token>
```

## Endpoints

### Person Management

#### List All Persons
```http
GET /api/persons
```

**Response:**
```json
[
  {
    "id": 1,
    "firstName": "John",
    "lastName": "Doe",
    "birthDate": "1950-01-15",
    "gender": "MALE",
    "isPublic": true
  }
]
```

#### Get Person by ID
```http
GET /api/persons/{id}
```

**Response:**
```json
{
  "id": 1,
  "firstName": "John",
  "middleName": "Robert",
  "lastName": "Doe",
  "birthDate": "1950-01-15",
  "deathDate": null,
  "gender": "MALE",
  "biography": "Family patriarch...",
  "birthPlace": "New York, NY, USA",
  "occupation": "Engineer",
  "nationality": "American",
  "isPublic": true,
  "visibility": "PUBLIC",
  "children": [],
  "parents": [],
  "spouses": []
}
```

#### Search Persons
```http
GET /api/persons/search?name=John
```

**Query Parameters:**
- `name` (required): Search term for first or last name

**Response:**
```json
[
  {
    "id": 1,
    "firstName": "John",
    "lastName": "Doe",
    ...
  },
  {
    "id": 5,
    "firstName": "Johnny",
    "lastName": "Smith",
    ...
  }
]
```

#### Create Person
```http
POST /api/persons
Authorization: Required (ADMIN or EDITOR)
Content-Type: application/json
```

**Request Body:**
```json
{
  "firstName": "Jane",
  "lastName": "Doe",
  "birthDate": "1975-06-20",
  "gender": "FEMALE",
  "birthPlace": "Boston, MA, USA",
  "occupation": "Doctor",
  "isPublic": true,
  "visibility": "PUBLIC"
}
```

**Response:** `201 Created`
```json
{
  "id": 10,
  "firstName": "Jane",
  "lastName": "Doe",
  ...
}
```

#### Update Person
```http
PUT /api/persons/{id}
Authorization: Required (ADMIN or EDITOR)
Content-Type: application/json
```

**Request Body:** Same as Create Person

**Response:** `200 OK`

#### Delete Person
```http
DELETE /api/persons/{id}
Authorization: Required (ADMIN)
```

**Response:** `204 No Content`

### Relationship Management

#### Add Parent-Child Relationship
```http
POST /api/persons/relationships/parent-child
Authorization: Required (ADMIN or EDITOR)
Content-Type: application/json
```

**Request Body:**
```json
{
  "person1Id": 1,
  "person2Id": 10,
  "relationshipType": "PARENT_CHILD",
  "description": "Biological parent"
}
```

**Response:** `200 OK`

#### Add Spouse Relationship
```http
POST /api/persons/relationships/spouse
Authorization: Required (ADMIN or EDITOR)
Content-Type: application/json
```

**Request Body:**
```json
{
  "person1Id": 1,
  "person2Id": 2,
  "relationshipType": "SPOUSE",
  "description": "Married 1970"
}
```

**Response:** `200 OK`

### Lineage and Genealogy Queries

#### Get Lineage
```http
GET /api/persons/{id}/lineage?ancestorDepth=5&descendantDepth=5
```

**Query Parameters:**
- `ancestorDepth` (optional, default: 5): Number of generations to look back
- `descendantDepth` (optional, default: 5): Number of generations to look forward

**Response:**
```json
{
  "personId": 10,
  "personName": "Jane Doe",
  "ancestors": [
    {
      "id": 1,
      "firstName": "John",
      "lastName": "Doe",
      ...
    }
  ],
  "descendants": [],
  "generationsUp": 5,
  "generationsDown": 5
}
```

#### Get Siblings
```http
GET /api/persons/{id}/siblings
```

**Response:**
```json
[
  {
    "id": 11,
    "firstName": "Jack",
    "lastName": "Doe",
    ...
  }
]
```

#### Find Relationship Path
```http
GET /api/persons/relationship-path?person1Id=1&person2Id=15
```

**Query Parameters:**
- `person1Id` (required): First person ID
- `person2Id` (required): Second person ID

**Response:**
```json
[
  {
    "id": 1,
    "firstName": "John",
    "lastName": "Doe"
  },
  {
    "id": 10,
    "firstName": "Jane",
    "lastName": "Doe"
  },
  {
    "id": 15,
    "firstName": "Emma",
    "lastName": "Smith"
  }
]
```

## Data Models

### Person DTO
```typescript
{
  id: number;
  firstName: string;
  middleName?: string;
  lastName: string;
  maidenName?: string;
  birthDate?: string; // ISO 8601 date
  deathDate?: string; // ISO 8601 date
  gender?: "MALE" | "FEMALE" | "OTHER" | "UNKNOWN";
  biography?: string;
  profileImageUrl?: string;
  birthPlace?: string;
  deathPlace?: string;
  currentLocation?: string;
  occupation?: string;
  nationality?: string;
  isPublic: boolean;
  visibility: "PUBLIC" | "FAMILY" | "PRIVATE";
  childrenIds?: number[];
  parentIds?: number[];
  spouseIds?: number[];
}
```

### Relationship DTO
```typescript
{
  person1Id: number;
  person2Id: number;
  relationshipType: string;
  description?: string;
}
```

### Lineage DTO
```typescript
{
  personId: number;
  personName: string;
  ancestors: PersonDTO[];
  descendants: PersonDTO[];
  generationsUp: number;
  generationsDown: number;
}
```

## Error Responses

### 400 Bad Request
```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Invalid input data"
}
```

### 401 Unauthorized
```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "Authentication required"
}
```

### 403 Forbidden
```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 403,
  "error": "Forbidden",
  "message": "Insufficient permissions"
}
```

### 404 Not Found
```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "Person not found"
}
```

## Rate Limiting

Currently, no rate limiting is enforced. Future versions will implement:
- 100 requests per minute for authenticated users
- 20 requests per minute for unauthenticated users

## Pagination

For endpoints returning large datasets, pagination will be added:
```http
GET /api/persons?page=0&size=20&sort=lastName,asc
```

## Versioning

API version is included in the base URL:
```
/api/v1/persons
/api/v2/persons
```

Currently using implicit v1.
