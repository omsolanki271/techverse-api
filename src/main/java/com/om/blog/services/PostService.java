package com.om.blog.services;

import com.om.blog.payloads.PostDto;
import com.om.blog.payloads.PostImageResponse;
import com.om.blog.payloads.PostResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface PostService {

    PostDto createPost(PostDto postDto , Integer categoryId);

    PostDto updatePost(PostDto postDto , Integer postId);

    void deletePost(Integer postId);

    // Get all posts with pagination
    PostResponse getAllPosts(Integer pageNumber , Integer pageSize , String sortBy , String sortDirection);

    PostDto getPostById(Integer postId);

    // Get posts by category with pagination
    PostResponse getPostByCategory(Integer categoryId, Integer pageNumber, Integer pageSize , String sortBy , String sortDirection);

    // Get posts by user with pagination
    PostResponse getPostByUser(Integer userId, Integer pageNumber, Integer pageSize, String sortBy , String sortDirection);

    //search
    List<PostDto> searchPosts(String keyword, Integer categoryId);

    PostDto uploadPostImage(Integer postId, MultipartFile image) throws IOException;

    PostImageResponse getPostImage(Integer postId) throws IOException;
}