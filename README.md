# AI Smart Study Assistant - MVC Version

## Technology

Frontend:
- HTML
- CSS
- JavaScript

Backend:
- Java 21
- Spring Boot
- Spring MVC
- Spring Security
- JWT
- Spring Data JPA

Database:
- MySQL

AI:
- Google Gemini API

## MVC Structure

The backend follows:

Controller -> Service -> Repository -> Database

### Model
Contains JPA entities:
- User
- StudyHistory

### View
The frontend:
- HTML
- CSS
- JavaScript

### Controller
Handles REST API requests:
- AuthController
- UserController
- AIController
- HistoryController

### Service
Contains application/business logic:
- AuthService
- UserService
- AIService
- GeminiService
- HistoryService

### Repository
Communicates with MySQL using JPA:
- UserRepository
- StudyHistoryRepository

### DTO
Contains request objects:
- AuthRequest
- LoginRequest
- AIRequest

### Security
Contains:
- JwtService
- JwtFilter
- SecurityConfig

## Database

Create:

CREATE DATABASE smart_study;

Spring Boot creates/updates tables automatically because:

spring.jpa.hibernate.ddl-auto=update

## Configuration

Open:

backend/src/main/resources/application.properties

Change:

spring.datasource.password=YOUR_MYSQL_PASSWORD

app.jwt.secret=YOUR_LONG_SECRET

gemini.api-key=YOUR_GEMINI_API_KEY

Never put the Gemini key inside frontend JavaScript.

## Run Backend

CMD:

cd backend
mvn spring-boot:run

Backend:

http://localhost:8080

## Run Frontend

Use VS Code Live Server.

Open:

frontend/login.html

Expected:

http://localhost:5500/login.html

## Profile Picture

Profile picture is optional.

New users have:

profile_picture = NULL

Therefore the application displays the blank/default avatar.

Uploaded images are stored in:

backend/uploads/profile/

## API

POST /api/auth/signup
POST /api/auth/login

GET /api/users/me
PUT /api/users/me

POST /api/users/profile-picture
DELETE /api/users/profile-picture

POST /api/ai/explain
POST /api/ai/notes
POST /api/ai/quiz

GET /api/history
