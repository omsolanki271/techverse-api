package com.om.blog.services.impl;

import com.om.blog.entities.Comment;
import com.om.blog.entities.Post;
import com.om.blog.entities.User;
import com.om.blog.exceptions.ResourceNotFoundException;
import com.om.blog.payloads.CommentDto;
import com.om.blog.repositories.CommentRepo;
import com.om.blog.repositories.PostRepo;
import com.om.blog.repositories.UserRepo;
import com.om.blog.services.CommentService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CommentServiceImpl implements CommentService {

    @Autowired
    private PostRepo postRepo;

    @Autowired
    private CommentRepo commentRepo;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private ModelMapper modelMapper;



    @Override
    public CommentDto createComment(CommentDto commentDto, Integer postId , Integer userId) {

        Post post = postRepo.findById(postId).orElseThrow(() -> new ResourceNotFoundException("Post", "Post Id", postId));
        User user = userRepo.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User", "User Id ", userId));
        Comment comment = this.modelMapper.map(commentDto, Comment.class);
        comment.setPost(post);
        comment.setUser(user);
        Comment save = commentRepo.save(comment);
        return this.modelMapper.map(save,CommentDto.class);
    }

    @Override
    public CommentDto updateComment(CommentDto commentDto, Integer commentId) {
        Comment comment = commentRepo.findById(commentId).orElseThrow(() -> new ResourceNotFoundException("Comment", "Comment Id ", commentId));
        comment.setContent(commentDto.getContent());
        Comment saved = commentRepo.save(comment);
        return this.modelMapper.map(saved,CommentDto.class);
    }

    @Override
    public void deleteComment(Integer commentId) {
        Comment comment = commentRepo.findById(commentId).orElseThrow(() -> new ResourceNotFoundException("Comment", "Comment Id", commentId));
        commentRepo.delete(comment);
    }

    @Override
    public List<CommentDto> getAllComment() {
        List<Comment> commentList = commentRepo.findAll();
        return commentList.stream().map(comment -> this.modelMapper.map(comment,CommentDto.class)).toList();
    }

    @Override
    public CommentDto getCommentById(Integer commentId) {
        Comment comment = commentRepo.findById(commentId).orElseThrow(() -> new ResourceNotFoundException("Comment", "Comment Id", commentId));
        return  modelMapper.map(comment, CommentDto.class);
    }

    @Override
    public List<CommentDto> getCommentsByPost(Integer postId) {
        Post post = postRepo.findById(postId).orElseThrow(() -> new ResourceNotFoundException("Post", "Post Id", postId));

        List<Comment> comments = commentRepo.findByPost(post);

        return comments.stream()
                .map(comment -> modelMapper.map(comment, CommentDto.class))
                .toList();
    }
}
