# LifeLink: Blood Donor & Emergency Request Network

LifeLink is a real-time, geo-targeted web application for connecting blood donors with emergency requests. It replaces manual chat forwarding with a reliable matching system using PostGIS and Firebase Cloud Messaging (FCM).

## Tech Stack
- **Backend**: Java 21, Spring Boot 3.3.x
- **Database**: PostgreSQL 15+ with PostGIS
- **Caching**: Redis 7.x
- **Auth**: JWT (JSON Web Tokens)
- **Push Notifications**: Firebase Cloud Messaging (FCM)
- **Containerization**: Docker Compose

## Quick Start

### 1. Configure Environment Variables
Rename the `.env.example` file to `.env` or just export the variables manually:
```bash
cp .env.example .env
```
Provide the correct database credentials, JWT secret, and path to your Firebase service account JSON.

### 2. Start Infrastructure
Start the Postgres (with PostGIS) and Redis containers:
```bash
docker-compose up -d postgres redis
```
(You can also start the `app` container directly via docker-compose if you configure the Dockerfile, but for local development, it's easier to run Maven).

### 3. Run the Application
Make sure you have Java 21 installed.
```bash
./mvnw spring-boot:run
```
Flyway migrations will automatically run and seed the database.

## API Documentation
The API documentation is generated automatically using OpenAPI/Swagger. Once the application is running, navigate to:
[http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

## Architecture Overview
- **Eligibility**: Donors are automatically filtered out if they have donated recently based on component type rules (90 days for whole blood, 14 for platelets, 28 for plasma).
- **Matching Engine**: Uses PostGIS `ST_DWithin` to find donors within a specific radius (defaults to 5km). Automatically expands to 15km and 30km if a request goes unanswered.
- **Anonymity**: Donor contact information is heavily guarded and only revealed when a donor explicitly accepts an emergency request.
