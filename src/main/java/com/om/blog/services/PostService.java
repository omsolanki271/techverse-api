package com.om.blog.services;

import com.om.blog.payloads.PostDto;
import com.om.blog.payloads.PostImageResponse;
import com.om.blog.payloads.PostResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface PostService {

    PostDto createPost(PostDto postDto , Integer categoryId);

    PostDto updatePost(PostDto postDto , Integer postId);

    void deletePost(Integer postId);

    // Get all published posts with pagination
    PostResponse getAllPosts(Integer pageNumber , Integer pageSize , String sortBy , String sortDirection);

    PostDto getPostById(Integer postId);

    // Get posts by category with pagination (published only)
    PostResponse getPostByCategory(Integer categoryId, Integer pageNumber, Integer pageSize , String sortBy , String sortDirection);

    // Get posts by user with pagination
    PostResponse getPostByUser(Integer userId, Integer pageNumber, Integer pageSize, String sortBy , String sortDirection);

    // Search published posts
    List<PostDto> searchPosts(String keyword, Integer categoryId);

    PostDto uploadPostImage(Integer postId, MultipartFile image) throws IOException;

    PostImageResponse getPostImage(Integer postId) throws IOException;

    // Feature 1 & 7: Like / Unlike & Count
    Map<String, Object> toggleLike(Integer postId);
    Map<String, Object> getLikeStatus(Integer postId);

    // Feature 4 & 8: Bookmark / Save Posts & Count
    Map<String, Object> toggleBookmark(Integer postId);
    Map<String, Object> getBookmarkStatus(Integer postId);
    PostResponse getMyBookmarks(Integer pageNumber, Integer pageSize, String sortBy, String sortDirection);

    // Feature 5: Share counter
    Map<String, Object> incrementShareCount(Integer postId);

    // Feature 9 & 10: Draft functionality & Counts
    PostDto publishPost(Integer postId);
    PostResponse getMyDrafts(Integer pageNumber, Integer pageSize, String sortBy, String sortDirection);
    Map<String, Object> getUserPostCounts(Integer userId);
}
