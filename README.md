# Spotify Mock Backend (Spring Boot)

A RESTful backend API for a Spotify-style music streaming platform built with **Java, Spring Boot, Spring Security, and MongoDB**.
The system supports authentication, music catalog management, playlists, user interactions, and administrative analytics.

---

## Overview

This backend provides the core functionality required for a music streaming platform, including:

* User registration and authentication with **JWT**
* Role-based authorization (**USER / ADMIN**)
* CRUD APIs for songs, albums, artists, and genres
* Playlist creation and song management
* User interactions such as likes and follows
* Admin dashboard endpoints for platform statistics

---

## Tech Stack

* Java 21
* Spring Boot
* Spring Web
* Spring Security
* Spring Data MongoDB
* JWT (jjwt)
* Maven
* Lombok

---

## Architecture

The project follows a **layered architecture**:

Controller → Service → Repository → MongoDB

**Controller Layer**
Handles HTTP requests and responses.

**Service Layer**
Contains business logic and data enrichment.

**Repository Layer**
Manages database access using Spring Data MongoDB.

**Model Layer**
Defines MongoDB document mappings.

Security flow:

Request → JWT Filter → Security Context → Authorized Controller

---

## Project Structure

```
src/main/java/com/spotify/backend
├── config
├── controller
├── service
├── repository
├── model
└── SpotifyBackendApplication
```

Main modules include:

* **AuthController** – login and registration
* **UserController** – user management
* **SongController** – music catalog APIs
* **PlaylistController** – playlist operations
* **AdminController** – platform analytics

---

## Security

Authentication is implemented using **JWT Bearer tokens**.

Authentication flow:

1. User submits credentials to `/api/auth/login`
2. Backend validates credentials
3. A JWT token is generated and returned
4. Client sends token in requests:

```
Authorization: Bearer <token>
```

Role-based access:

* `USER`
* `ADMIN`

Admin endpoints require admin privileges.

---

## Running the Project

### Prerequisites

* JDK 21
* Maven
* MongoDB (local or remote)

### Environment Variable

Set the MongoDB connection URI:

```bash
MONGO_URL=mongodb://localhost:27017/spotify_mock
```

### Start the Application

```
./mvnw spring-boot:run
```

Windows:

```
mvnw.cmd spring-boot:run
```

### Build the Project

```
./mvnw clean package
```

### Run Tests

```
./mvnw test
```

The server runs on:

```
http://localhost:8082
```

---

## API Modules

### Authentication

* `POST /api/auth/login`
* `POST /api/auth/register`
* `GET /api/auth/validate`
* `GET /api/auth/me`

### Songs

* `GET /api/songs`
* `GET /api/songs/{id}`
* `GET /api/songs/genre/{genre}`
* `POST /api/songs`
* `PUT /api/songs/{id}`
* `DELETE /api/songs/{id}`

### Albums

* `GET /api/albums`
* `POST /api/albums`
* `PUT /api/albums/{id}`
* `DELETE /api/albums/{id}`

### Artists

* `GET /api/artists`
* `POST /api/artists`
* `PUT /api/artists/{id}`
* `DELETE /api/artists/{id}`

### Playlists

* `POST /api/playlists`
* `GET /api/playlists`
* `PUT /api/playlists/{id}`
* `DELETE /api/playlists/{id}`

### Admin

* `GET /api/admin/stats`
* `GET /api/admin/users`
* `POST /api/admin/users/{id}/ban`

---

## Database Collections

MongoDB collections used by the system:

* users
* songs
* albums
* artists
* genres
* playlists
* user_likes
* user_follows
* admin_logs

---

## Example Login Request

```
POST /api/auth/login
```

```json
{
  "username": "admin@example.com",
  "password": "your-password"
}
```

Response:

```json
{
  "token": "jwt_token_here",
  "role": "ADMIN",
  "message": "Login successful"
}
```

---

## Future Improvements

* Swagger / OpenAPI documentation
* Refresh token support
* Request validation DTOs
* Integration tests
* Seed data initializer

---

## License

This project was built for educational and portfolio purposes.
