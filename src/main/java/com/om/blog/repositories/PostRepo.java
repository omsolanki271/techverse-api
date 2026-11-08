
package com.om.blog.repositories;

import com.om.blog.entities.Category;
import com.om.blog.entities.Media;
import com.om.blog.entities.Post;
import com.om.blog.entities.User;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PostRepo extends JpaRepository<Post, Integer> {

    Page<Post> findByUser(User user, Pageable pageable);

    Page<Post> findByCategory(Category category, Pageable pageable);

    List<Post> findByTitleContainingOrContentContaining(
            String titleKeyword,
            String contentKeyword
    );

    List<Post> findByMedia(Media media);

    // Search posts by category and title
    @Query("SELECT p FROM Post p WHERE " +
            "(:categoryId IS NULL OR p.category.categoryId = :categoryId) " +
            "AND (:title IS NULL OR :title = '' OR " +
            "LOWER(p.title) LIKE LOWER(CONCAT('%', :title, '%')))")
    List<Post> searchByCategoryAndTitle(
            @Param("categoryId") Integer categoryId,
            @Param("title") String title
    );
}
