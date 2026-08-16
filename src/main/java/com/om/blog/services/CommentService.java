package com.om.blog.services;

import com.om.blog.payloads.CommentDto;

import java.util.List;

public interface CommentService {

    CommentDto createComment(CommentDto commentDto, Integer postId);

    CommentDto updateComment(CommentDto commentDto, Integer commentId);

    void deleteComment(Integer commentId);

    List<CommentDto> getAllComment();

    CommentDto getCommentById(Integer commentId);

    //get comments particular post
    List<CommentDto> getCommentsByPost(Integer postId);
}
