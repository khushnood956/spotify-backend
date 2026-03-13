# Spotify Mock Backend API

A RESTful backend API for a Spotify-like music streaming application built with Node.js and Express. The system supports user authentication, song browsing, playlist management, and basic admin analytics using a MongoDB database.

## Tech Stack

* Node.js
* Express.js
* MongoDB (Atlas)
* express-session (session authentication)
* bcryptjs (password hashing)
* dotenv (environment configuration)
* cors

## Features

* User registration and login with hashed passwords
* Session-based authentication and protected routes
* Browse songs and retrieve song details
* Play songs and track play counts
* Create and manage user playlists
* Admin statistics dashboard for system analytics

## Project Structure

```
backend/
├── server.js            # Application entry point
├── package.json         # Project dependencies
├── seedData.js          # Database seeding script
│
├── middleware/
│   └── auth.js          # Authentication / authorization middleware
│
├── routes/
│   ├── auth.js          # Authentication endpoints
│   ├── songs.js         # Song browsing and play tracking
│   ├── playlists.js     # Playlist management
│   └── admin.js         # Admin statistics routes
│
└── utils/
    └── dbConnection.js  # MongoDB connection
```

## API Endpoints

### Authentication

| Method | Endpoint             | Description               |
| ------ | -------------------- | ------------------------- |
| POST   | `/api/auth/register` | Register new user         |
| POST   | `/api/auth/login`    | Login with username/email |
| POST   | `/api/auth/logout`   | Logout user               |
| GET    | `/api/auth/me`       | Get current session user  |

### Songs

| Method | Endpoint              | Description             |
| ------ | --------------------- | ----------------------- |
| GET    | `/api/songs`          | Get all songs           |
| GET    | `/api/songs/:id`      | Get single song details |
| POST   | `/api/songs/:id/play` | Increment play count    |

### Playlists

| Method | Endpoint         | Description         |
| ------ | ---------------- | ------------------- |
| GET    | `/api/playlists` | Get user playlists  |
| POST   | `/api/playlists` | Create new playlist |

### Admin

| Method | Endpoint           | Description                    |
| ------ | ------------------ | ------------------------------ |
| GET    | `/api/admin/stats` | System statistics (admin only) |

## Environment Variables

Create a `.env` file in the backend directory.

```
MONGODB_URI=your_mongodb_connection_string
SESSION_SECRET=your_secret_key
PORT=3000
```

## Installation

```bash
git clone https://github.com/yourusername/spotify-backend.git
cd spotify-backend/backend
npm install
```

Run the server:

```bash
npm start
```

Seed the database with sample data:

```bash
npm run seed
```

## Authentication

The application uses **session-based authentication**. After login, a session cookie (`connect.sid`) is stored in the client and used to access protected routes.

## Database Collections

* users
* artists
* albums
* songs
* genres
* playlists
* playlist_songs

## Purpose

This project was built as a backend learning project to demonstrate REST API design, authentication, database integration, and modular backend architecture using Node.js and MongoDB.
