# TechVerse — Spring Boot RESTful Blogging Platform

TechVerse is a secure RESTful backend API for a modern blogging platform built with Java and Spring Boot.

It provides user management, JWT-based authentication, role-based authorization, categories, posts, comments, media management, and MySQL integration.

---

## Key Features

- JWT-based authentication
- BCrypt password encryption
- Role-Based Access Control (`ROLE_ADMIN`, `ROLE_USER`)
- User profile management with extended fields
- Category management
- Blog post CRUD with pagination, sorting, and search
- Comment management with owner/admin authorization
- Media upload with user-specific folders
- Global exception handling and validation
- MySQL database integration

---

## Technology Stack

- Java 21
- Spring Boot
- Spring MVC
- Spring Security
- Spring Data JPA
- Hibernate
- JWT
- BCrypt
- MySQL 8
- Maven
- Lombok
- ModelMapper
- Jakarta Validation

---

## Project Structure

```text
src/main/java/com/om/blog/

├── config/
├── controllers/
├── entities/
├── exceptions/
├── payloads/
├── repositories/
├── security/
└── services/
    └── impl/
```

---

## Database

* **Database:** `blog_app_api`
* **Database Engine:** MySQL 8
* **MySQL Port:** `3307`
* **Application Port:** `8081`

### Main Tables

* `users` → User accounts and profiles
* `roles` → `ROLE_ADMIN`, `ROLE_USER`
* `user_roles` → User-role relationship
* `categories` → Blog categories
* `posts` → Blog posts
* `comments` → Comments linked to posts and users
* `media` → Uploaded media information

---

## API Endpoints

### Authentication

* `POST /api/auth/login` → User login and JWT generation

### User APIs

* `POST /api/users/` → Create user
* `GET /api/users/` → Get all users (Admin)
* `GET /api/users/{userId}` → Get user by ID (Admin/Owner)
* `PUT /api/users/{userId}` → Update user
* `DELETE /api/users/{userId}` → Delete user (Admin)

### Category APIs

* `POST /api/categories/` → Create category (Admin)
* `PUT /api/categories/{categoryId}` → Update category (Admin)
* `DELETE /api/categories/{categoryId}` → Delete category (Admin)
* `GET /api/categories/` → Get all categories
* `GET /api/categories/{categoryId}` → Get category by ID

### Post APIs

* `POST /api/posts/category/{categoryId}/posts` → Create post
* `GET /api/posts/` → Get all posts with pagination and sorting
* `GET /api/posts/{postId}` → Get post by ID
* `PUT /api/posts/{postId}` → Update post (Owner/Admin)
* `DELETE /api/posts/{postId}` → Delete post (Owner/Admin)
* `GET /api/posts/user/{userId}/posts` → Get posts by user
* `GET /api/posts/category/{categoryId}/posts` → Get posts by category
* `GET /api/posts/search?keyword={keyword}` → Search posts
* `POST /api/posts/image/upload/{postId}` → Upload post image
* `GET /api/posts/image/{postId}` → Get post image

### Comment APIs

* `POST /api/comments/post/{postId}` → Create comment
* `PUT /api/comments/{commentId}` → Update comment (Owner/Admin)
* `DELETE /api/comments/{commentId}` → Delete comment (Owner/Admin)
* `GET /api/comments/` → Get all comments
* `GET /api/comments/{commentId}` → Get comment by ID
* `GET /api/comments/post/{postId}` → Get comments by post

### Media APIs

* `POST /api/media/user/{userId}/upload` → Upload media
* `GET /api/media/` → Get media
* `GET /api/media/{mediaId}` → Get media by ID
* `GET /api/media/user/{userId}` → Get media by user
* `DELETE /api/media/{mediaId}` → Delete media

Media deletion is prevented when the media is currently being used by a post.

---

## Authentication

Protected APIs use JWT authentication.

Include the token in the request header:

```text
Authorization: Bearer <JWT_TOKEN>
```

The backend uses:

* `ROLE_ADMIN`
* `ROLE_USER`

Ownership-based authorization is applied to protected User, Post, Comment, and Media operations.

---

## Media Storage

Uploaded media is stored in user-specific folders:

```text
images/
├── user-2/
├── user-12/
└── ...
```

This keeps uploaded files separated by user.

---

## Run Project

### Prerequisites

* Java 21
* MySQL 8
* Maven

### Database

Create/use:

```text
blog_app_api
```

MySQL runs on:

```text
localhost:3307
```

### Build

```bash
mvn clean install
```

### Run

```bash
mvn spring-boot:run
```

Backend runs at:

```text
http://localhost:8081
```

---

## Frontend Integration

The backend provides REST APIs for integration with a React frontend.

Base URL:

```text
http://localhost:8081
```

Authenticated requests use:

```text
Authorization: Bearer <JWT_TOKEN>
```

---

## Project Purpose

TechVerse was developed as an MCA college project to demonstrate a secure, layered RESTful blogging backend using Java, Spring Boot, Spring Security, JWT, JPA/Hibernate, and MySQL.

---

## Developed By

**Om Solanki**

MCA Student | Java Backend Developer