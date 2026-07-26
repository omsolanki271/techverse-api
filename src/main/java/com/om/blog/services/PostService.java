package com.om.blog.services;

import com.om.blog.entities.Post;
import com.om.blog.payloads.PostDto;

import java.util.List;

public interface PostService {

    Post createPost(PostDto postDto);

    Post updatePost(PostDto postDto);

    void deletePost(Integer postId);

    List<Post> getAllPost();

    List<Post> getPostById(Integer postId);
    
    List<Post> getPostByCategory(Integer categoryId);

    List<Post> getPostByUser(Integer userId);

}