package com.om.blog.controllers;

import com.om.blog.payloads.ApiResponse;
import com.om.blog.payloads.PostDto;
import com.om.blog.payloads.PostImageResponse;
import com.om.blog.payloads.PostResponse;
import com.om.blog.services.PostService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;

    // Constructor Injection
    public PostController(PostService postService) {
        this.postService = postService;
    }

    // Create Post
    @PostMapping("/category/{categoryId}/posts")
    public ResponseEntity<PostDto> createPost(
            @Valid @RequestBody PostDto postDto,
            @PathVariable Integer categoryId) {

        PostDto createdPost = postService.createPost(postDto, categoryId);

        return new ResponseEntity<>(createdPost, HttpStatus.CREATED);
    }

    // Get All Published Posts
    @GetMapping({"", "/"})
    public ResponseEntity<PostResponse> getAllPosts(
            @RequestParam(value = "pageNumber", defaultValue = "0", required = false) Integer pageNumber,
            @RequestParam(value = "pageSize", defaultValue = "10", required = false) Integer pageSize,
            @RequestParam(value = "sortBy", defaultValue = "postId", required = false) String sortBy,
            @RequestParam(value = "sortDirection", defaultValue = "asc", required = false) String sortDirection) {

        PostResponse postResponse = postService.getAllPosts(
                pageNumber, pageSize, sortBy, sortDirection);

        return new ResponseEntity<>(postResponse, HttpStatus.OK);
    }

    // Get Post By ID
    @GetMapping("/{postId}")
    public ResponseEntity<PostDto> getPostById(
            @PathVariable Integer postId) {

        return ResponseEntity.ok(postService.getPostById(postId));
    }

    // Update Post
    @PutMapping("/{postId}")
    public ResponseEntity<PostDto> updatePost(
            @Valid @RequestBody PostDto postDto,
            @PathVariable Integer postId) {

        PostDto updatedPost = postService.updatePost(postDto, postId);

        return ResponseEntity.ok(updatedPost);
    }

    // Delete Post
    @DeleteMapping("/{postId}")
    public ResponseEntity<ApiResponse> deletePost(
            @PathVariable Integer postId) {

        postService.deletePost(postId);

        return new ResponseEntity<>(
                new ApiResponse("Post deleted successfully", true),
                HttpStatus.OK
        );
    }

    // Get Posts By User
    @GetMapping("/user/{userId}/posts")
    public ResponseEntity<PostResponse> getPostsByUser(
            @PathVariable Integer userId,
            @RequestParam(value = "pageNumber", defaultValue = "0", required = false) Integer pageNumber,
            @RequestParam(value = "pageSize", defaultValue = "10", required = false) Integer pageSize,
            @RequestParam(value = "sortBy", defaultValue = "postId", required = false) String sortBy,
            @RequestParam(value = "sortDirection", defaultValue = "asc", required = false) String sortDirection) {

        PostResponse postByUser = postService.getPostByUser(
                userId, pageNumber, pageSize, sortBy, sortDirection);

        return new ResponseEntity<>(postByUser, HttpStatus.OK);
    }

    // Get Posts By Category
    @GetMapping("/category/{categoryId}/posts")
    public ResponseEntity<PostResponse> getPostsByCategory(
            @PathVariable Integer categoryId,
            @RequestParam(value = "pageNumber", defaultValue = "0", required = false) Integer pageNumber,
            @RequestParam(value = "pageSize", defaultValue = "10", required = false) Integer pageSize,
            @RequestParam(value = "sortBy", defaultValue = "postId", required = false) String sortBy,
            @RequestParam(value = "sortDirection", defaultValue = "asc", required = false) String sortDirection) {

        PostResponse postByCategory = postService.getPostByCategory(
                categoryId, pageNumber, pageSize, sortBy, sortDirection);

        return new ResponseEntity<>(postByCategory, HttpStatus.OK);
    }

    // Search Posts
    @GetMapping("/search")
    public ResponseEntity<List<PostDto>> searchPostByTitle(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer categoryId) {

        List<PostDto> postDto = postService.searchPosts(keyword, categoryId);

        return new ResponseEntity<>(postDto, HttpStatus.OK);
    }

    // Feature 1 & 7: Toggle Like
    @PostMapping("/{postId}/like")
    public ResponseEntity<Map<String, Object>> toggleLike(@PathVariable Integer postId) {
        return ResponseEntity.ok(postService.toggleLike(postId));
    }

    // Get Like Status & Count
    @GetMapping("/{postId}/like-status")
    public ResponseEntity<Map<String, Object>> getLikeStatus(@PathVariable Integer postId) {
        return ResponseEntity.ok(postService.getLikeStatus(postId));
    }

    // Feature 4 & 8: Toggle Bookmark
    @PostMapping("/{postId}/bookmark")
    public ResponseEntity<Map<String, Object>> toggleBookmark(@PathVariable Integer postId) {
        return ResponseEntity.ok(postService.toggleBookmark(postId));
    }

    // Get Bookmark Status & Count
    @GetMapping("/{postId}/bookmark-status")
    public ResponseEntity<Map<String, Object>> getBookmarkStatus(@PathVariable Integer postId) {
        return ResponseEntity.ok(postService.getBookmarkStatus(postId));
    }

    // Get User's Bookmarked Posts
    @GetMapping("/bookmarks/my-bookmarks")
    public ResponseEntity<PostResponse> getMyBookmarks(
            @RequestParam(value = "pageNumber", defaultValue = "0", required = false) Integer pageNumber,
            @RequestParam(value = "pageSize", defaultValue = "10", required = false) Integer pageSize,
            @RequestParam(value = "sortBy", defaultValue = "postId", required = false) String sortBy,
            @RequestParam(value = "sortDirection", defaultValue = "desc", required = false) String sortDirection) {

        return ResponseEntity.ok(postService.getMyBookmarks(pageNumber, pageSize, sortBy, sortDirection));
    }

    // Feature 5: Share counter
    @PostMapping("/{postId}/share")
    public ResponseEntity<Map<String, Object>> incrementShare(@PathVariable Integer postId) {
        return ResponseEntity.ok(postService.incrementShareCount(postId));
    }

    // Feature 9 & 10: Publish Draft
    @PutMapping("/{postId}/publish")
    public ResponseEntity<PostDto> publishPost(@PathVariable Integer postId) {
        return ResponseEntity.ok(postService.publishPost(postId));
    }

    // Get Authenticated User's Drafts
    @GetMapping("/drafts/my-drafts")
    public ResponseEntity<PostResponse> getMyDrafts(
            @RequestParam(value = "pageNumber", defaultValue = "0", required = false) Integer pageNumber,
            @RequestParam(value = "pageSize", defaultValue = "10", required = false) Integer pageSize,
            @RequestParam(value = "sortBy", defaultValue = "postId", required = false) String sortBy,
            @RequestParam(value = "sortDirection", defaultValue = "desc", required = false) String sortDirection) {

        return ResponseEntity.ok(postService.getMyDrafts(pageNumber, pageSize, sortBy, sortDirection));
    }

    // Get Post Counts (draft vs published)
    @GetMapping("/user/{userId}/counts")
    public ResponseEntity<Map<String, Object>> getUserPostCounts(@PathVariable Integer userId) {
        return ResponseEntity.ok(postService.getUserPostCounts(userId));
    }

    // Upload Post Image
    @PostMapping("/image/upload/{postId}")
    public ResponseEntity<?> uploadPostImage(
            @RequestParam(value = "image", required = false) MultipartFile image,
            @PathVariable Integer postId) {

        try {
            PostDto updatedPost = postService.uploadPostImage(postId, image);

            return ResponseEntity.ok(updatedPost);

        } catch (IOException e) {
            return ResponseEntity.badRequest().body(
                    new ApiResponse(e.getMessage(), false)
            );
        }
    }

    // Get / Display Post Image
    @GetMapping("/image/{postId}")
    public ResponseEntity<?> downloadImage(
            @PathVariable Integer postId,
            HttpServletResponse response) throws IOException {

        PostDto post = postService.getPostById(postId);

        // If post has a Cloudinary URL, redirect directly
        if (post.getCloudinaryUrl() != null && !post.getCloudinaryUrl().isBlank()) {
            response.sendRedirect(post.getCloudinaryUrl());
            return null;
        }

        // Backward compatibility for legacy local images
        PostImageResponse image = postService.getPostImage(postId);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(image.contentType()))
                .body(image.imageData());
    }
}
