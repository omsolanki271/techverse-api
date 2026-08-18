# TechVerse Backend Analysis & API Reference Guide

> **NOTICE FOR FUTURE ANTIGRAVITY TASKS**:  
> Whenever a future React/UI prompt is provided for this project, FIRST read `BACKEND_ANALYSIS.md`.  
> Use this file as the primary backend connectivity reference.  
> Do NOT re-analyze the entire backend project unless the requested UI functionality requires information that is missing from this file.  
> Continue from the existing backend APIs.  
> Do not create duplicate backend APIs unnecessarily.  
> Do not modify backend code unless the frontend integration actually requires a backend change.  
> If a backend change is required, clearly explain the reason before making the change.

---

## 1. Project Status

The TechVerse Spring Boot RESTful Blogging Platform backend is **Fully Completed, Built, Tested, and Deployment-Ready**.
- **Compilation**: `BUILD SUCCESS` (0 errors).
- **Test Suite**: Passed 100% (`BlogBackendApplicationTests`).
- **Database Status**: Connected and operational on MySQL `blog_app_api` (port `3307`).
- **Frontend Integration Status**: Ready for React / Vite / Next.js frontend connection.

---

## 2. Tech Stack

- **Core**: Java 21, Spring Boot 3.x / 4.x
- **Web**: Spring MVC RESTful Controllers
- **Security**: Spring Security, JWT (JSON Web Token), BCrypt Password Hashing
- **Database & Persistence**: MySQL 8.0, Spring Data JPA, Hibernate ORM
- **Payload Mapping**: ModelMapper, Lombok
- **Validation**: Jakarta Bean Validation (`@Valid`, `@NotEmpty`, `@Size`, `@Email`, etc.)

---

## 3. Server Information

- **Server Host**: `http://localhost`
- **Server Port**: `8081`
- **API Base URL**: `http://localhost:8081`
- **Database Engine**: MySQL 8.0
- **Database Name**: `blog_app_api`
- **Database Port**: `3307`
- **Database Connection String**: `jdbc:mysql://localhost:3307/blog_app_api`
- **Image Storage Location**: `images/` directory in backend root with user-isolated subfolders `images/user-{userId}/`
- **Default Post Image**: `default.png`

---

## 4. Database Schema & Entities

### Table: `users`
- **Primary Key**: `id` (`INT AUTO_INCREMENT`)
- **Columns**:
  - `id` (`INT`, PK)
  - `name` (`VARCHAR(100)`, NOT NULL)
  - `email` (`VARCHAR(255)`, NOT NULL, UNIQUE)
  - `password` (`VARCHAR(255)`, NOT NULL) — BCrypt hashed
  - `about` (`VARCHAR(255)`)
  - `mobile_number` (`VARCHAR(15)`, UNIQUE)
  - `address` (`VARCHAR(255)`)
  - `github_url` (`VARCHAR(255)`)
  - `linkedin_url` (`VARCHAR(255)`)
  - `insta_url` (`VARCHAR(255)`)
  - `created_at` (`DATETIME(6)`) — Auto-populated `@CreationTimestamp`
  - `updated_at` (`DATETIME(6)`) — Auto-populated `@UpdateTimestamp`
- **Relationships**:
  - Many-to-Many with `roles` via `user_roles`
  - One-to-Many with `posts`, `comments`, and `media`

### Table: `roles`
- **Primary Key**: `id` (`INT`)
- **Columns**: `id`, `name` (`VARCHAR(255)`)
- **Pre-populated Roles**:
  - `501`: `ROLE_ADMIN`
  - `502`: `ROLE_USER`

### Table: `user_roles` (Join Table)
- **Foreign Keys**: `user_id` -> `users.id`, `role_id` -> `roles.id`

### Table: `categories`
- **Primary Key**: `category_id` (`INT AUTO_INCREMENT`)
- **Columns**:
  - `category_id` (`INT`, PK)
  - `title` (`VARCHAR(100)`, NOT NULL)
  - `description` (`VARCHAR(255)`)
- **Relationships**: One-to-Many with `posts` (`cascade = CascadeType.ALL`, `orphanRemoval = true`)

