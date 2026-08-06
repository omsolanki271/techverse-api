package com.om.blog.controllers;

import com.om.blog.entities.Post;
import com.om.blog.payloads.ApiResponse;
import com.om.blog.payloads.PostDto;
import com.om.blog.services.PostService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    @Autowired
    private PostService postService;

    //create
    @PostMapping("/user/{userId}/category/{categoryId}/posts")
    public ResponseEntity<PostDto> createPost(@Valid @RequestBody PostDto postDto , @PathVariable Integer userId , @PathVariable Integer categoryId )
    {
        PostDto createPost = postService.createPost(postDto, userId, categoryId);
        return  new ResponseEntity<>(createPost , HttpStatus.CREATED);
    }

    //delete
    @DeleteMapping("/{postId}")
    public ResponseEntity<ApiResponse> deletePost(@PathVariable Integer postId )
    {
        postService.deletePost(postId);
        return  new ResponseEntity<>(new ApiResponse("Post deleted Successfully ", true),HttpStatus.OK );
    }

    // Get post by id
    @GetMapping("/{postId}")
    public ResponseEntity<PostDto> getPostById(@PathVariable Integer postId) {

        return ResponseEntity.ok(postService.getPostById(postId));
    }

    // Get posts
    @GetMapping("/")
    public ResponseEntity<List<PostDto>> getAllPosts()
    {
        final List<PostDto> allPost = postService.getAllPost();
        return new ResponseEntity<>(allPost, HttpStatus.OK);
    }

    // get post by User
    @GetMapping("/user/{userId}/posts")
    public ResponseEntity<List<PostDto>> getPostsByUser(
            @PathVariable Integer userId) {

        List<PostDto> posts = postService.getPostByUser(userId);
        return new ResponseEntity<>(posts ,HttpStatus.OK);
    }

    // Get posts by category
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<PostDto>> getPostsByCategory(
            @PathVariable Integer categoryId) {
        List<PostDto> posts = postService.getPostByCategory(categoryId);
        return new ResponseEntity<>(posts, HttpStatus.OK);
    }

    @PutMapping("/{postId}")
    public ResponseEntity<PostDto> updatePost(
            @Valid @RequestBody PostDto postDto,
            @PathVariable Integer postId) {

        PostDto updatedPost = postService.updatePost(postDto, postId);

        return ResponseEntity.ok(updatedPost);
    }
}
