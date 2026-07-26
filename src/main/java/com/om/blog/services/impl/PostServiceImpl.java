package com.om.blog.services.impl;

import com.om.blog.entities.Post;
import com.om.blog.payloads.PostDto;
import com.om.blog.services.PostService;

import java.util.List;

public class PostServiceImpl implements PostService {

    @Override
    public Post createPost(PostDto postDto) {
        return null;
    }

    @Override
    public Post updatePost(PostDto postDto) {
        return null;
    }

    @Override
    public void deletePost(Integer postId) {

    }

    @Override
    public List<Post> getAllPost() {
        return List.of();
    }

    @Override
    public List<Post> getPostById(Integer postId) {
        return List.of();
    }

    @Override
    public List<Post> getPostByCategory(Integer categoryId) {
        return List.of();
    }

    @Override
    public List<Post> getPostByUser(Integer userId) {
        return List.of();
    }
}