### Table: `posts`
- **Primary Key**: `post_id` (`INT AUTO_INCREMENT`)
- **Columns**:
  - `post_id` (`INT`, PK)
  - `post_title` (`VARCHAR(100)`, NOT NULL)
  - `content` (`VARCHAR(10000)`, NOT NULL)
  - `image_name` (`VARCHAR(255)`, NOT NULL)
  - `added_date` (`DATETIME(6)`, NOT NULL) — Auto `@CurrentTimestamp`
  - `category_id` (`INT`, FK -> `categories.category_id`)
  - `user_id` (`INT`, FK -> `users.id`)
  - `media_id` (`INT`, FK -> `media.media_id`, NULLABLE)
- **Relationships**:
  - Many-to-One with `categories`, `users`, `media`
  - One-to-Many with `comments` (`cascade = CascadeType.ALL`)

### Table: `comments`
- **Primary Key**: `id` (`INT AUTO_INCREMENT`)
- **Columns**:
  - `id` (`INT`, PK)
  - `content` (`VARCHAR(1000)`, NOT NULL)
  - `post_id` (`INT`, FK -> `posts.post_id`, NOT NULL)
  - `user_id` (`INT`, FK -> `users.id`, NOT NULL)
- **Relationships**: Many-to-One with `posts` and `users`

### Table: `media`
- **Primary Key**: `media_id` (`INT AUTO_INCREMENT`)
- **Columns**:
  - `media_id` (`INT`, PK)
  - `file_name` (`VARCHAR(100)`, NOT NULL)
  - `file_type` (`VARCHAR(255)`, NOT NULL)
  - `file_path` (`VARCHAR(255)`, NOT NULL)
  - `uploaded_date` (`DATETIME(6)`, NOT NULL) — Auto `@CurrentTimestamp`
  - `user_id` (`INT`, FK -> `users.id`)
- **Relationships**: Many-to-One with `users`

---

## 5. Authentication & Security Model

### Authentication Endpoint
- **URL**: `POST /api/auth/login`
- **Request Body**:
  ```json
  {
    "email": "user@gmail.com",
    "password": "password123"
  }
  ```
- **Response (HTTP 200 OK)**:
  ```json
  {
    "token": "eyJhbGciOiJIUzI1NiJ9..."
  }
  ```

### Authorization Header Format
For all protected APIs, include the JWT token in the HTTP Authorization header:
```
Authorization: Bearer <your_jwt_token>
```

### Access Control & Security Rules
- **Public APIs** (No token needed):
  - `POST /api/auth/login`
  - `POST /api/users/` (User Registration)
  - `GET /api/categories/` & `GET /api/categories/{categoryId}`
  - `GET /api/posts/image/{postId}`
- **Admin-Only APIs** (`ROLE_ADMIN`):
  - `GET /api/users/` (Get all users)
  - `DELETE /api/users/{userId}` (Delete user)
  - `POST /api/categories/` (Create category)
  - `PUT /api/categories/{categoryId}` (Update category)
  - `DELETE /api/categories/{categoryId}` (Delete category)
- **User-Owner or Admin APIs**:
  - `GET /api/users/{userId}` & `PUT /api/users/{userId}` (Owner user or Admin)
  - `PUT /api/posts/{postId}` & `DELETE /api/posts/{postId}` (Post owner or Admin)
  - `PUT /api/comments/{commentId}` & `DELETE /api/comments/{commentId}` (Comment owner or Admin)
  - `DELETE /api/media/{mediaId}` (Media owner or Admin)
- **HTTP Status Errors**:
  - `401 Unauthorized`: Returned when Authorization token is missing or invalid.
  - `403 Forbidden`: Returned when user is authenticated but lacks required role or ownership.
  - `404 Not Found`: Returned when target resource (User, Post, Category, Comment, Media) does not exist.

---

## 6. User APIs

