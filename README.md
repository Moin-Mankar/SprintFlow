# 🚀 SprintFlow

> A backend-focused project management platform built with Spring Boot, designed for teams to organize workspaces, projects, Kanban boards, tasks, and collaboration.

![Java](https://img.shields.io/badge/Java-21-orange?style=for-the-badge&logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen?style=for-the-badge&logo=springboot)
![Spring Security](https://img.shields.io/badge/Spring%20Security-JWT-green?style=for-the-badge&logo=springsecurity)
![MySQL](https://img.shields.io/badge/MySQL-8.x-blue?style=for-the-badge&logo=mysql)
![Hibernate](https://img.shields.io/badge/Hibernate-JPA-brown?style=for-the-badge&logo=hibernate)
![Swagger](https://img.shields.io/badge/OpenAPI-Swagger%20UI-85EA2D?style=for-the-badge&logo=swagger)

---

## 📌 Overview

**SprintFlow** is a RESTful project management backend that provides teams with a structured way to manage their work.

The system is built around a hierarchy of:

```text
User
  ↓
Workspace
  ↓
Project
  ↓
Board
  ↓
Task
  ↓
Comments

### 📋 Kanban Boards

Projects can be organized using Kanban boards.

Example workflow:

```text
┌──────────────┬──────────────────┬──────────────┐
│     TODO     │   IN PROGRESS    │     DONE     │
├──────────────┼──────────────────┼──────────────┤
│ Task A       │ Task C           │ Task E       │
│ Task B       │ Task D           │ Task F       │
└──────────────┴──────────────────┴──────────────┘
```

Features:

- Create boards
- Retrieve project boards
- Position-based board ordering
- Move tasks between boards
- Automatic task-status synchronization

Board-to-status mapping:

```text
TODO        → TODO
IN PROGRESS → IN_PROGRESS
IN REVIEW   → IN_REVIEW
DONE        → DONE
```

Moving a task to a different Kanban board automatically updates its corresponding task status.

---

### ✅ Task Management

Tasks support:

- Create tasks
- Retrieve tasks
- Update tasks
- Delete tasks
- Assign tasks to users
- Due dates
- Task priorities
- Task statuses
- Move tasks between boards
- Track task creator
- Track task assignee

#### Task Priorities

```text
LOW
MEDIUM
HIGH
CRITICAL
```

#### Task Statuses

```text
TODO
IN_PROGRESS
IN_REVIEW
DONE
```

---

### 💬 Comments

Users can collaborate directly on tasks through comments.

Features:

- Add comments to tasks
- Retrieve task comments
- Delete comments
- Track comment author
- Track comment creation time
- Project membership authorization

---

### 📊 Dashboard

SprintFlow provides workspace-level dashboard statistics.

The dashboard currently tracks:

- Total projects
- Total tasks
- TODO tasks
- In-progress tasks
- In-review tasks
- Completed tasks
- Overdue tasks

Example response:

```json
{
  "totalProjects": 3,
  "totalTasks": 10,
  "todoTasks": 4,
  "inProgressTasks": 3,
  "inReviewTasks": 1,
  "completedTasks": 2,
  "overdueTasks": 1
}
```

---

### 🔎 Search & Filtering

Tasks can be searched and filtered using query parameters.

Supported filters:

- Task status
- Task priority
- Task title

#### Filter by Status

```http
GET /api/boards/{boardId}/tasks/search?status=TODO
```

#### Filter by Priority

```http
GET /api/boards/{boardId}/tasks/search?priority=HIGH
```

#### Search by Title

```http
GET /api/boards/{boardId}/tasks/search?title=JWT
```

Title searching is case-insensitive.

---

## 🛠️ Tech Stack

| Technology | Purpose |
|------------|---------|
| Java 21 | Backend programming language |
| Spring Boot | Application framework |
| Spring MVC | REST API development |
| Spring Data JPA | Database persistence |
| Hibernate | ORM |
| Spring Security | Authentication & authorization |
| JWT | Stateless authentication |
| BCrypt | Password hashing |
| MySQL | Relational database |
| Maven | Dependency management |
| Swagger / OpenAPI | API documentation |
| Postman | API testing |
| IntelliJ IDEA | Development environment |

---

## 🏗️ Architecture

SprintFlow follows a layered backend architecture:

```text
                    ┌────────────────────┐
                    │      Client        │
                    │  Postman / Swagger │
                    └─────────┬──────────┘
                              │
                              ▼
                    ┌────────────────────┐
                    │    Controllers     │
                    │     REST Layer     │
                    └─────────┬──────────┘
                              │
                              ▼
                    ┌────────────────────┐
                    │      Services      │
                    │   Business Logic   │
                    └─────────┬──────────┘
                              │
                              ▼
                    ┌────────────────────┐
                    │    Repositories    │
                    │     Data Access    │
                    └─────────┬──────────┘
                              │
                              ▼
                    ┌────────────────────┐
                    │       MySQL        │
                    │      Database      │
                    └────────────────────┘
```

---

## 🗄️ Core Domain Model

The major domain relationships are:

```text
User
 │
 ├───────────────┐
 │               │
 ▼               ▼
Workspace      Project
 │               │
 │               ▼
 │             Board
 │               │
 │               ▼
 └────────────► Task
                 │
                 ▼
              Comment
```

### Main Entities

- `User`
- `Workspace`
- `WorkspaceMember`
- `Project`
- `ProjectMember`
- `Board`
- `Task`
- `Comment`
- `Invitation`

---

## 🔐 Authentication Flow

SprintFlow uses JWT-based stateless authentication.

```text
Client
  │
  │ Login
  ▼
AuthController
  │
  ▼
Authentication Service
  │
  ▼
JWT generated
  │
  ▼
Client receives token
  │
  │ Authorization: Bearer <JWT>
  ▼
JwtAuthenticationFilter
  │
  ▼
Spring Security
  │
  ▼
Protected Controller
  │
  ▼
Service Authorization
  │
  ▼
Business Logic
```

Passwords are hashed using BCrypt and are never stored as plain text.

---

## 👥 Authorization Model

SprintFlow uses membership-based authorization.

```text
Workspace
    │
    ├── Workspace Members
    │
    ▼
Project
    │
    ├── Project Members
    │
    ▼
Board
    │
    ▼
Task
    │
    ▼
Comment
```

Before performing protected operations, the service layer verifies that the authenticated user belongs to the relevant workspace or project.

---

## 📖 API Documentation

SprintFlow uses **OpenAPI 3 / Swagger UI** for interactive API documentation.

Once the application is running, open:

```text
http://localhost:8080/swagger-ui/index.html
```

Swagger provides:

- API endpoint documentation
- Request and response schemas
- Interactive endpoint testing
- JWT Bearer authentication
- OpenAPI specification

The **Authorize** button can be used to provide a JWT token and test protected endpoints directly from Swagger UI.

---

## 🧪 API Testing

The API has been tested using:

- Postman
- Swagger UI
- MySQL database verification

Major workflows tested include:

```text
Registration
      ↓
Login
      ↓
JWT Authentication
      ↓
Workspace Creation
      ↓
Workspace Invitation
      ↓
Project Creation
      ↓
Project Membership
      ↓
Board Creation
      ↓
Task Creation
      ↓
Task Assignment
      ↓
Task Movement
      ↓
Comments
      ↓
Dashboard
      ↓
Search / Filtering
```

---

## 📂 Project Structure

```text
src/
└── main/
    └── java/
        └── com/
            └── sprintflow/
                └── backend/
                    ├── config/
                    ├── controller/
                    ├── dto/
                    ├── entity/
                    ├── enums/
                    ├── repository/
                    ├── security/
                    ├── service/
                    └── SprintflowApplication.java
```

---

## 🚀 Getting Started

### Prerequisites

Make sure you have installed:

- Java 21
- Maven
- MySQL 8+
- Git

---

### 1. Clone the Repository

```bash
git clone <YOUR_GITHUB_REPOSITORY_URL>
```

```bash
cd sprintflow
```

---

### 2. Create the Database

Create a MySQL database:

```sql
CREATE DATABASE sprintflow;
```

---

### 3. Configure the Application

Update:

```text
src/main/resources/application.properties
```

Example:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/sprintflow
spring.datasource.username=YOUR_USERNAME
spring.datasource.password=YOUR_PASSWORD

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

> ⚠️ Never commit real database credentials or JWT secrets to GitHub.

---

### 4. Configure JWT Secret

Configure the JWT secret using an environment variable or local configuration.

Example:

```properties
jwt.secret=${JWT_SECRET}
```

Then configure the environment variable:

```text
JWT_SECRET=your-secret-value
```

Keep secrets out of source control.

---

### 5. Run the Application

Using Maven:

```bash
./mvnw spring-boot:run
```

On Windows:

```bash
mvnw.cmd spring-boot:run
```

You can also run the Spring Boot application directly from IntelliJ IDEA.

---

### 6. Open Swagger

Once the application is running:

```text
http://localhost:8080/swagger-ui/index.html
```

Click **Authorize** and provide your JWT token to test protected endpoints.

---

## 🎯 Key Backend Concepts Demonstrated

SprintFlow demonstrates practical backend engineering concepts including:

- REST API design
- Spring Boot layered architecture
- Dependency injection
- Spring Security
- JWT authentication
- Authorization
- Role-based access control
- JPA entity relationships
- Spring Data JPA
- Repository query methods
- DTO-based API design
- Request validation
- Transaction management
- Database-driven business logic
- Kanban workflow modeling
- Task assignment
- Search and filtering
- Dashboard aggregation
- OpenAPI / Swagger documentation

---

## 🔮 Future Improvements

Potential future improvements include:

- Pagination for large datasets
- Advanced combined task filtering
- Task activity/history tracking
- Notifications
- Real-time updates using WebSockets
- File attachments
- Audit logging
- Redis caching
- Docker containerization
- CI/CD pipeline
- Cloud deployment
- Automated unit and integration testing

---

## 👨‍💻 Author

### Moin Mankar

Backend-focused software engineering project built using Java and Spring Boot.

---

## ⭐ SprintFlow

**SprintFlow** demonstrates a backend-focused project management workflow using Spring Boot, JWT authentication, Spring Security, JPA, MySQL, Kanban boards, task management, collaboration features, dashboard analytics, search/filtering, and OpenAPI documentation.