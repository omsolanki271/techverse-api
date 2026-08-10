package com.om.blog.controllers;

import com.om.blog.entities.Comment;
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

    @PutMapping("/{commentId}")
    public ResponseEntity<CommentDto> updateComment(
            @Valid @RequestBody CommentDto commentDto,
            @PathVariable Integer commentId
            )
    {
        CommentDto updateComment = commentService.updateComment(commentDto, commentId);
        return new ResponseEntity<>(updateComment,HttpStatus.OK);
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

    @GetMapping("/{commentId}")
    public ResponseEntity<CommentDto> getCommentById(
            @PathVariable Integer commentId
    )
    {
        return  new ResponseEntity<>(commentService.getCommentById(commentId),HttpStatus.OK);
    }

    // Get comments by post
    @GetMapping("/post/{postId}")
    public ResponseEntity<List<CommentDto>> getCommentsByPost(
            @PathVariable Integer postId
    ) {
        List<CommentDto> comments = commentService.getCommentsByPost(postId);
        return new ResponseEntity<>(comments, HttpStatus.OK);
    }
}
