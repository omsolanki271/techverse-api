package com.om.blog.controllers;

import com.om.blog.payloads.ApiResponse;
import com.om.blog.payloads.CommentDto;
import com.om.blog.services.CommentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comments")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @PostMapping("/user/{userId}/post/{postId}")
    public ResponseEntity<CommentDto> createComment(
            @Valid @RequestBody CommentDto commentDto ,
            @PathVariable Integer userId,
            @PathVariable Integer postId
    )
    {
        CommentDto comment = commentService.createComment(commentDto, postId, userId);
        return  new ResponseEntity<>(comment, HttpStatus.CREATED);
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<ApiResponse> deleteComment(
            @PathVariable Integer commentId
    )
    {
        commentService.deleteComment(commentId);
        return  new ResponseEntity<>(new ApiResponse("Comment delete successfully.. ", true),HttpStatus.OK);
    }

    @GetMapping("/")
    public ResponseEntity<List<CommentDto>> getAllComment()
    {
        return  new ResponseEntity<>(commentService.getAllComment(),HttpStatus.OK);
    }
}
