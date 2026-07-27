package com.om.blog.controllers;

import com.om.blog.payloads.PostDto;
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

}
