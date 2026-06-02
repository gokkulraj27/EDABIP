 Enterprise Data Analytics & Business Intelligence Platform (EDABIP)

A full-featured enterprise backend built with **Spring Boot 4**, **Spring Security + JWT**, **MySQL**, and **Spring Data JPA**.

---

## Tech Stack

| Layer       | Technology                        |
|-------------|-----------------------------------|
| Language    | Java 17                           |
| Framework   | Spring Boot 4.0.6                 |
| Security    | Spring Security 7 + JWT (jjwt 0.12) |
| Database    | MySQL 8                           |
| ORM         | Spring Data JPA / Hibernate       |
| Build       | Maven                             |
| Utilities   | Lombok                            |

---

## Modules

| Module                   | Endpoints                           |
|--------------------------|-------------------------------------|
| Auth & User Management   | `/api/auth/**`, `/api/users/**`     |
| Employee Management      | `/api/employees/**`                 |
| Department Management    | `/api/departments/**`               |
| Project & Task Mgmt      | `/api/projects/**`, `/api/tasks/**` |
| Dashboard & Analytics    | `/api/dashboard/**`                 |
| Report Generation        | `/api/reports/**`                   |
| Audit & Logging          | `/api/audit/**`                     |

---

## Setup

### 1. Database
```sql
CREATE DATABASE edabip CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```
Then run `database.sql` to create tables and seed data.

### 2. Configuration
Edit `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/edabip?useSSL=false&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=YOUR_PASSWORD
jwt.secret=your-256-bit-secret-key-here-make-it-long
jwt.expiration=86400000
```

### 3. Run
```bash
mvn spring-boot:run
```
The server starts on `http://localhost:8080`.

---

## Authentication

All secured endpoints require:
```
Authorization: Bearer <token>
```

### Register
```
POST /api/auth/register
{
  "username": "john",
  "email": "john@example.com",
  "password": "pass123",
  "roles": ["ROLE_MANAGER"]
}
```

### Login
```
POST /api/auth/login
{
  "usernameOrEmail": "john",
  "password": "pass123"
}
```
Returns a JWT token valid for 24 hours.

---

## Role-Based Access Control

| Role            | Access                                              |
|-----------------|-----------------------------------------------------|
| `ROLE_ADMIN`    | Full access including user management, audit logs   |
| `ROLE_MANAGER`  | Create/update employees, projects, tasks, dashboard |
| `ROLE_EMPLOYEE` | Read employees, tasks assigned to them              |

---

## API Reference

### Employees
| Method | URL                             | Role            |
|--------|---------------------------------|-----------------|
| POST   | `/api/employees`                | ADMIN, MANAGER  |
| GET    | `/api/employees`                | All auth        |
| GET    | `/api/employees/{id}`           | All auth        |
| GET    | `/api/employees/search?name=&departmentId=&status=` | All auth |
| PUT    | `/api/employees/{id}`           | ADMIN, MANAGER  |
| DELETE | `/api/employees/{id}`           | ADMIN           |

### Departments
| Method | URL                             | Role            |
|--------|---------------------------------|-----------------|
| POST   | `/api/departments`              | ADMIN, MANAGER  |
| GET    | `/api/departments`              | All auth        |
| GET    | `/api/departments/{id}`         | All auth        |
| PUT    | `/api/departments/{id}`         | ADMIN, MANAGER  |
| DELETE | `/api/departments/{id}`         | ADMIN           |

### Projects
| Method | URL                                    | Role            |
|--------|----------------------------------------|-----------------|
| POST   | `/api/projects`                        | ADMIN, MANAGER  |
| GET    | `/api/projects`                        | All auth        |
| GET    | `/api/projects/{id}`                   | All auth        |
| GET    | `/api/projects/department/{deptId}`    | All auth        |
| GET    | `/api/projects/status/{status}`        | All auth        |
| PUT    | `/api/projects/{id}`                   | ADMIN, MANAGER  |
| DELETE | `/api/projects/{id}`                   | ADMIN, MANAGER  |

### Tasks
| Method | URL                                          | Role           |
|--------|----------------------------------------------|----------------|
| POST   | `/api/tasks`                                 | ADMIN, MANAGER |
| GET    | `/api/tasks`                                 | All auth       |
| GET    | `/api/tasks/{id}`                            | All auth       |
| GET    | `/api/tasks/project/{projectId}`             | All auth       |
| GET    | `/api/tasks/employee/{employeeId}`           | All auth       |
| GET    | `/api/tasks/employee/{employeeId}/pending`   | All auth       |
| GET    | `/api/tasks/overdue`                         | ADMIN, MANAGER |
| PUT    | `/api/tasks/{id}`                            | All auth       |
| DELETE | `/api/tasks/{id}`                            | ADMIN, MANAGER |

### Dashboard
| Method | URL                    | Role           |
|--------|------------------------|----------------|
| GET    | `/api/dashboard/stats` | ADMIN, MANAGER |

### Reports
| Method | URL                    | Role           |
|--------|------------------------|----------------|
| POST   | `/api/reports/generate`| ADMIN, MANAGER |
| GET    | `/api/reports`         | All auth       |
| GET    | `/api/reports/{id}`    | All auth       |
| GET    | `/api/reports/filter`  | All auth       |
| DELETE | `/api/reports/{id}`    | ADMIN          |

### Audit Logs
| Method | URL                                           | Role  |
|--------|-----------------------------------------------|-------|
| GET    | `/api/audit?page=0&size=20`                   | ADMIN |
| GET    | `/api/audit/user/{userId}`                    | ADMIN |
| GET    | `/api/audit/entity/{entityType}/{entityId}`   | ADMIN |

---

## Default Admin Credentials
```
Username: admin
Password: admin123
```

---

## Project Structure
```
src/main/java/com/edabip/
├── config/          # SecurityConfig, DataInitializer
├── controller/      # REST controllers
├── dto/
│   ├── request/     # Input DTOs
│   └── response/    # Output DTOs
├── entity/          # JPA entities
│   └── enums/       # Enum types
├── exception/       # Custom exceptions + GlobalExceptionHandler
├── repository/      # Spring Data JPA interfaces
├── security/        # JWT provider, filter, UserDetails
└── service/         # Business logic
```
