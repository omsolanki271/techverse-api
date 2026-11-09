package com.om.blog.repositories;

import com.om.blog.entities.Post;
import com.om.blog.entities.PostBookmark;
import com.om.blog.entities.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostBookmarkRepo extends JpaRepository<PostBookmark, Integer> {
    Optional<PostBookmark> findByPostAndUser(Post post, User user);
    boolean existsByPostAndUser(Post post, User user);
    long countByPost(Post post);
    Page<PostBookmark> findByUser(User user, Pageable pageable);
    List<PostBookmark> findByUser(User user);
    void deleteByPostAndUser(Post post, User user);
}

