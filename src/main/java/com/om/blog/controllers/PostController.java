package com.om.blog.controllers;

import com.om.blog.config.AppConstants;
import com.om.blog.entities.Post;
import com.om.blog.exceptions.ResourceNotFoundException;
import com.om.blog.payloads.ApiResponse;
import com.om.blog.payloads.PostDto;
import com.om.blog.payloads.PostResponse;
import com.om.blog.repositories.PostRepo;
import com.om.blog.services.FileService;
import com.om.blog.services.PostService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;


@RestController
@RequestMapping("/api/posts")
public class PostController {

    @Autowired
    private PostService postService;

    @Autowired
    private FileService fileService;

    @Autowired
    private PostRepo postRepo;

    //get value from application properties
    @Value("${project.image}")
    private String path;

    //create
    @PostMapping("/category/{categoryId}/posts")
    public ResponseEntity<PostDto> createPost(
            @Valid @RequestBody PostDto postDto,
            @PathVariable Integer categoryId
    )
    {
        PostDto createPost = postService.createPost(postDto, categoryId);
        return  new ResponseEntity<>(createPost , HttpStatus.CREATED);
    }

    // Get all posts
    @GetMapping("/")
    public ResponseEntity<PostResponse> getAllPosts(
            @RequestParam(value = "pageNumber" , defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
            @RequestParam(value = "pageSize" , defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize,
            @RequestParam(value = "sortBy" , defaultValue = AppConstants.SORT_BY, required = false) String sortBy,
            @RequestParam(value = "sortDirection" , defaultValue = AppConstants.SORT_DIRECTION, required = false) String sortDirection
    )
    {
        PostResponse postResponse = postService.getAllPosts(pageNumber,pageSize,sortBy,sortDirection);
        return new ResponseEntity<>(postResponse, HttpStatus.OK);
    }

    // Get post by id
    @GetMapping("/{postId}")
    public ResponseEntity<PostDto> getPostById(@PathVariable Integer postId) {

        return ResponseEntity.ok(postService.getPostById(postId));
    }

    @PutMapping("/{postId}")
    public ResponseEntity<PostDto> updatePost(
            @Valid @RequestBody PostDto postDto,
            @PathVariable Integer postId) {

        PostDto updatedPost = postService.updatePost(postDto, postId);

        return ResponseEntity.ok(updatedPost);
    }

    //delete
    @DeleteMapping("/{postId}")
    public ResponseEntity<ApiResponse> deletePost(@PathVariable Integer postId )
    {
        postService.deletePost(postId);
        return  new ResponseEntity<>(new ApiResponse("Post deleted Successfully ", true),HttpStatus.OK );
    }


    // get post by User
    @GetMapping("/user/{userId}/posts")
    public ResponseEntity<PostResponse> getPostsByUser(
            @PathVariable Integer userId,
            @RequestParam(value = "pageNumber" , defaultValue = "0", required = false) Integer pageNumber,
            @RequestParam(value = "pageSize" , defaultValue = "10", required = false) Integer pageSize,
            @RequestParam(value = "sortBy" , defaultValue = "postId" , required = false) String sortBy,
            @RequestParam(value = "sortDirection" , defaultValue = "asc" , required = false) String sortDirection
            ) {

        PostResponse postByUser = postService.getPostByUser(userId, pageNumber, pageSize, sortBy, sortDirection);
        return new ResponseEntity<>(postByUser ,HttpStatus.OK);
    }

    // Get posts by category
    @GetMapping("/category/{categoryId}/posts")
    public ResponseEntity<PostResponse> getPostsByCategory(
            @PathVariable Integer categoryId,
            @RequestParam(value = "pageNumber" , defaultValue = "0" , required = false) Integer pageNumber,
            @RequestParam(value = "pageSize" , defaultValue = "10" , required = false) Integer pageSize,
            @RequestParam(value = "sortBy" , defaultValue = "postId" , required = false) String sortBy,
            @RequestParam(value = "sortDirection" , defaultValue = "asc" , required = false) String sortDirection
            ) {
        PostResponse postByCategory = postService.getPostByCategory(categoryId, pageNumber, pageSize,sortBy,sortDirection);
        return new ResponseEntity<>(postByCategory, HttpStatus.OK);
    }

    //search

    @GetMapping("/search")
    public ResponseEntity<List<PostDto>> searchPostByTitle(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer categoryId
    )
    {
        List<PostDto> postDto = postService.searchPosts(keyword, categoryId);
        return new ResponseEntity<>(postDto,HttpStatus.OK);
    }

    //post image upload

    @PostMapping("/image/upload/{postId}")
    public ResponseEntity<?> uploadPostImage(
            @RequestParam(value = "image", required = false) MultipartFile image,
            @PathVariable Integer postId
    ) throws IOException
    {
        if (image == null || image.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        //check post is avaliable or not
        PostDto postDto = this.postService.getPostById(postId);

        try {
            //uploaded process
            Post post = postRepo.findById(postId)
                    .orElseThrow(() ->
                            new ResourceNotFoundException("Post", "Post Id", postId));

            String userFolder = "user-" + post.getUser().getId();
            String uploadImage = this.fileService.uploadImage(path,userFolder, image);
            postDto.setImageName(uploadImage);
            PostDto updatePost = this.postService.updatePost(postDto, postId);
            return  new ResponseEntity<>(updatePost,HttpStatus.OK);
        } catch (IOException e) {
            return new ResponseEntity<>(
                    new ApiResponse(e.getMessage(), false),
                    HttpStatus.BAD_REQUEST
            );
        }
    }
    //method to serve file

// get to display post image with url

    @GetMapping("/image/{postId}")
    public void downloadImage(
            @PathVariable("postId") Integer postId,
            HttpServletResponse response
    ) throws IOException {
        Post post = postRepo.findById(postId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Post", "Post Id", postId));

        InputStream resource;

        if (AppConstants.DEFAULT_IMAGE.equals(post.getImageName())) {
            resource = fileService.getResource(path, "", post.getImageName());
        }
        else
        {
            String userFolder = "user-" + post.getUser().getId();
            resource = fileService.getResource(path, userFolder, post.getImageName());
        }
        String contentType = getContentType(post.getImageName());
        response.setContentType(contentType);
        StreamUtils.copy(resource,response.getOutputStream());
    }

    private String getContentType(String imageName) {
        String extension =
                imageName.substring(
                        imageName.lastIndexOf(".")
                ).toLowerCase();

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

}