### 1. Register User (Public)
- **Method**: `POST`
- **Endpoint**: `/api/users/`
- **Auth**: None
- **Request Body** (`UserDto`):
  ```json
  {
    "name": "Om Solanki",
    "email": "om@gmail.com",
    "password": "password123",
    "about": "Full Stack Java Developer",
    "mobileNumber": "9876543210",
    "address": "Ahmedabad, Gujarat",
    "githubUrl": "https://github.com/omsolanki271",
    "linkedinUrl": "https://linkedin.com/in/omsolanki",
    "instaUrl": "https://instagram.com/omsolanki"
  }
  ```
- **Response (HTTP 201 Created)**: `UserDto` (without password).

### 2. Update User Profile (Owner or Admin)
- **Method**: `PUT`
- **Endpoint**: `/api/users/{userId}`
- **Auth**: Bearer Token
- **Request Body**: `UserDto`
- **Response (HTTP 200 OK)**: Updated `UserDto`.

### 3. Get User By ID (Owner or Admin)
- **Method**: `GET`
- **Endpoint**: `/api/users/{userId}`
- **Auth**: Bearer Token
- **Response (HTTP 200 OK)**: `UserDto`.

### 4. Get All Users (Admin Only)
- **Method**: `GET`
- **Endpoint**: `/api/users/`
- **Auth**: Bearer Token (`ROLE_ADMIN`)
- **Response (HTTP 200 OK)**: `List<UserDto>`.

### 5. Delete User (Admin Only)
- **Method**: `DELETE`
- **Endpoint**: `/api/users/{userId}`
- **Auth**: Bearer Token (`ROLE_ADMIN`)
- **Response (HTTP 200 OK)**: `{"message": "User deleted successfully", "success": true}`.

---

## 7. Category APIs

### 1. Create Category (Admin Only)
- **Method**: `POST`
- **Endpoint**: `/api/categories/`
- **Auth**: Bearer Token (`ROLE_ADMIN`)
- **Request Body** (`CategoryDto`):
  ```json
  {
    "categoryTitle": "Java & Spring Boot",
    "categoryDescription": "Tutorials and guides on backend development."
  }
  ```
- **Response (HTTP 201 Created)**: `CategoryDto`.

### 2. Update Category (Admin Only)
- **Method**: `PUT`
- **Endpoint**: `/api/categories/{categoryId}`
- **Auth**: Bearer Token (`ROLE_ADMIN`)
- **Response (HTTP 200 OK)**: `CategoryDto`.

### 3. Delete Category (Admin Only)
- **Method**: `DELETE`
- **Endpoint**: `/api/categories/{categoryId}`
- **Auth**: Bearer Token (`ROLE_ADMIN`)
- **Response (HTTP 200 OK)**: `{"message": "Category deleted Successfully.", "success": true}`.

### 4. Get Category By ID (Public / Authenticated)
- **Method**: `GET`
- **Endpoint**: `/api/categories/{categoryId}`
- **Response (HTTP 200 OK)**: `CategoryDto`.

### 5. Get All Categories (Public / Authenticated)
- **Method**: `GET`
- **Endpoint**: `/api/categories/`
- **Response (HTTP 200 OK)**: `List<CategoryDto>`.

---

## 8. Post APIs

### 1. Create Post
- **Method**: `POST`
- **Endpoint**: `/api/posts/category/{categoryId}/posts`
- **Auth**: Bearer Token
- **Request Body** (`PostDto`):
  ```json
  {
    "title": "Getting Started with Spring Boot 3",
    "content": "Spring Boot makes it easy to create stand-alone Spring applications...",
    "mediaId": 1
  }
  ```
  *(Note: `mediaId` is optional. If not provided, default image is used).*
- **Response (HTTP 201 Created)**: `PostDto`.

### 2. Get All Posts (Paginated & Sorted)
- **Method**: `GET`
- **Endpoint**: `/api/posts/?pageNumber=0&pageSize=5&sortBy=postId&sortDirection=desc`
- **Auth**: Bearer Token
- **Query Params** (Optional defaults):
  - `pageNumber` (default `0`)
  - `pageSize` (default `5`)
  - `sortBy` (default `postId`)
  - `sortDirection` (default `asc`)
