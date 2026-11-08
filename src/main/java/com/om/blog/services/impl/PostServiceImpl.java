
package com.om.blog.services.impl;

import com.om.blog.config.AppConstants;
import com.om.blog.entities.Category;
import com.om.blog.entities.Media;
import com.om.blog.entities.Post;
import com.om.blog.entities.User;
import com.om.blog.exceptions.ResourceNotFoundException;
import com.om.blog.payloads.PostDto;
import com.om.blog.payloads.PostImageResponse;
import com.om.blog.payloads.PostResponse;
import com.om.blog.repositories.CategoryRepo;
import com.om.blog.repositories.MediaRepo;
import com.om.blog.repositories.PostRepo;
import com.om.blog.repositories.UserRepo;
import com.om.blog.services.FileService;
import com.om.blog.services.PostService;

import org.modelmapper.ModelMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;

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

    @Autowired
    private MediaRepo mediaRepo;

    @Autowired
    private FileService fileService;

    @Value("${project.image}")
    private String path;


    // Create Post
    @Override
    public PostDto createPost(PostDto postDto, Integer categoryId) {

        Category category = categoryRepo.findById(categoryId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Category", "Category Id", categoryId));

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        User user = userRepo.findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new ResourceNotFoundException("User", "Email", 0));

        Post post = modelMapper.map(postDto, Post.class);

        if (postDto.getMediaId() != null) {

            Media media = mediaRepo.findById(postDto.getMediaId())
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "Media", "Media Id", postDto.getMediaId()));

            checkMediaOwnerOrAdmin(media);

            post.setMedia(media);
            post.setImageName(media.getFileName());

        } else {
            post.setImageName(AppConstants.DEFAULT_IMAGE);
        }

        post.setCategory(category);
        post.setUser(user);

        Post savedPost = postRepo.save(post);

        return modelMapper.map(savedPost, PostDto.class);
    }


    // Update Post
    @Override
    public PostDto updatePost(PostDto postDto, Integer postId) {

        Post post = postRepo.findById(postId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Post", "Post Id", postId));

        checkPostOwnerOrAdmin(post);

        post.setTitle(postDto.getTitle());
        post.setContent(postDto.getContent());

        // Update media only when a new media ID is provided
        if (postDto.getMediaId() != null) {

            Media media = mediaRepo.findById(postDto.getMediaId())
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "Media", "Media Id", postDto.getMediaId()));

            checkMediaOwnerOrAdmin(media);

            post.setMedia(media);
            post.setImageName(media.getFileName());
        }

        Post updatedPost = postRepo.save(post);

        return modelMapper.map(updatedPost, PostDto.class);
    }


    // Delete Post
    @Override
    public void deletePost(Integer postId) {

        Post post = postRepo.findById(postId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Post", "Post Id", postId));

        checkPostOwnerOrAdmin(post);

        postRepo.delete(post);
    }


    // Get All Posts with Pagination
    @Override
    public PostResponse getAllPosts(
            Integer pageNumber,
            Integer pageSize,
            String sortBy,
            String sortDirection) {

        Sort sort = sortDirection.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(
                pageNumber, pageSize, sort);

        Page<Post> pagePost = postRepo.findAll(pageable);

        return createPostResponse(pagePost);
    }


    // Get Post By ID
    @Override
    public PostDto getPostById(Integer postId) {

        Post post = postRepo.findById(postId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Post", "Post Id", postId));

        post.setViews(post.getViews() + 1);

        postRepo.save(post);

        return modelMapper.map(post, PostDto.class);
    }


    // Get Posts By Category
    @Override
    public PostResponse getPostByCategory(
            Integer categoryId,
            Integer pageNumber,
            Integer pageSize,
            String sortBy,
            String sortDirection) {

        Category category = categoryRepo.findById(categoryId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Category", "Category Id", categoryId));

        Sort sort = sortDirection.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(
                pageNumber, pageSize, sort);

        Page<Post> pagePost =
                postRepo.findByCategory(category, pageable);

        return createPostResponse(pagePost);
    }


    // Get Posts By User
    @Override
    public PostResponse getPostByUser(
            Integer userId,
            Integer pageNumber,
            Integer pageSize,
            String sortBy,
            String sortDirection) {

        User user = userRepo.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User", "User Id", userId));

        Sort sort = sortDirection.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(
                pageNumber, pageSize, sort);

        Page<Post> pagePost =
                postRepo.findByUser(user, pageable);

        return createPostResponse(pagePost);
    }


    // Create Post Response
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


    // Search Posts
    @Override
    public List<PostDto> searchPosts(
            String keyword,
            Integer categoryId) {

        List<Post> posts =
                postRepo.searchByCategoryAndTitle(categoryId, keyword);

        return posts.stream()
                .map(post -> modelMapper.map(post, PostDto.class))
                .toList();
    }


    // Upload Post Image
    @Override
    public PostDto uploadPostImage(
            Integer postId,
            MultipartFile image) throws IOException {

        if (image == null || image.isEmpty()) {
            throw new IOException("Please select an image.");
        }

        Post post = postRepo.findById(postId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Post", "Post Id", postId));

        // Check ownership before uploading the image
        checkPostOwnerOrAdmin(post);

        String userFolder = "user-" + post.getUser().getId();

        String fileName = fileService.uploadImage(
                path, userFolder, image);

        post.setImageName(fileName);

        // This post now uses its own uploaded image
        post.setMedia(null);

        Post updatedPost = postRepo.save(post);

        return modelMapper.map(updatedPost, PostDto.class);
    }


    // Get Post Image
    @Override
    public PostImageResponse getPostImage(
            Integer postId) throws IOException {

        Post post = postRepo.findById(postId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Post", "Post Id", postId));

        String userFolder = "";

        if (!AppConstants.DEFAULT_IMAGE.equals(post.getImageName())) {
            userFolder = "user-" + post.getUser().getId();
        }

        try (InputStream inputStream = fileService.getResource(
                path, userFolder, post.getImageName())) {

            byte[] imageData = inputStream.readAllBytes();

            String contentType = getContentType(post.getImageName());

            return new PostImageResponse(imageData, contentType);
        }
    }


    // Get Image Content Type
    private String getContentType(String imageName) {

        String extension = imageName.substring(
                imageName.lastIndexOf(".")
        ).toLowerCase(Locale.ROOT);

        return switch (extension) {

            case ".jpg", ".jpeg" ->
                    MediaType.IMAGE_JPEG_VALUE;

            case ".png" ->
                    MediaType.IMAGE_PNG_VALUE;

            case ".gif" ->
                    MediaType.IMAGE_GIF_VALUE;

            case ".webp" ->
                    "image/webp";

            default ->
                    MediaType.APPLICATION_OCTET_STREAM_VALUE;
        };
    }


    // Check Post Owner or Admin
    private void checkPostOwnerOrAdmin(Post post) {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(authority ->
                        authority.getAuthority().equals("ROLE_ADMIN"));

        if (isAdmin) {
            return;
        }

        String loggedInEmail = authentication.getName();

        if (!post.getUser().getEmail().equals(loggedInEmail)) {

            throw new AccessDeniedException(
                    "You are not allowed to modify this post");
        }
    }


    // Check Media Owner or Admin
    private void checkMediaOwnerOrAdmin(Media media) {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(authority ->
                        authority.getAuthority().equals("ROLE_ADMIN"));

        if (isAdmin) {
            return;
        }

        String loggedInEmail = authentication.getName();

        if (!media.getUser().getEmail().equals(loggedInEmail)) {

            throw new AccessDeniedException(
                    "You are not allowed to use this media");
        }
    }
}
