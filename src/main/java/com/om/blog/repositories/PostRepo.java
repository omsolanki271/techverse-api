package com.om.blog.repositories;

import com.om.blog.entities.Category;
import com.om.blog.entities.Media;
import com.om.blog.entities.Post;
import com.om.blog.entities.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostRepo extends JpaRepository<Post , Integer> {

    List<Post> findByUser(User user);
    List<Post> findByCategory(Category category);
    Page<Post> findByUser(User user, Pageable pageable);

    Page<Post> findByCategory(Category category, Pageable pageable);
    List<Post> findByTitleContainingOrContentContaining( String titleKeyword, String contentKeyword);
    List<Post> findByMedia(Media media);

    @org.springframework.data.jpa.repository.Query("SELECT p FROM Post p WHERE (:categoryId IS NULL OR p.category.categoryId = :categoryId) AND (:title IS NULL OR :title = '' OR LOWER(p.title) LIKE LOWER(CONCAT('%', :title, '%')))")
    List<Post> searchByCategoryAndTitle(@org.springframework.data.repository.query.Param("categoryId") Integer categoryId, @org.springframework.data.repository.query.Param("title") String title);

    Integer postId(Integer postId);
}
