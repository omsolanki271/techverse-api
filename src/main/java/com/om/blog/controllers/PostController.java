package com.om.blog.controllers;

import com.om.blog.payloads.ApiResponse;
import com.om.blog.payloads.PostDto;
import com.om.blog.payloads.PostResponse;
import com.om.blog.services.PostService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


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

    // Get all posts
    @GetMapping("/")
    public ResponseEntity<PostResponse> getAllPosts(
            @RequestParam(value = "pageNumber" , defaultValue = "0", required = false) Integer pageNumber,
            @RequestParam(value = "pageSize" , defaultValue = "10", required = false) Integer pageSize,
            @RequestParam(value = "sortBy" , defaultValue = "postId" , required = false) String sortBy,
            @RequestParam(value = "sortDirection" , defaultValue = "asc" , required = false) String sortDirection
    )
    {
        PostResponse postResponse = postService.getAllPosts(pageNumber,pageSize,sortBy,sortDirection);
        return new ResponseEntity<>(postResponse, HttpStatus.OK);
    }

    // Get post by id
    @GetMapping("/{postId}")
    public ResponseEntity<PostDto> getPostById(@PathVariable Integer postId) {

        return ResponseEntity.ok(postService.getPostById(postId));
    }

    @PutMapping("/{postId}")
    public ResponseEntity<PostDto> updatePost(
            @Valid @RequestBody PostDto postDto,
            @PathVariable Integer postId) {

        PostDto updatedPost = postService.updatePost(postDto, postId);

        return ResponseEntity.ok(updatedPost);
    }

    //delete
    @DeleteMapping("/{postId}")
    public ResponseEntity<ApiResponse> deletePost(@PathVariable Integer postId )
    {
        postService.deletePost(postId);
        return  new ResponseEntity<>(new ApiResponse("Post deleted Successfully ", true),HttpStatus.OK );
    }


    // get post by User
    @GetMapping("/user/{userId}/posts")
    public ResponseEntity<PostResponse> getPostsByUser(
            @PathVariable Integer userId,
            @RequestParam(value = "pageNumber" , defaultValue = "0", required = false) Integer pageNumber,
            @RequestParam(value = "pageSize" , defaultValue = "10", required = false) Integer pageSize,
            @RequestParam(value = "sortBy" , defaultValue = "postId" , required = false) String sortBy,
            @RequestParam(value = "sortDirection" , defaultValue = "asc" , required = false) String sortDirection
            ) {

        PostResponse postByUser = postService.getPostByUser(userId, pageNumber, pageSize, sortBy, sortDirection);
        return new ResponseEntity<>(postByUser ,HttpStatus.OK);
    }

    // Get posts by category
    @GetMapping("/category/{categoryId}/posts")
    public ResponseEntity<PostResponse> getPostsByCategory(
            @PathVariable Integer categoryId,
            @RequestParam(value = "pageNumber" , defaultValue = "0" , required = false) Integer pageNumber,
            @RequestParam(value = "pageSize" , defaultValue = "10" , required = false) Integer pageSize,
            @RequestParam(value = "sortBy" , defaultValue = "postId" , required = false) String sortBy,
            @RequestParam(value = "sortDirection" , defaultValue = "asc" , required = false) String sortDirection
            ) {
        PostResponse postByCategory = postService.getPostByCategory(categoryId, pageNumber, pageSize,sortBy,sortDirection);
        return new ResponseEntity<>(postByCategory, HttpStatus.OK);
    }
}
