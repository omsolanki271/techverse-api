package com.om.blog.services.impl;

import com.om.blog.entities.Category;
import com.om.blog.entities.Post;
import com.om.blog.entities.User;
import com.om.blog.exceptions.ResourceNotFoundException;
import com.om.blog.payloads.PostDto;
import com.om.blog.repositories.CategoryRepo;
import com.om.blog.repositories.PostRepo;
import com.om.blog.repositories.UserRepo;
import com.om.blog.services.PostService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PostServiceImpl implements PostService {

    @Autowired
    private PostRepo postRepo;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private CategoryRepo categoryRepo;

    @Autowired
    private UserRepo userRepo;

    @Override
    public PostDto createPost(PostDto postDto, Integer userId, Integer categoryId) {

        Category category = this.categoryRepo.findById(categoryId).orElseThrow(() -> new ResourceNotFoundException("Category", "Category Id", categoryId));
        User user = this.userRepo.findById(userId).orElseThrow(()-> new ResourceNotFoundException("User" , "User id" , userId));

        Post post = this.modelMapper.map(postDto, Post.class);

        post.setImageName("default.png");
        post.setCategory(category);
        post.setUser(user);
        Post savePost = postRepo.save(post);
        return this.modelMapper.map(savePost , PostDto.class);
    }

    @Override
    public Post updatePost(PostDto postDto , Integer postId) {
        return null;
    }

    @Override
    public void deletePost(Integer postId) {
        Post findId = postRepo.findById(postId).orElseThrow(() -> new ResourceNotFoundException("Post", "PostId", postId));
        postRepo.delete(findId);
    }

    @Override
    public List<PostDto> getAllPost() {
        List<Post> postList = postRepo.findAll();
        return postList.stream().map(post -> modelMapper.map(post,PostDto.class)).toList();
    }

    @Override
    public PostDto getPostById(Integer postId) {
        Post post = postRepo.findById(postId).orElseThrow(() -> new ResourceNotFoundException("Post", "Post Id", postId));
        return modelMapper.map(post, PostDto.class);
    }

    @Override
    public List<PostDto> getPostByCategory(Integer categoryId) {
//        System.out.println("Category Id = " + categoryId);

        Category category = categoryRepo.findById(categoryId)
                .orElseThrow(() ->  new ResourceNotFoundException("Category", "Category Id", categoryId));
//       System.out.println("Category Found = " + category.getCategoryTitle());

        List<Post> posts = postRepo.findByCategory(category);
        return posts.stream()
                .map(post -> modelMapper.map(post, PostDto.class))
                .toList();
    }

    @Override
    public List<PostDto> getPostByUser(Integer userId) {

        User user = this.userRepo.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User", "User Id", userId));
        List<Post> posts = this.postRepo.findByUser(user);
        return posts.stream()
                .map(post -> this.modelMapper.map(post, PostDto.class))
                .toList();
    }

    @Override
    public List<PostDto> searchPosts(String keyword) {
        return List.of();
    }
}

