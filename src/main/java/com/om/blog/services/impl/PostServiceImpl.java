package com.om.blog.services.impl;

import com.om.blog.entities.Category;
import com.om.blog.entities.Post;
import com.om.blog.entities.User;
import com.om.blog.exceptions.ResourceNotFoundException;
import com.om.blog.payloads.PostDto;
import com.om.blog.payloads.PostResponse;
import com.om.blog.repositories.CategoryRepo;
import com.om.blog.repositories.PostRepo;
import com.om.blog.repositories.UserRepo;
import com.om.blog.services.PostService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
    public PostDto updatePost(PostDto postDto , Integer postId) {
        Post post = postRepo.findById(postId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Post", "Post Id", postId));

        post.setTitle(postDto.getTitle());
        post.setContent(postDto.getContent());
        post.setImageName(postDto.getImageName());

        Post updatedPost = postRepo.save(post);

        return modelMapper.map(updatedPost, PostDto.class);
    }

    @Override
    public void deletePost(Integer postId) {
        Post findId = postRepo.findById(postId).orElseThrow(() -> new ResourceNotFoundException("Post", "Post Id", postId));
        postRepo.delete(findId);
    }

    @Override
    public PostResponse getAllPosts(Integer pageNumber , Integer pageSize , String sortBy , String sortDirection) {

        Sort sort = (sortDirection.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending());
        Pageable pageable  = PageRequest.of(pageNumber,pageSize,sort);

        Page<Post> pagePost = postRepo.findAll(pageable);

        return createPostResponse(pagePost);
    }

    @Override
    public PostDto getPostById(Integer postId) {
        Post post = postRepo.findById(postId).orElseThrow(() -> new ResourceNotFoundException("Post", "Post Id", postId));
        return modelMapper.map(post, PostDto.class);
    }

    @Override
    public PostResponse getPostByCategory(Integer categoryId, Integer pageNumber, Integer pageSize , String sortBy , String sortDirection)  {

//        System.out.println("Category Id = " + categoryId);
        Category category = categoryRepo.findById(categoryId)
                .orElseThrow(() ->  new ResourceNotFoundException("Category", "Category Id", categoryId));
//       System.out.println("Category Found = " + category.getCategoryTitle());

        Sort sort = (sortDirection.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending());

        Pageable pageable = PageRequest.of(pageNumber, pageSize , sort);

        Page<Post> pagePost = postRepo.findByCategory(category, pageable);

        return createPostResponse(pagePost);
    }

    @Override
    public PostResponse getPostByUser(Integer userId, Integer pageNumber, Integer pageSize ,  String sortBy , String sortDirection)
    {

        User user = this.userRepo.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "User Id", userId));
        Sort sort = (sortDirection.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending());

        Pageable pageable = PageRequest.of(pageNumber, pageSize , sort);

        Page<Post> pagePost = postRepo.findByUser(user, pageable);

        return createPostResponse(pagePost);
    }

    private PostResponse createPostResponse(Page<Post> pagePost) {

        List<PostDto> postDtos = pagePost.getContent()
                .stream()
                .map(post -> modelMapper.map(post, PostDto.class))
                .toList();

        PostResponse postResponse = new PostResponse();

        postResponse.setContent(postDtos);
        postResponse.setPageNumber(pagePost.getNumber());
        postResponse.setPageSize(pagePost.getSize());
        postResponse.setTotalElements(pagePost.getTotalElements());
        postResponse.setTotalPages(pagePost.getTotalPages());
        postResponse.setLastPage(pagePost.isLast());

        return postResponse;
    }

    // work like query 
    @Override
    public List<PostDto> searchPosts(String keyword) {
        List<Post> byTitleContaining = this.postRepo.findByTitleContaining(keyword);
        return byTitleContaining.stream().map(post -> this.modelMapper.map(post, PostDto.class)).toList();
    }
}