- **Response (HTTP 200 OK)** (`PostResponse`):
  ```json
  {
    "content": [
      {
        "postId": 1,
        "title": "Getting Started with Spring Boot 3",
        "content": "...",
        "imageName": "default.png",
        "addedDate": "2026-08-18T22:00:00",
        "category": { "categoryId": 1, "categoryTitle": "Java", "categoryDescription": "..." },
        "user": { "id": 1, "name": "Om Solanki", "email": "om@gmail.com" },
        "comments": []
      }
    ],
    "pageNumber": 0,
    "pageSize": 5,
    "totalElements": 1,
    "totalPages": 1,
    "lastPage": true
  }
  ```

### 3. Get Post By ID
- **Method**: `GET`
- **Endpoint**: `/api/posts/{postId}`
- **Auth**: Bearer Token
- **Response (HTTP 200 OK)**: `PostDto`.

### 4. Update Post (Owner or Admin)
- **Method**: `PUT`
- **Endpoint**: `/api/posts/{postId}`
- **Auth**: Bearer Token
- **Request Body**: `PostDto`
- **Response (HTTP 200 OK)**: `PostDto`.

### 5. Delete Post (Owner or Admin)
- **Method**: `DELETE`
- **Endpoint**: `/api/posts/{postId}`
- **Auth**: Bearer Token
- **Response (HTTP 200 OK)**: `{"message": "Post deleted Successfully ", "success": true}`.

### 6. Get Posts By User
- **Method**: `GET`
- **Endpoint**: `/api/posts/user/{userId}/posts?pageNumber=0&pageSize=10&sortBy=postId&sortDirection=asc`
- **Auth**: Bearer Token
- **Response (HTTP 200 OK)**: `PostResponse`.

### 7. Get Posts By Category
- **Method**: `GET`
- **Endpoint**: `/api/posts/category/{categoryId}/posts?pageNumber=0&pageSize=10&sortBy=postId&sortDirection=asc`
- **Auth**: Bearer Token
- **Response (HTTP 200 OK)**: `PostResponse`.

### 8. Search Posts By Keyword
- **Method**: `GET`
- **Endpoint**: `/api/posts/search?keyword=spring`
- **Auth**: Bearer Token
- **Response (HTTP 200 OK)**: `List<PostDto>`.

### 9. Upload Post Image (Multipart)
- **Method**: `POST`
- **Endpoint**: `/api/posts/image/upload/{postId}`
- **Auth**: Bearer Token
- **Content-Type**: `multipart/form-data`
- **Form Data**: `image` (File: JPG, JPEG, PNG, GIF, WEBP)
- **Response (HTTP 200 OK)**: Updated `PostDto`.

### 10. Serve / Display Post Image (Public)
- **Method**: `GET`
- **Endpoint**: `/api/posts/image/{postId}`
- **Auth**: None
- **Response**: Image byte stream with appropriate `Content-Type` (`image/jpeg`, `image/png`, etc.).

---

## 9. Comment APIs

### 1. Create Comment
- **Method**: `POST`
- **Endpoint**: `/api/comments/post/{postId}`
- **Auth**: Bearer Token
- **Request Body** (`CommentDto`):
  ```json
  {
    "content": "Great article! Very helpful."
  }
  ```
- **Response (HTTP 201 Created)**: `CommentDto`.

### 2. Update Comment (Owner or Admin)
- **Method**: `PUT`
- **Endpoint**: `/api/comments/{commentId}`
- **Auth**: Bearer Token
- **Request Body**: `CommentDto`
- **Response (HTTP 200 OK)**: `CommentDto`.

### 3. Delete Comment (Owner or Admin)
- **Method**: `DELETE`
- **Endpoint**: `/api/comments/{commentId}`
- **Auth**: Bearer Token
- **Response (HTTP 200 OK)**: `{"message": "Comment delete successfully.. ", "success": true}`.

### 4. Get All Comments
- **Method**: `GET`
- **Endpoint**: `/api/comments/`
- **Auth**: Bearer Token
- **Response (HTTP 200 OK)**: `List<CommentDto>`.

### 5. Get Comment By ID
- **Method**: `GET`
- **Endpoint**: `/api/comments/{commentId}`
- **Auth**: Bearer Token
- **Response (HTTP 200 OK)**: `CommentDto`.

