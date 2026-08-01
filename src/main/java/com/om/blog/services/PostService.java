package com.om.blog.services;

import com.om.blog.entities.Post;
import com.om.blog.payloads.PostDto;

import java.util.List;

public interface PostService {

    PostDto createPost(PostDto postDto , Integer userId ,Integer categoryId);

    Post updatePost(PostDto postDto , Integer postId);

    void deletePost(Integer postId);

    List<Post> getAllPost();

    List<Post> getPostById(Integer postId);

    List<PostDto> getPostByCategory(Integer categoryId);

    List<PostDto> getPostByUser(Integer userId);

    List<PostDto> searchPosts(String keyword);
}