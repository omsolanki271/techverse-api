package com.om.blog.services;

import com.om.blog.entities.Post;
import com.om.blog.payloads.PostDto;
import com.om.blog.payloads.PostResponse;

import java.util.List;

public interface PostService {

    PostDto createPost(PostDto postDto , Integer userId ,Integer categoryId);

    PostDto updatePost(PostDto postDto , Integer postId);

    void deletePost(Integer postId);

    PostResponse getAllPosts(Integer pageNumber , Integer pageSize);

    PostDto getPostById(Integer postId);

    List<PostDto> getPostByCategory(Integer categoryId);

    List<PostDto> getPostByUser(Integer userId);

    List<PostDto> searchPosts(String keyword);
}