### 6. Get Comments By Post
- **Method**: `GET`
- **Endpoint**: `/api/comments/post/{postId}`
- **Auth**: Bearer Token
- **Response (HTTP 200 OK)**: `List<CommentDto>`.

---

## 10. Media APIs

### 1. Upload Media File
- **Method**: `POST`
- **Endpoint**: `/api/media/user/{userId}/upload`
- **Auth**: Bearer Token (Owner user or Admin)
- **Content-Type**: `multipart/form-data`
- **Form Data**: `image` (File: JPG, JPEG, PNG, GIF, WEBP)
- **Response (HTTP 201 Created)** (`MediaDto`):
  ```json
  {
    "mediaId": 1,
    "fileName": "a1b2c3d4.png",
    "fileType": "image/png",
    "filePath": "images/user-1",
    "uploadedDate": "2026-08-18T22:15:00",
    "user": { "id": 1, "name": "Om Solanki" }
  }
  ```

### 2. Get All Media (Admin vs User Filtering)
- **Method**: `GET`
- **Endpoint**: `/api/media/`
- **Auth**: Bearer Token
- **Behavior**: Admins get all media across all users; normal users get only their uploaded media.
- **Response (HTTP 200 OK)**: `List<MediaDto>`.

### 3. Get Media By ID
- **Method**: `GET`
- **Endpoint**: `/api/media/{mediaId}`
- **Auth**: Bearer Token
- **Response (HTTP 200 OK)**: `MediaDto`.

### 4. Get Media By User
- **Method**: `GET`
- **Endpoint**: `/api/media/user/{userId}`
- **Auth**: Bearer Token
- **Response (HTTP 200 OK)**: `List<MediaDto>`.

### 5. Delete Media (Owner or Admin)
- **Method**: `DELETE`
- **Endpoint**: `/api/media/{mediaId}`
- **Auth**: Bearer Token
- **Special Protection Rule**: If media is assigned to an active Post, backend throws `ResourceAlreadyInUseException` ("Cannot delete media because it is assigned to an existing post.").
- **Response (HTTP 200 OK)**: `{"message": "Media deleted successfully", "success": true}`.

---

## 11. Frontend Integration Rules for React Developers

1. **Base URL**: Set Axios / Fetch base URL to `http://localhost:8081`.
2. **Authentication Flow**:
   - Call `POST /api/auth/login` with email and password.
   - Save the returned `token` in `localStorage` or `sessionStorage`.
   - Attach token to all subsequent requests via HTTP Header: `Authorization: Bearer ${token}`.
3. **CORS Handling**: CORS is enabled on the backend for all origins (`*`) and methods (`GET`, `POST`, `PUT`, `DELETE`, `OPTIONS`).
4. **Multipart File Uploads**:
   - For `/api/posts/image/upload/{postId}` and `/api/media/user/{userId}/upload`, use `FormData` object with field key `image`.
   - Do NOT set manual `Content-Type: application/json` header for file uploads; let Axios/browser set `multipart/form-data; boundary=...`.
5. **Displaying Post Images**:
   - Image URL pattern: `http://localhost:8081/api/posts/image/${postId}`.
   - Use this URL directly in React `<img src={`http://localhost:8081/api/posts/image/${post.postId}`} />`.
6. **Error Response Handling**:
   - **Validation Error (HTTP 400 Bad Request)**: Returns map of field name -> error message:
     ```json
     {
       "email": "Email is invalid",
       "password": "Password must be minimum of 3 characters"
     }
     ```
   - **Resource Not Found (HTTP 404)**:
     ```json
     {
       "message": "User not found with User Id : 99",
       "success": false
     }
     ```
   - **Resource Already In Use (HTTP 400)**:
     ```json
     {
       "message": "User already exists with Email : om@gmail.com",
       "success": false
     }
     ```
   - **Forbidden Access (HTTP 403)**:
     ```json
     {
       "message": "You are not allowed to modify this post",
       "success": false
     }
     ```

---

## 12. Verified Functionality Checklist

