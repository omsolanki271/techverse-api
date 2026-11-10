package com.om.blog.repositories;

import com.om.blog.entities.Category;
import com.om.blog.entities.Media;
import com.om.blog.entities.Post;
import com.om.blog.entities.User;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface PostRepo extends JpaRepository<Post, Integer> {

    Page<Post> findByUser(User user, Pageable pageable);

    // All published posts (matches published posts and backwards-compatible posts where status was null)
    @Query(value = "SELECT p FROM Post p WHERE p.status IS NULL OR UPPER(p.status) = 'PUBLISHED' OR UPPER(p.status) != 'DRAFT'",
           countQuery = "SELECT COUNT(p) FROM Post p WHERE p.status IS NULL OR UPPER(p.status) = 'PUBLISHED' OR UPPER(p.status) != 'DRAFT'")
    Page<Post> findAllPublished(Pageable pageable);

    // Published posts by Category
    @Query(value = "SELECT p FROM Post p WHERE p.category = :category AND (p.status IS NULL OR UPPER(p.status) = 'PUBLISHED' OR UPPER(p.status) != 'DRAFT')",
           countQuery = "SELECT COUNT(p) FROM Post p WHERE p.category = :category AND (p.status IS NULL OR UPPER(p.status) = 'PUBLISHED' OR UPPER(p.status) != 'DRAFT')")
    Page<Post> findByCategoryPublished(@Param("category") Category category, Pageable pageable);

    // Published posts by User
    @Query(value = "SELECT p FROM Post p WHERE p.user = :user AND (p.status IS NULL OR UPPER(p.status) = 'PUBLISHED' OR UPPER(p.status) != 'DRAFT')",
           countQuery = "SELECT COUNT(p) FROM Post p WHERE p.user = :user AND (p.status IS NULL OR UPPER(p.status) = 'PUBLISHED' OR UPPER(p.status) != 'DRAFT')")
    Page<Post> findByUserPublished(@Param("user") User user, Pageable pageable);

    // Draft posts by User
    @Query(value = "SELECT p FROM Post p WHERE p.user = :user AND UPPER(p.status) = 'DRAFT'",
           countQuery = "SELECT COUNT(p) FROM Post p WHERE p.user = :user AND UPPER(p.status) = 'DRAFT'")
    Page<Post> findByUserDrafts(@Param("user") User user, Pageable pageable);

    @Query("SELECT COUNT(p) FROM Post p WHERE p.user = :user AND (p.status IS NULL OR UPPER(p.status) = 'PUBLISHED' OR UPPER(p.status) != 'DRAFT')")
    long countPublishedByUser(@Param("user") User user);

    @Query("SELECT COUNT(p) FROM Post p WHERE p.user = :user AND UPPER(p.status) = 'DRAFT'")
    long countDraftsByUser(@Param("user") User user);

    long countByUser(User user);

    List<Post> findByTitleContainingOrContentContaining(
            String titleKeyword,
            String contentKeyword
    );

    List<Post> findByMedia(Media media);

    // Search published posts across all categories by keyword in title or content
    @Query("SELECT p FROM Post p WHERE " +
            "(p.status IS NULL OR UPPER(p.status) = 'PUBLISHED' OR UPPER(p.status) != 'DRAFT') " +
            "AND (LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(p.content) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Post> searchByKeyword(@Param("keyword") String keyword);

    // Search published posts within a specific category by keyword in title or content
    @Query("SELECT p FROM Post p WHERE " +
            "(p.status IS NULL OR UPPER(p.status) = 'PUBLISHED' OR UPPER(p.status) != 'DRAFT') " +
            "AND p.category.categoryId = :categoryId " +
            "AND (LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(p.content) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Post> searchByCategoryAndKeyword(
            @Param("categoryId") Integer categoryId,
            @Param("keyword") String keyword
    );

    // Search published posts by category and title or content
    @Query("SELECT p FROM Post p WHERE " +
            "(p.status IS NULL OR UPPER(p.status) = 'PUBLISHED' OR UPPER(p.status) != 'DRAFT') " +
            "AND (:categoryId IS NULL OR p.category.categoryId = :categoryId) " +
            "AND (:title IS NULL OR :title = '' OR " +
            "LOWER(p.title) LIKE LOWER(CONCAT('%', :title, '%')) OR " +
            "LOWER(p.content) LIKE LOWER(CONCAT('%', :title, '%')))")
    List<Post> searchByCategoryAndTitle(
            @Param("categoryId") Integer categoryId,
            @Param("title") String title
    );

    @Modifying
    @Transactional
    @Query("UPDATE Post p SET p.status = 'PUBLISHED' WHERE p.status IS NULL OR p.status = ''")
    int updateNullStatusToPublished();
}