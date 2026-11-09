package com.om.blog.services.impl;

import com.om.blog.config.AppConstants;
import com.om.blog.entities.*;
import com.om.blog.exceptions.ResourceNotFoundException;
import com.om.blog.payloads.PostDto;
import com.om.blog.payloads.PostImageResponse;
import com.om.blog.payloads.PostResponse;
import com.om.blog.repositories.*;
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
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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

    @Autowired
    private PostLikeRepo postLikeRepo;

    @Autowired
    private PostBookmarkRepo postBookmarkRepo;

    @Value("${project.image}")
    private String path;

    @jakarta.annotation.PostConstruct
    public void initStatusMigration() {
        try {
            postRepo.updateNullStatusToPublished();
        } catch (Exception ignored) {}
    }

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

        if (postDto.getStatus() != null && !postDto.getStatus().isBlank()) {
            post.setStatus(postDto.getStatus().toUpperCase());
        } else {
            post.setStatus("PUBLISHED");
        }

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

        return mapToPostDto(savedPost);
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

        if (postDto.getStatus() != null && !postDto.getStatus().isBlank()) {
            post.setStatus(postDto.getStatus().toUpperCase());
        }

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

        return mapToPostDto(updatedPost);
    }

    // Delete Post
    @Transactional
    @Override
    public void deletePost(Integer postId) {

        Post post = postRepo.findById(postId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Post", "Post Id", postId));

        checkPostOwnerOrAdmin(post);

        postRepo.delete(post);
    }

    // Get All Published Posts with Pagination
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

        // Public listings return all published posts (including legacy posts where status was null)
        Page<Post> pagePost = postRepo.findAllPublished(pageable);

        return createPostResponse(pagePost);
    }

    // Get Post By ID
    @Override
    public PostDto getPostById(Integer postId) {

        Post post = postRepo.findById(postId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Post", "Post Id", postId));

        // If post is DRAFT, only owner or ADMIN can view it
        if ("DRAFT".equalsIgnoreCase(post.getStatus())) {
            checkPostOwnerOrAdmin(post);
        } else {
            post.setViews(post.getViews() + 1);
            postRepo.save(post);
        }

        return mapToPostDto(post);
    }

    // Get Posts By Category (Published only)
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
                postRepo.findByCategoryPublished(category, pageable);

        return createPostResponse(pagePost);
    }

    // Get Posts By User (If owner/admin return all; else return published only)
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

        User currentUser = getCurrentUserOrNull();
        boolean isOwnerOrAdmin = currentUser != null && (currentUser.getId() == userId || isCurrentUserAdmin());

        Page<Post> pagePost;
        if (isOwnerOrAdmin) {
            pagePost = postRepo.findByUser(user, pageable);
        } else {
            pagePost = postRepo.findByUserPublished(user, pageable);
        }

        return createPostResponse(pagePost);
    }

    // Search Published Posts
    @Override
    public List<PostDto> searchPosts(String keyword, Integer categoryId) {

        List<Post> posts = postRepo.searchByCategoryAndTitle(categoryId, keyword);

        return posts.stream()
                .map(this::mapToPostDto)
                .toList();
    }

    // Upload Post Image
    @Override
    public PostDto uploadPostImage(Integer postId, MultipartFile image) throws IOException {

        Post post = postRepo.findById(postId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Post", "Post Id", postId));

        checkPostOwnerOrAdmin(post);

        String userFolder = "user_" + post.getUser().getId();
        String fileName = fileService.uploadImage(path, userFolder, image);

        post.setImageName(fileName);

        Post updatedPost = postRepo.save(post);

        return mapToPostDto(updatedPost);
    }

    // Get / Download Post Image
    @Override
    public PostImageResponse getPostImage(Integer postId) throws IOException {

        Post post = postRepo.findById(postId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Post", "Post Id", postId));

        String fileName = post.getImageName();
        String userFolder = "user_" + post.getUser().getId();

        InputStream inputStream = fileService.getResource(path, userFolder, fileName);

        byte[] imageData = inputStream.readAllBytes();

        String contentType = determineContentType(fileName);

        return new PostImageResponse(imageData, contentType);
    }

    // Feature 1 & 7: Toggle Like and Get Count
    @Transactional
    @Override
    public Map<String, Object> toggleLike(Integer postId) {
        Post post = postRepo.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post", "Post Id", postId));
        User currentUser = getCurrentUserOrNull();
        if (currentUser == null) {
            throw new BadCredentialsException("User must be authenticated to like a post");
        }

        boolean liked;
        var existing = postLikeRepo.findByPostAndUser(post, currentUser);
        if (existing.isPresent()) {
            postLikeRepo.delete(existing.get());
            liked = false;
        } else {
            postLikeRepo.save(new PostLike(post, currentUser));
            liked = true;
        }

        long count = postLikeRepo.countByPost(post);
        Map<String, Object> response = new HashMap<>();
        response.put("liked", liked);
        response.put("likeCount", count);
        return response;
    }

    @Override
    public Map<String, Object> getLikeStatus(Integer postId) {
        Post post = postRepo.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post", "Post Id", postId));
        User currentUser = getCurrentUserOrNull();
        boolean liked = currentUser != null && postLikeRepo.existsByPostAndUser(post, currentUser);
        long count = postLikeRepo.countByPost(post);

        Map<String, Object> response = new HashMap<>();
        response.put("liked", liked);
        response.put("likeCount", count);
        return response;
    }

    // Feature 4 & 8: Bookmark / Save Posts and Count
    @Transactional
    @Override
    public Map<String, Object> toggleBookmark(Integer postId) {
        Post post = postRepo.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post", "Post Id", postId));
        User currentUser = getCurrentUserOrNull();
        if (currentUser == null) {
            throw new BadCredentialsException("User must be authenticated to bookmark a post");
        }

        boolean bookmarked;
        var existing = postBookmarkRepo.findByPostAndUser(post, currentUser);
        if (existing.isPresent()) {
            postBookmarkRepo.delete(existing.get());
            bookmarked = false;
        } else {
            postBookmarkRepo.save(new PostBookmark(post, currentUser));
            bookmarked = true;
        }

        long count = postBookmarkRepo.countByPost(post);
        Map<String, Object> response = new HashMap<>();
        response.put("bookmarked", bookmarked);
        response.put("bookmarkCount", count);
        return response;
    }

    @Override
    public Map<String, Object> getBookmarkStatus(Integer postId) {
        Post post = postRepo.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post", "Post Id", postId));
        User currentUser = getCurrentUserOrNull();
        boolean bookmarked = currentUser != null && postBookmarkRepo.existsByPostAndUser(post, currentUser);
        long count = postBookmarkRepo.countByPost(post);

        Map<String, Object> response = new HashMap<>();
        response.put("bookmarked", bookmarked);
        response.put("bookmarkCount", count);
        return response;
    }

    @Override
    public PostResponse getMyBookmarks(Integer pageNumber, Integer pageSize, String sortBy, String sortDirection) {
        User currentUser = getCurrentUserOrNull();
        if (currentUser == null) {
            throw new BadCredentialsException("User must be authenticated");
        }
        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by("createdAt").descending());
        Page<PostBookmark> bookmarkPage = postBookmarkRepo.findByUser(currentUser, pageable);

        List<PostDto> postDtos = bookmarkPage.getContent().stream()
                .map(b -> mapToPostDto(b.getPost()))
                .toList();

        PostResponse postResponse = new PostResponse();
        postResponse.setContent(postDtos);
        postResponse.setPageNumber(bookmarkPage.getNumber());
        postResponse.setPageSize(bookmarkPage.getSize());
        postResponse.setTotalElements(bookmarkPage.getTotalElements());
        postResponse.setTotalPages(bookmarkPage.getTotalPages());
        postResponse.setLastPage(bookmarkPage.isLast());
        return postResponse;
    }

    // Feature 5: Share counter
    @Transactional
    @Override
    public Map<String, Object> incrementShareCount(Integer postId) {
        Post post = postRepo.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post", "Post Id", postId));
        post.setShareCount(post.getShareCount() + 1);
        postRepo.save(post);

        Map<String, Object> response = new HashMap<>();
        response.put("shareCount", post.getShareCount());
        return response;
    }

    // Feature 9 & 10: Publish Post & Draft Management
    @Transactional
    @Override
    public PostDto publishPost(Integer postId) {
        Post post = postRepo.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post", "Post Id", postId));
        checkPostOwnerOrAdmin(post);
        post.setStatus("PUBLISHED");
        Post saved = postRepo.save(post);
        return mapToPostDto(saved);
    }

    @Override
    public PostResponse getMyDrafts(Integer pageNumber, Integer pageSize, String sortBy, String sortDirection) {
        User currentUser = getCurrentUserOrNull();
        if (currentUser == null) {
            throw new BadCredentialsException("User must be authenticated");
        }
        Sort sort = sortDirection.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);
        Page<Post> pagePost = postRepo.findByUserDrafts(currentUser, pageable);
        return createPostResponse(pagePost);
    }

    @Override
    public Map<String, Object> getUserPostCounts(Integer userId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "User Id", userId));
        long publishedCount = postRepo.countPublishedByUser(user);
        long draftCount = postRepo.countDraftsByUser(user);
        long totalCount = postRepo.countByUser(user);

        Map<String, Object> response = new HashMap<>();
        response.put("publishedCount", publishedCount);
        response.put("draftCount", draftCount);
        response.put("totalCount", totalCount);
        return response;
    }

    // Helper: Map Post to PostDto with counts and user flags
    private PostDto mapToPostDto(Post post) {
        PostDto dto = modelMapper.map(post, PostDto.class);
        dto.setShareCount(post.getShareCount());
        dto.setStatus(post.getStatus() != null ? post.getStatus() : "PUBLISHED");
        dto.setLikeCount(postLikeRepo.countByPost(post));
        dto.setBookmarkCount(postBookmarkRepo.countByPost(post));

        User currentUser = getCurrentUserOrNull();
        if (currentUser != null) {
            dto.setLiked(postLikeRepo.existsByPostAndUser(post, currentUser));
            dto.setBookmarked(postBookmarkRepo.existsByPostAndUser(post, currentUser));
        } else {
            dto.setLiked(false);
            dto.setBookmarked(false);
        }
        return dto;
    }

    // Helper: Create Post Response
    private PostResponse createPostResponse(Page<Post> pagePost) {

        List<PostDto> postDtos = pagePost.getContent()
                .stream()
                .map(this::mapToPostDto)
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

    // Helper: Get Current Authenticated User or null
    private User getCurrentUserOrNull() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
                return userRepo.findByEmail(auth.getName()).orElse(null);
            }
        } catch (Exception ignored) {}
        return null;
    }

    private boolean isCurrentUserAdmin() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated()) {
                return auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            }
        } catch (Exception ignored) {}
        return false;
    }

    // Helper: Determine Image Content Type
    private String determineContentType(String fileName) {

        int dotIndex = fileName.lastIndexOf('.');

        if (dotIndex == -1) {
            return MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }

        String extension = fileName.substring(dotIndex).toLowerCase(Locale.ROOT);

        return switch (extension) {
            case ".jpg", ".jpeg" -> MediaType.IMAGE_JPEG_VALUE;
            case ".png" -> MediaType.IMAGE_PNG_VALUE;
            case ".gif" -> MediaType.IMAGE_GIF_VALUE;
            case ".webp" -> "image/webp";
            default -> MediaType.APPLICATION_OCTET_STREAM_VALUE;
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
