package com.om.blog.repositories;

import com.om.blog.entities.Post;
import com.om.blog.entities.PostView;
import com.om.blog.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostViewRepo extends JpaRepository<PostView, Integer> {
    boolean existsByPostAndUser(Post post, User user);
}