- [x] JWT Authentication & Token Generation (`/api/auth/login`)
- [x] User Registration & Signup (`POST /api/users/`)
- [x] User Profile CRUD & Enhanced Fields (`mobileNumber`, `address`, `githubUrl`, `linkedinUrl`, `instaUrl`, `createdAt`, `updatedAt`)
- [x] Role-Based Access Control (`ROLE_ADMIN`, `ROLE_USER`)
- [x] Category CRUD (Admin management, Public read)
- [x] Post CRUD with Pagination, Sorting, Search, User/Category filters
- [x] Comment CRUD with Post linking & Owner/Admin security
- [x] Media Upload & User Folder Isolation (`images/user-{userId}/`)
- [x] Media Deletion Protection for active post media
- [x] Image Serving Endpoint (`GET /api/posts/image/{postId}`)
- [x] CORS Enabled for React Frontend Apps

---

## 13. API Reference Table

| Method | Endpoint | Purpose | Auth Required | Required Role / Rule |
| :--- | :--- | :--- | :--- | :--- |
| **POST** | `/api/auth/login` | Authenticate user & get JWT token | No | Public |
| **POST** | `/api/users/` | Register new user | No | Public |
| **GET** | `/api/users/{userId}` | Get user details | Yes | Owner or Admin |
| **PUT** | `/api/users/{userId}` | Update user profile | Yes | Owner or Admin |
| **GET** | `/api/users/` | Get list of all users | Yes | Admin Only |
| **DELETE** | `/api/users/{userId}` | Delete user | Yes | Admin Only |
| **GET** | `/api/categories/` | Get all categories | No | Public |
| **GET** | `/api/categories/{categoryId}` | Get category by ID | No | Public |
| **POST** | `/api/categories/` | Create new category | Yes | Admin Only |
| **PUT** | `/api/categories/{categoryId}` | Update category | Yes | Admin Only |
| **DELETE** | `/api/categories/{categoryId}` | Delete category | Yes | Admin Only |
| **POST** | `/api/posts/category/{categoryId}/posts` | Create post in category | Yes | Authenticated User |
| **GET** | `/api/posts/` | Get paginated/sorted posts | Yes | Authenticated User |
| **GET** | `/api/posts/{postId}` | Get post details by ID | Yes | Authenticated User |
| **PUT** | `/api/posts/{postId}` | Update post | Yes | Post Owner or Admin |
| **DELETE** | `/api/posts/{postId}` | Delete post | Yes | Post Owner or Admin |
| **GET** | `/api/posts/user/{userId}/posts` | Get posts created by user | Yes | Authenticated User |
| **GET** | `/api/posts/category/{categoryId}/posts` | Get posts in category | Yes | Authenticated User |
| **GET** | `/api/posts/search?keyword={kw}` | Search posts by keyword | Yes | Authenticated User |
| **POST** | `/api/posts/image/upload/{postId}` | Upload image for post | Yes | Post Owner or Admin |
| **GET** | `/api/posts/image/{postId}` | Serve post image stream | No | Public |
| **POST** | `/api/comments/post/{postId}` | Add comment to post | Yes | Authenticated User |
| **GET** | `/api/comments/post/{postId}` | Get comments for post | Yes | Authenticated User |
| **PUT** | `/api/comments/{commentId}` | Update comment | Yes | Comment Owner or Admin |
| **DELETE** | `/api/comments/{commentId}` | Delete comment | Yes | Comment Owner or Admin |
| **GET** | `/api/comments/` | Get all comments | Yes | Authenticated User |
| **GET** | `/api/comments/{commentId}` | Get comment by ID | Yes | Authenticated User |
| **POST** | `/api/media/user/{userId}/upload` | Upload media file | Yes | Owner User or Admin |
| **GET** | `/api/media/` | Get all media | Yes | Admin (All) / User (Own) |
| **GET** | `/api/media/{mediaId}` | Get media metadata by ID | Yes | Authenticated User |
| **GET** | `/api/media/user/{userId}` | Get media by user ID | Yes | Authenticated User |
| **DELETE** | `/api/media/{mediaId}` | Delete media file | Yes | Media Owner or Admin |
