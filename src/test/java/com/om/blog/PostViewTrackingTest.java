package com.om.blog;

import com.om.blog.entities.Category;
import com.om.blog.entities.Post;
import com.om.blog.entities.User;
import com.om.blog.payloads.PostDto;
import com.om.blog.repositories.PostBookmarkRepo;
import com.om.blog.repositories.PostLikeRepo;
import com.om.blog.repositories.PostRepo;
import com.om.blog.services.impl.PostServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class PostViewTrackingTest {

    private PostServiceImpl postService;
    private Map<Integer, Post> postDatabase;
    private int incrementViewsCallCount;

    @BeforeEach
    void setUp() {
        postDatabase = new HashMap<>();
        incrementViewsCallCount = 0;

        postService = new PostServiceImpl();

        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        modelMapper.typeMap(Post.class, PostDto.class).addMappings(mapper -> mapper.skip(PostDto::setMediaId));
        ReflectionTestUtils.setField(postService, "modelMapper", modelMapper);

        // Dynamic proxy for PostRepo without byte-buddy agent attachment
        PostRepo dummyPostRepo = (PostRepo) Proxy.newProxyInstance(
                PostRepo.class.getClassLoader(),
                new Class<?>[]{PostRepo.class},
                (proxy, method, args) -> {
                    String methodName = method.getName();
                    if ("findById".equals(methodName)) {
                        Integer id = (Integer) args[0];
                        return Optional.ofNullable(postDatabase.get(id));
                    }
                    if ("incrementViews".equals(methodName)) {
                        incrementViewsCallCount++;
                        return 1;
                    }
                    return null;
                }
        );
        ReflectionTestUtils.setField(postService, "postRepo", dummyPostRepo);

        // Dynamic proxy for PostLikeRepo
        PostLikeRepo dummyLikeRepo = (PostLikeRepo) Proxy.newProxyInstance(
                PostLikeRepo.class.getClassLoader(),
                new Class<?>[]{PostLikeRepo.class},
                (proxy, method, args) -> {
                    if ("countByPost".equals(method.getName())) {
                        return 0L;
                    }
                    if ("existsByUserAndPost".equals(method.getName())) {
                        return false;
                    }
                    return null;
                }
        );
        ReflectionTestUtils.setField(postService, "postLikeRepo", dummyLikeRepo);

        // Dynamic proxy for PostBookmarkRepo
        PostBookmarkRepo dummyBookmarkRepo = (PostBookmarkRepo) Proxy.newProxyInstance(
                PostBookmarkRepo.class.getClassLoader(),
                new Class<?>[]{PostBookmarkRepo.class},
                (proxy, method, args) -> {
                    if ("countByPost".equals(method.getName())) {
                        return 0L;
                    }
                    if ("existsByUserAndPost".equals(method.getName())) {
                        return false;
                    }
                    return null;
                }
        );
        ReflectionTestUtils.setField(postService, "postBookmarkRepo", dummyBookmarkRepo);
    }

    @Test
    @DisplayName("Issue 3: First visit to a published post atomically increments views and returns updated count")
    void testFirstVisitIncrementsViews() {
        Integer postId = 1;
        Post post = new Post();
        post.setPostId(postId);
        post.setTitle("Introduction to Microservices");
        post.setContent("Microservices architecture guide");
        post.setStatus("PUBLISHED");
        post.setViews(0);

        User author = new User();
        author.setId(5);
        author.setName("Ashish");
        post.setUser(author);

        Category category = new Category();
        category.setCategoryId(2);
        category.setCategoryTitle("Microservices");
        post.setCategory(category);

        postDatabase.put(postId, post);

        PostDto result = postService.getPostById(postId);

        assertNotNull(result);
        assertEquals(1, result.getViews(), "Returned PostDto must have the incremented view count");
        assertEquals(1, incrementViewsCallCount, "incrementViews must be called exactly once");
    }

    @Test
    @DisplayName("Issue 3: Repeat visits by the same user or different users increment view count each time")
    void testRepeatVisitsIncrementViewsEachTime() {
        Integer postId = 42;
        Post post = new Post();
        post.setPostId(postId);
        post.setTitle("Java Concurrency in Practice");
        post.setContent("Concurrency guide");
        post.setStatus("PUBLISHED");
        post.setViews(10);

        User author = new User();
        author.setId(1);
        post.setUser(author);

        Category category = new Category();
        category.setCategoryId(1);
        category.setCategoryTitle("Java");
        post.setCategory(category);

        postDatabase.put(postId, post);

        // First visit (User A opens post)
        PostDto visit1 = postService.getPostById(postId);
        assertEquals(11, visit1.getViews(), "Visit 1 view count");

        // Repeat visit 1 (User B opens post)
        PostDto visit2 = postService.getPostById(postId);
        assertEquals(12, visit2.getViews(), "Visit 2 view count");

        // Repeat visit 2 (User A opens post again)
        PostDto visit3 = postService.getPostById(postId);
        assertEquals(13, visit3.getViews(), "Visit 3 view count");

        // Repeat visit 3 (User A opens post again)
        PostDto visit4 = postService.getPostById(postId);
        assertEquals(14, visit4.getViews(), "Visit 4 view count");

        // Verify atomic database increment was called for each visit
        assertEquals(4, incrementViewsCallCount, "Atomic incrementViews must be called for each separate visit");
    }

    @Test
    @DisplayName("Issue 3: Explicit visit endpoint incrementViews method performs atomic database update")
    void testExplicitIncrementViewsMethod() {
        Integer postId = 7;
        Post post = new Post();
        post.setPostId(postId);
        post.setStatus("PUBLISHED");
        post.setViews(5);

        postDatabase.put(postId, post);

        int updatedViews = postService.incrementViews(postId);

        assertEquals(6, updatedViews);
        assertEquals(1, incrementViewsCallCount);
    }

    @Test
    @DisplayName("Issue 3: DRAFT posts do not increment view counter")
    void testDraftPostDoesNotIncrementViews() {
        Integer postId = 99;
        Post draftPost = new Post();
        draftPost.setPostId(postId);
        draftPost.setStatus("DRAFT");
        draftPost.setViews(0);

        postDatabase.put(postId, draftPost);

        int views = postService.incrementViews(postId);

        assertEquals(0, views);
        assertEquals(0, incrementViewsCallCount, "Draft posts should not trigger incrementViews");
    }
}
